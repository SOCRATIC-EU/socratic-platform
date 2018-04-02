/**
 *
 */
package de.atb.socratic.service.security;

/*-
 * #%L
 * socratic-platform
 * %%
 * Copyright (C) 2016 - 2018 Institute for Applied Systems Technology Bremen GmbH (ATB)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ejb.EJBException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.persistence.NoResultException;

import de.atb.socratic.authorization.authentication.ldap.ActiveDirectory;
import de.atb.socratic.authorization.authentication.ldap.ActiveDirectory.ActiveDirectoryUser;
import de.atb.socratic.exception.AutoCreatedLDAPUserException;
import de.atb.socratic.exception.AutoCreatedLDAPUserWithoutCompanyException;
import de.atb.socratic.exception.NoPendingRegistrationException;
import de.atb.socratic.exception.NoPendingResetPWRequestException;
import de.atb.socratic.exception.NoSuchUserException;
import de.atb.socratic.exception.RegistrationNotConfirmedException;
import de.atb.socratic.exception.RegistrationTimeoutException;
import de.atb.socratic.exception.ResetPWRequestTimeoutException;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.exception.UserRegistrationException;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@ApplicationScoped
public class AuthenticationService {

    public static final String PW_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[!§$%&@#_,.-|<>]).{6,20})";

    // inject a logger
    @Inject
    Logger logger;

    @Inject
    ActiveDirectory activeDirectory;

    @Inject
    UserService userService;

    @Inject
    CompanyService companyService;

    @Inject
    EmploymentService employmentService;

    private SecureRandom random = new SecureRandom();

    /**
     * @param email
     * @throws UserRegistrationException
     */
    public void isEmailAlreadyRegistered(String email) throws UserRegistrationException {
        // check if user already exists
        Long count = userService.countByEmail(email);
        if (count > 0) {
            User existingUser = getUserByEmail(email);
            if ((existingUser != null)
                    && !existingUser.getRegistrationStatus().equals(
                    RegistrationStatus.CANCELLED)) {
                // user already exists and has pending or confirmed registration
                throw new UserRegistrationException(
                        "A user with this email is already registered.");
            }
        }
    }

    /**
     * @param nickName
     * @throws UserRegistrationException
     */
    public void isNickNameAlreadyRegistered(String nickName) throws UserRegistrationException {
        // check if user already exists
        Long count = userService.countByNickName(nickName);
        if (count > 0) {
            User existingUser = getUserByNickName(nickName);
            if ((existingUser != null)
                    && !existingUser.getRegistrationStatus().equals(
                    RegistrationStatus.CANCELLED)) {
                // user already exists and has pending or confirmed registration
                throw new UserRegistrationException(
                        "A user with this nickName is already registered.");
            }
        }
    }

    public void isLDAPPrincipalAlreadyRegistered(String principal) throws UserRegistrationException {
        // check if user already exists
        Long count = userService.countByLDAPPrincipal(principal);
        if (count > 0) {
            User existingUser = getUserByLDAPLogin(principal);
            if (existingUser != null) {
                // user already exists and has pending or confirmed registration
                throw new UserRegistrationException(
                        "A user with this LDAP principal is already registered.");
            }
        }
    }

    public boolean isLinkedInIDExistent(String linkedInId) {
        return userService.countByLinkedInId(linkedInId) > 0;
    }

    public User authenticateUserThroughLinkedIn(final String linkedInId) {
        return getUserByLinkedInId(linkedInId);
    }

    public User authenticateUserThroughFacebook(final String facebookId) {
        return getUserByFacebookId(facebookId);
    }

    /**
     * @param user
     * @param password
     * @return
     */
    public User register(User user, String password) {
        // register user
        try {
            // generate hashed + salted password and store user with password
            // and salt in db
            byte[] salt = generateSalt();
            byte[] encryptedPassword = getEncryptedPassword(password, salt);

            user.setPassword(encryptedPassword);
            user.setPwSalt(salt);

            // set registration information
            user.setRegisteredThroughEFF(true);
            user.setRegistrationToken(generateToken());
            user.setRegistrationStatus(RegistrationStatus.PENDING);
            user.setRegistrationDate(new Date());

            user = userService.create(user);

            // and add default employment
            return addDefaultEmployment(user);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("An unexpected error occurred!", e);
        }
    }

    /**
     * @param user
     * @return
     */
    public User addDefaultEmployment(User user) {
        Employment employment = new Employment();
        employment.setUser(user);
        employment.setCompany(getDefaultCompany());

        // in socratic users may do anything for now.
        employment.setRole(UserRole.MANAGER);
        employment = employmentService.create(employment);
        user.getEmployments().add(employment);
        return userService.update(user);
    }

    public User registerLDAPUser(User user, String password) {
        try {
            // generate hashed + salted password and store user with password
            // and salt in db although this is senseless, because we'll use LDAP
            // later on
            byte[] salt = generateSalt();
            byte[] encryptedPassword = getEncryptedPassword(password + "-dummy", salt);

            user.setPassword(encryptedPassword);
            user.setPwSalt(salt);
            user = userService.create(user);
            return user;
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("An unexpected error occurred!", e);
        }
    }

    /**
     * @param email
     * @return
     * @throws NoPendingRegistrationException
     */
    public User resetRegistrationInformation(String email)
            throws NoPendingRegistrationException {
        // check if user already exists
        User user = getUserByEmail(email);
        if ((user == null)
                || !user.getRegistrationStatus().equals(
                RegistrationStatus.PENDING)) {
            // user doesn't exist or has already confirmed/cancelled
            // registration
            throw new NoPendingRegistrationException();
        }

        // set new registration information
        user.setRegistrationToken(generateToken());
        user.setRegistrationStatus(RegistrationStatus.PENDING);
        user.setRegistrationDate(new Date());
        return userService.update(user);
    }

    /**
     * @param token
     * @return
     * @throws NoPendingRegistrationException
     * @throws RegistrationTimeoutException
     */
    public User checkRegistrationStatus(String token)
            throws NoPendingRegistrationException, RegistrationTimeoutException {
        // check arguments
        if ((token == null) || token.isEmpty()) {
            throw new NoPendingRegistrationException(
                    "No pending registration found!");
        }
        // load user
        User user = getPendingUserByRegistrationToken(token);
        // check if registration token has timed out
        if (isRegistrationTimeout(user)) {
            throw new RegistrationTimeoutException(user.getEmail());
        }
        return user;
    }

    /**
     * @param email
     * @param password
     * @param registrationStatus
     * @return
     * @throws UserAuthenticationException
     */
    public User completeRegistration(String email, String password,
                                     RegistrationStatus registrationStatus)
            throws UserAuthenticationException {
        // load user
        User user = getUserByEmail(email);
        if ((user == null)
                || user.getRegistrationStatus().equals(
                RegistrationStatus.CANCELLED)) {
            throw new UserAuthenticationException(
                    "Unknown email and password combination.");
        }
        // authenticate
        boolean authenticated = authenticate(password, user.getPassword(), user.getPwSalt());
        if (!authenticated) {
            throw new UserAuthenticationException(
                    "Unknown email and password combination.");
        }
        // successfully authenticated --> confirm or cancel registration
        try {
            // finish registration process
            return finishRegistration(user, registrationStatus);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("An unexpected error occurred!", e);
        }
    }

    /**
     * @param email
     * @param password
     * @return
     * @throws RegistrationNotConfirmedException
     * @throws AutoCreatedLDAPUserException
     * @throws AutoCreatedLDAPUserWithoutCompanyException
     */
    public User authenticate(String email, String password)
            throws UserAuthenticationException,
            RegistrationNotConfirmedException,
            AutoCreatedLDAPUserException,
            AutoCreatedLDAPUserWithoutCompanyException {
        // load user
        User user = getUserByEmail(email);

        // try with LDAP
        handleLDAPLogin(user, email, password);

        if ((user == null) || user.getRegistrationStatus().equals(RegistrationStatus.CANCELLED)) {
            throw new UserAuthenticationException("Unknown email and password combination.");
        }

        // authenticate
        boolean authenticated = authenticate(password, user.getPassword(), user.getPwSalt());
        if (!authenticated) {
            throw new UserAuthenticationException("Unknown email and password combination.");
        }
        if (authenticated && user.getRegistrationStatus().equals(RegistrationStatus.PENDING)) {
            throw new RegistrationNotConfirmedException();
        }
        return user;
    }

    /**
     * @param user
     * @param password
     * @return
     * @throws UserAuthenticationException
     */
    public void authenticateForPWReset(User user, String password) throws UserAuthenticationException {
        if (!authenticateAgainstLDAP(user, password)) {
            if (!authenticate(password, user.getPassword(), user.getPwSalt())) {
                throw new UserAuthenticationException("Unknown email and password combination.");
            }
        }
    }

    /**
     * @param user
     * @param attemptedPassword
     * @return
     */
    private boolean authenticateAgainstLDAP(User user, String attemptedPassword) {
        if ((user == null) || !user.authenticatesThroughLDAP()) {
            return false;
        }
        if (activeDirectory.isEnabled()) {
            try {
                LdapContext connection = activeDirectory.getConnection(user.getLdapLogin(), attemptedPassword);
                connection.close();
                return true;
            } catch (NamingException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * @param user
     * @param email
     * @param password
     * @throws AutoCreatedLDAPUserException
     * @throws UserAuthenticationException
     * @throws AutoCreatedLDAPUserWithoutCompanyException
     * @throws RegistrationNotConfirmedException
     */
    private void handleLDAPLogin(User user, String email, String password)
            throws AutoCreatedLDAPUserException, UserAuthenticationException,
            AutoCreatedLDAPUserWithoutCompanyException,
            RegistrationNotConfirmedException {
        if (activeDirectory.isEnabled()) {
            if ((user == null)) {
                // there is no EFF user yet, try to create one from LDAP data
                autoCreateUserFromLDAP(email, password);
            } else {
                // we have an existing user, try to login with LDAP and update
                // data if needed
                loginAndUpdateUserFromLDAP(user, email, password);
            }
        }
    }

    /**
     * @param user
     * @param email
     * @param password
     * @throws AutoCreatedLDAPUserException
     * @throws AutoCreatedLDAPUserWithoutCompanyException
     * @throws UserAuthenticationException
     * @throws RegistrationNotConfirmedException
     */
    private void loginAndUpdateUserFromLDAP(User user, String email, String password)
            throws AutoCreatedLDAPUserException,
            AutoCreatedLDAPUserWithoutCompanyException,
            UserAuthenticationException, RegistrationNotConfirmedException {

        // check if he is still a valid user
        if (user.getRegistrationStatus().equals(RegistrationStatus.CANCELLED)) {
            throw new UserAuthenticationException("Unknown email and password combination.");
        }
        if (user.getRegistrationStatus().equals(RegistrationStatus.PENDING)) {
            throw new RegistrationNotConfirmedException();
        }

        // authenticate agains LDAP
        LdapContext connection = null;
        try {
            connection = activeDirectory.getConnection(email, password);
        } catch (NamingException e) {
            // if we cannot connect via LDAP, maybe the user wants to login with his regular EFF password
            return;
        }

        // update user data
        try {
            ActiveDirectoryUser adUser = activeDirectory.getUser(email, connection);
            connection.close();
            if (adUser != null) {
                if (StringUtils.isBlank(user.getLdapLogin())) {
                    user.setLdapLogin(adUser.getLogin());
                    userService.update(user);
                }
                if (user.getCurrentEmployment() == null) {
                    Company company = companyService.findByLDAPDomain(activeDirectory.getDomainNameFromPrincipal(email));
                    if (company != null) {
                        Employment emp = new Employment(user, company);
                        employmentService.create(emp);
                        user.getEmployments().add(emp);
                        user.setCurrentEmployment(emp);
                        userService.update(user);
                        throw new AutoCreatedLDAPUserException(user, company);
                    } else {
                        throw new AutoCreatedLDAPUserWithoutCompanyException(user);
                    }
                } else {
                    throw new AutoCreatedLDAPUserException(user, user.getCurrentCompany());
                }
            }
        } catch (NamingException e) {
            throw new UserAuthenticationException(e.getExplanation(), e);
        }
    }

    /**
     * @param email
     * @param password
     * @throws AutoCreatedLDAPUserException
     * @throws AutoCreatedLDAPUserWithoutCompanyException
     * @throws UserAuthenticationException
     */
    private void autoCreateUserFromLDAP(String email, String password)
            throws AutoCreatedLDAPUserException,
            AutoCreatedLDAPUserWithoutCompanyException,
            UserAuthenticationException {
        try {
            LdapContext connection = activeDirectory.getConnection(email, password);
            ActiveDirectoryUser adUser = activeDirectory.getUser(email, connection);
            connection.close();
            if (adUser != null) {
                if (!StringUtils.isEmpty(adUser.getEMail())) {
                    if (getUserByEmail(adUser.getEMail()) != null) {
                        throw new UserAuthenticationException("A user with your E-Mail address already exists!");
                    }
                }
                Company company = companyService.findByLDAPDomain(activeDirectory.getDomainNameFromPrincipal(email));
                User newUser = registerLDAPUser(User.fromActiveDirectoryUser(adUser), password);
                if (company != null) {
                    Employment emp = new Employment(newUser, company);
                    employmentService.create(emp);
                    newUser.getEmployments().add(emp);
                    newUser.setCurrentEmployment(emp);
                    userService.update(newUser);
                    throw new AutoCreatedLDAPUserException(newUser, company);
                } else {
                    throw new AutoCreatedLDAPUserWithoutCompanyException(newUser);
                }
            }
        } catch (CommunicationException e) {
            logger.warn(e.getExplanation(), e);
            throw new UserAuthenticationException("Unable to to connect to LDAP server and user with e-Mail adress '" + email + "' does not seem to exist yet.");
        } catch (NamingException e) {
            logger.warn(e.getExplanation(), e);
            throw new UserAuthenticationException(e.getExplanation(), e);
        }
    }

    /**
     * @param email
     * @return
     * @throws UserAuthenticationException
     */
    public User requestNewPassword(String email) throws NoSuchUserException {
        // load user
        User user = getConfirmedUserByEmail(email);
        if (user == null || !user.isRegisteredThroughEFF()) {
            throw new NoSuchUserException();
        }

        // set reset password request information
        user.setResetPWRequestToken(generateToken());
        user.setResetPWRequestDate(new Date());
        return userService.update(user);
    }

    /**
     * @param user
     * @param password
     * @return
     */
    public User resetPassword(User user, String password) {
        // generate hashed + salted password and store user with password
        // and salt in db
        try {
            byte[] salt = generateSalt();
            byte[] encryptedPassword = getEncryptedPassword(password, salt);

            user.setPassword(encryptedPassword);
            user.setPwSalt(salt);

            // set registration information
            user.setResetPWRequestToken(null);
            user.setResetPWRequestDate(null);

            return userService.update(user);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred!", e);
        }
    }

    /**
     * @param token
     * @return
     * @throws NoPendingResetPWRequestException
     * @throws ResetPWRequestTimeoutException
     * @throws UserRegistrationException
     */
    public User checkResetPWToken(String token)
            throws NoPendingResetPWRequestException,
            ResetPWRequestTimeoutException {
        // check arguments
        if ((token == null) || token.isEmpty()) {
            throw new NoPendingResetPWRequestException(
                    "No pending request for resetting password found!");
        }
        // load user
        User user = getUserByResetPWRequestToken(token);
        // check if reset password token has timed out
        if (isResetPWRequestTimeout(user)) {
            throw new ResetPWRequestTimeoutException(
                    "Your request for resetting your password has timed out!");
        }
        return user;
    }

    /**
     * @param password
     * @param salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static byte[] getEncryptedPassword(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        // PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
        // specifically names SHA-1 as an acceptable hashing algorithm for
        // PBKDF2
        String algorithm = "PBKDF2WithHmacSHA1";
        // SHA-1 generates 160 bit hashes, so that's what makes sense here
        int derivedKeyLength = 160;
        // Pick an iteration count that works for you. The NIST recommends at
        // least 1,000 iterations:
        // http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
        // iOS 4.x reportedly uses 10,000:
        // http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
        int iterations = 20000;

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations,
                derivedKeyLength);

        SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

        return f.generateSecret(spec).getEncoded();
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static byte[] generateSalt() throws NoSuchAlgorithmException {
        // VERY important to use SecureRandom instead of just Random
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        // Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
        byte[] salt = new byte[8];
        random.nextBytes(salt);

        return salt;
    }

    /**
     * @return
     */
    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * @param email
     * @return
     */
    private User getUserByEmail(String email) {
        User user = userService.getByMailNullIfNotFound(email);
        if (user == null) {
            user = userService.getByLDAPLoginNullIfNotFound(email);
        }
        if (user == null) {
            user = userService.getByFacebookIdNullIfNotFound(email);
        }
        return user;
    }

    /**
     * @param nickName
     * @return
     */
    private User getUserByNickName(String nickName) {
        User user = userService.getByNickNameNullIfNotFound(nickName);
        return user;
    }

    private User getUserByLDAPLogin(String login) {
        return userService.getByLDAPLoginNullIfNotFound(login);
    }

    private User getUserByLinkedInId(String id) {
        return userService.getByLinkedInIdNullIfNotFound(id);
    }

    private User getUserByFacebookId(String id) {
        return userService.getByFacebookIdNullIfNotFound(id);
    }

    /**
     * @param email
     * @return
     * @throws RuntimeException
     */
    private User getConfirmedUserByEmail(String email) {
        try {
            return userService.getByEmailAndStatus(email, RegistrationStatus.CONFIRMED);
        } catch (Exception e) {
            if ((e instanceof EJBException) && (e.getCause() != null) && (e.getCause() instanceof NoResultException)) {
                return null;
            } else {
                logger.error(e);
                throw new RuntimeException("An unexpected error occurred!", e);
            }
        }
    }

    /**
     * @param token
     * @return
     * @throws NoPendingResetPWRequestException
     * @throws UserRegistrationException
     */
    private User getUserByResetPWRequestToken(String token)
            throws NoPendingResetPWRequestException {
        try {
            return userService.getByResetPWRequestToken(token);
        } catch (Exception e) {
            if ((e instanceof EJBException) && (e.getCause() != null)
                    && (e.getCause() instanceof NoResultException)) {
                // reset password token does not exist
                logger.error(e);
                throw new NoPendingResetPWRequestException(
                        "No pending request for resetting password found!");
            } else {
                logger.error(e);
                throw new RuntimeException("An unexpected error occurred!", e);
            }
        }
    }

    /**
     * @param token
     * @return
     * @throws NoPendingRegistrationException
     * @throws UserRegistrationException
     */
    private User getPendingUserByRegistrationToken(String token)
            throws NoPendingRegistrationException {
        try {
            return userService.getByRegistrationTokenPending(token);
        } catch (Exception e) {
            if ((e instanceof EJBException) && (e.getCause() != null)
                    && (e.getCause() instanceof NoResultException)) {
                // confirmation token does not exist
                logger.error(e);
                throw new NoPendingRegistrationException(
                        "No pending registration found!");
            } else {
                logger.error(e);
                throw new RuntimeException("An unexpected error occurred!", e);
            }
        }
    }

    /**
     * Checks if registration has been more than one day ago.
     *
     * @param user
     * @return
     */
    private boolean isRegistrationTimeout(User user) {
        Date now = new Date();
        long oneDay = 86400000l;
        if ((now.getTime() - user.getRegistrationDate().getTime()) > oneDay) {
            return true;
        }
        return false;
    }

    /**
     * Checks if request for resetting password has been more than half a day
     * ago.
     *
     * @param user
     * @return
     */
    private boolean isResetPWRequestTimeout(User user) {
        Date now = new Date();
        long halfDay = 43200000l;
        if ((now.getTime() - user.getResetPWRequestDate().getTime()) > halfDay) {
            return true;
        }
        return false;
    }

    /**
     * @param user
     * @param registrationStatus
     * @return
     * @throws Exception
     */
    private User finishRegistration(User user,
                                    RegistrationStatus registrationStatus) throws Exception {
        if (registrationStatus.equals(RegistrationStatus.CONFIRMED)) {
            // update status, remove token
            user.setRegistrationStatus(registrationStatus);
            user.setRegistrationToken(null);
            userService.update(user);
        } else if (registrationStatus.equals(RegistrationStatus.CANCELLED)) {
            // delete the user
            userService.delete(user);
        }
        return user;
    }

    /**
     * @param attemptedPassword
     * @param encryptedPassword
     * @param salt
     * @return
     */
    private boolean authenticate(String attemptedPassword,
                                 byte[] encryptedPassword, byte[] salt) {
        try {
            // Encrypt the clear-text password using the same salt that was used to
            // encrypt the original password
            byte[] encryptedAttemptedPassword = getEncryptedPassword(
                    attemptedPassword, salt);

            // Authentication succeeds if encrypted password that the user entered
            // is equal to the stored hash
            return Arrays.equals(encryptedPassword, encryptedAttemptedPassword);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException("An unexpected error occurred!", e);
        }
    }

    private String generateRandomPassword() {
        return new BigInteger(130, random).toString(32);
    }

    private Company getDefaultCompany() {
        // there can only be one company -- see ModelInitializer
        if (companyService.countAll() > 0) {
            return companyService.getAll().get(0);
        }
        return null;
    }
}

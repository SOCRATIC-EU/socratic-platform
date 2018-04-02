package de.atb.socratic.authorization.authentication.http;

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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import de.atb.socratic.authorization.authentication.AuthenticationType;
import de.atb.socratic.authorization.authentication.RequiresAuthentication;
import de.atb.socratic.exception.RegistrationNotConfirmedException;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.exception.UserAuthenticationResponseException;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.security.AuthenticationService;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.solder.logging.Logger;

/**
 * Authenticator
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@ApplicationScoped
public abstract class HttpRequestAuthenticator implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 2624468951337076719L;
    protected static final String HTTP_HEADER_EMPLOYMENT_KEY = "CurrentEmploymentId";

    @Inject
    Logger logger;

    @Inject
    EmploymentService employmentService;

    @Inject
    AuthenticationService authService;

    /**
     * Determines whether the Authenticator can handle one or more of the given
     * authentication types.
     * <p>
     * Only if the Authenticator is able to handle at least one of the given
     * types, the {@link HttpRequestAuthenticator#verifyRequest(HttpServletRequest)} is called in order to
     * ensure the HttpRequest uses a valid authentication type which can be
     * handled by this Authenticator.
     *
     * @param types A set of authentication types where at least one must be
     *              handable by this Authenticator.
     * @return <code>true</code> if this Authenticator can handle at least one
     * of the given set of types, <code>false</code> otherwise.
     */
    public abstract boolean canHandleAuthentication(EnumSet<AuthenticationType> types);

    /**
     * Checks if the given HttpRequest contains information about authentication
     * that this Authenticator knows to handle.
     * <p>
     * If the given request is valid and handable by this Authenticator,
     * {@link HttpRequestAuthenticator#authenticate(User, Employment, HttpServletRequest, RequiresAuthentication)}
     * can be called.
     *
     * @param request the HttpRequest with authentication information to be handled
     *                by this Authenticator.
     * @return <code>true</code> if the given HttpRequest contains
     * authentication information this Authenticator can handle,
     * <code>false</code> otherwise.
     */
    public abstract boolean verifyRequest(HttpServletRequest request);

    /**
     * Analyses the given HttpRequest in order to extract username/password or
     * other identification data in order to retrieve the corresponding user
     * from the {@link AuthenticationService}.
     * <p>
     * <p>
     * The existence of authentication information according to schema
     * implemented by this Authenticator is guaranteed at this point when
     * {@link HttpRequestAuthenticator#verifyRequest(HttpServletRequest)} returned <code>true</code>.
     * <p>
     * <p>
     * If the credentials are wrong, there is no valid token stored for the user
     * or any other problem is preventing identifcation of a user, this method
     * should always throw an {@link UnauthorizedException}.
     *
     * @param request the HttpRequest containing potential credentials, tokens etc.
     *                for identification of a User.
     * @return The user that tries to authenticate with information in the given
     * HttpRequest.
     * @throws UnauthorizedException thrown whenever a user cannot be identified based on the
     *                               information given in the HttpRequest (i.e. invalid username,
     *                               invalid password, invalid tokens etc.).
     */
    public abstract User getAuthenticatedUserFromRequest(HttpServletRequest request) throws UserAuthenticationException,
            RegistrationNotConfirmedException;

    public abstract AuthenticationType getAuthenticationType();

    public static void authenticate(User user, Employment currentEmployment) throws UserAuthenticationException {
        authenticate(user, currentEmployment, null);
    }

    public static void authenticate(User user, Employment currentEmployment, RequiresAuthentication annotation)
            throws UserAuthenticationException {
        EnumSet<UserRole> allowedRoles = UserRole.getRequiredRolesFromAnnotation(annotation);

        if (!currentEmployment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Given user '" + user.getEmail() + "' is not part of specified employment (id "
                    + currentEmployment.getId() + ")!");
        }
        UserRole userRole = currentEmployment.getRole();
        if (!userRole.hasAnyRoles(allowedRoles)) {
            throw new UnauthorizedException("Access  is only allowed for roles '" + allowedRoles + "', given user '" + user.getEmail()
                    + "' with specified employment (id " + currentEmployment.getId() + ") has role '" + currentEmployment.getRole() + "'!");
        }
        if (annotation != null) {
            Set<String> userLogins = getUserLoginsFromAnnotation(annotation);
            if (userLogins.size() > 0) {
                for (String userLogin : userLogins) {
                    if ((userLogin != null) && userLogin.trim().equalsIgnoreCase(user.getEmail().trim())) {
                        return;
                    }
                }
                throw new UserAuthenticationResponseException("Access is not allowed for given user '" + user.getEmail() + "!");
            }
        }
    }

    public void authenticate(User user, Employment currentEmployment, HttpServletRequest request, RequiresAuthentication annotation)
            throws UnauthorizedException {
        EnumSet<UserRole> allowedRoles = UserRole.getRequiredRolesFromAnnotation(annotation);

        if (!currentEmployment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Given user '" + user.getEmail() + "' is not part of specified employment (id "
                    + currentEmployment.getId() + ")!");
        }
        UserRole userRole = currentEmployment.getRole();
        if (!userRole.hasAnyRoles(allowedRoles)) {
            throw new UnauthorizedException("Access to resource " + request.getRequestURI() + " is only allowed for roles '" + allowedRoles
                    + "', given user '" + user.getEmail() + "' with specified employment (id " + currentEmployment.getId() + ") has role '"
                    + currentEmployment.getRole() + "'!");
        }
        Set<String> userLogins = getUserLoginsFromAnnotation(annotation);
        if (userLogins.size() > 0) {
            for (String userLogin : userLogins) {
                if ((userLogin != null) && userLogin.trim().equalsIgnoreCase(user.getEmail().trim())) {
                    return;
                }
            }
            throw new UnauthorizedException("Access to resource " + request.getRequestURI() + " is not allowed for given user '"
                    + user.getEmail() + "!");
        }
    }

    public static Set<String> getUserLoginsFromAnnotation(RequiresAuthentication annotation) {
        if ((annotation.allowedUserLogins() != null) && (annotation.allowedUserLogins().length > 0)) {
            return Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(annotation.allowedUserLogins())));
        } else {
            return Collections.emptySet();
        }
    }

    public Employment getCurrentEmploymentFromRequest(User user, HttpServletRequest request) {
        Set<Employment> employments = user.getEmployments();
        if ((employments == null) || employments.isEmpty()) {
            return null;
        }
        Employment currentEmployment = null;
        if (employments.size() == 1) {
            currentEmployment = employments.iterator().next();
        } else {
            currentEmployment = getEmploymentFromRequest(request);
        }
        return currentEmployment;
    }

    protected Employment getEmploymentFromRequest(HttpServletRequest request) throws EntityNotFoundException {
        Long employmentId = getEmploymentIdFromRequest(request);
        if (employmentId != null) {
            return employmentService.getById(employmentId);
        }
        return null;
    }

    private Long getEmploymentIdFromRequest(HttpServletRequest request) {
        String employmentId = request.getHeader(HTTP_HEADER_EMPLOYMENT_KEY);
        if ((employmentId != null) && (!employmentId.isEmpty())) {
            try {
                return Long.valueOf(employmentId);
            } catch (NumberFormatException e) {
                logger.debug("Non-numeric employment id (" + employmentId + ") given in HttpRequest!", e);
            }
        }
        return null;
    }

}

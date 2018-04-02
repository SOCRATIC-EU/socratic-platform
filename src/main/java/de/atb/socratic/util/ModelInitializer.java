/**
 *
 */
package de.atb.socratic.util;

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
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.employment.DepartmentService;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.CampaignTimerService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.inception.InnovationObjectiveService;
import de.atb.socratic.service.inception.ScopeService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.service.periodicTimer.PeriodicTimerService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.jboss.solder.logging.Logger;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.bound.Bound;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequest;
import org.jboss.weld.context.bound.MutableBoundRequest;
import org.jboss.weld.context.http.Http;
import org.joda.time.DateTime;

/**
 * @author ATB
 */
@Singleton
@Startup
@ApplicationScoped
public class ModelInitializer implements Serializable {

    private static final long serialVersionUID = 7617782923344156164L;

    public static final List<String> tags = Arrays.asList("manufacturing",
            "welding", "laser-cut", "coating", "management",
            "human resources",
            "IT", "sales", "ELAM", "cloud", "mobile");

    @Inject
    @Http
    ConversationContext context;

    @Inject
    @Bound
    BoundConversationContext boundContext;

    // inject a logger
    @Inject
    Logger logger;

    @Inject
    EntityManager em;

    @Inject
    CampaignTimerService campaignTimerService;

    @Inject
    PeriodicTimerService periodicTimerService;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    UserService userService;

    @Inject
    TagService tagService;

    @Inject
    CompanyService companyService;

    @Inject
    DepartmentService departmentService;

    @Inject
    EmploymentService employmentService;

    @Inject
    InnovationObjectiveService innovationObjectiveService;

    @Inject
    CampaignService campaignService;

    @Inject
    ScopeService scopeService;

    @Inject
    IdeaService ideaService;

    @Inject
    ConfigFileHandler configFileHandler;

    private static final String ddlKey = "hibernate.hbm2ddl.auto";
    private static final String create = "create";
    private static final String ADMIN_FIRSTNAME = "SOCRATIC";
    private static final String ADMIN_LASTNAME = "Admin";
    private String ADMIN_EMAIL = "";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    private static final String ADMIN_COMPANY_SHORT_NAME = "SOC";
    private static final String ADMIN_COMPANY_FULL_NAME = "SOCRATIC";

    private static final Map<String, User> cachedUsers = new HashMap<>();
    private static final Map<String, Company> cachedCompanies = new HashMap<>();

    @PostConstruct
    public void populateDB() {
        logger.info("----- POPULATING DB -----");
        BoundRequest storage = null;
        if (!context.isActive() && !boundContext.isActive()) {
            Map<String, Object> session = new HashMap<>();
            Map<String, Object> request = new HashMap<>();
            storage = new MutableBoundRequest(request, session);
            boundContext.associate(storage);
            boundContext.activate();
        }

        try {
            // initialize hibernate full text search
            FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
            ftem.createIndexer().startAndWait();

            // check if we need to re-populate the db
            if (!shouldPopulateDB()) {
                // create base data
                createAbsolutelyNecessaryBaseData();
            } else {
                // create the whole hullaballoo
                createAllData();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (storage != null) {
                boundContext.deactivate();
                boundContext.dissociate(storage);
            }
        }
    }

    @PreDestroy
    public void preDestroy() {
        logger.info("----- SHUTTING DOWN -----");
    }

    /**
     * Checks whether we should populate the DB, depending on
     * hibernate.hbm2ddl.auto setting in persistence.xml.
     *
     * @return
     */
    private boolean shouldPopulateDB() {
        Map<String, Object> props = em.getEntityManagerFactory().getProperties();
        if (props.containsKey(ddlKey)) {
            Object ddl = props.get(ddlKey);
            if ((ddl instanceof String) && ((String) ddl).contains(create)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates base data for specified Business Case, or nothing at all, if no
     * business case specified.
     *
     * @throws Exception
     */
    private void createAbsolutelyNecessaryBaseData() throws Exception {

        periodicTimerService.createTimer();

        //create admin account and company
        createAdminAccount();
        // create default company & employment
        createAdminCompany(cachedUsers.get(ADMIN_EMAIL));
        createAdminEmployment();
    }

    /**
     * @throws Exception
     */
    private void createAllData() throws Exception {
        // cancel all existing timers for campaigns
        campaignTimerService.cancelAllTimers();

        // cancel all existing timers for prio configuration
        periodicTimerService.createTimer();

        // create users
        createAdminAccount();
        createUsers();

        // create admin company & employments
        createAdminCompany(cachedUsers.get(ADMIN_EMAIL));
        createAdminEmployment();
        createEmployments();
    }

    /**
     * @return
     */
    public Campaign createTestCampaignObject() {
        Campaign testCampaign = new Campaign();
        testCampaign.setName("Test Campaign for Timer service");
        testCampaign.setElevatorPitch("ElevatorPitch");
        testCampaign.setSocialChallenge("socialChallenge");
        testCampaign.setBeneficiaries("beneficiaries");
        testCampaign.setPotentialImpact("potentialImpact");
        testCampaign.setLevelOfSupport("levelOfSupport");
        testCampaign.setIdeasProposed("ideasProposed");
        testCampaign.setOpenForDiscussion(true);

        testCampaign.setDescription("description");
        testCampaign.setCampaignType(CampaignType.FREE_FORM);
        testCampaign.setActive(true);
        testCampaign.setCreatedBy(getTestUser());
        testCampaign.setCreatedOn(new Date());
        testCampaign.setCompany(getTestCompany());

        // set different dates
        DateTime definitionStartDate = new DateTime();
        testCampaign.setChallengeOpenForDiscussionStartDate(definitionStartDate.toDate());
        testCampaign.setChallengeOpenForDiscussionEndDate(definitionStartDate.plusMinutes(2).toDate());
        testCampaign.setIdeationStartDate(definitionStartDate.plusMinutes(3).toDate());
        testCampaign.setIdeationEndDate(definitionStartDate.plusMinutes(4).toDate());

        testCampaign.setSelectionStartDate(definitionStartDate.plusMinutes(5).toDate());
        testCampaign.setSelectionEndDate(definitionStartDate.plusMinutes(6).toDate());
        testCampaign.setDueDate(testCampaign.getChallengeOpenForDiscussionEndDate());

        // logic for Challenge Innovation Status
        testCampaign.setDefinitionActive(true);
        testCampaign.setInnovationStatus(InnovationStatus.DEFINITION);

        campaignService.create(testCampaign);

        return testCampaign;
    }

    private boolean isEmailValid(String email) {
        logger.debugf("Checking if given email is valid or not: ", email);
        if (pattern.matcher(email).matches()) {
            logger.infof("given email is valid: ", email);
            return true;
        } else {
            logger.errorf("given email is not valid: ", email);
            return true;
        }

    }

    private void createAdminAccount() throws Exception {
        ADMIN_EMAIL = configFileHandler.getAdminEmail();
        if (isEmailValid(ADMIN_EMAIL) && StringUtils.isNotBlank(configFileHandler.getAdminPW())) {
            final User admin;
            if (userService.countByEmail(ADMIN_EMAIL) == 0) {
                admin = createAndCacheUser(ADMIN_FIRSTNAME, ADMIN_LASTNAME, ADMIN_EMAIL, configFileHandler.getAdminPW());
            } else {
                admin = userService.getByEmail(ADMIN_EMAIL);
            }
            cachedUsers.put(ADMIN_EMAIL, admin);
        } else {
            throw new RuntimeException("Cannot create admin user - invalid email or password!");
        }
    }

    /**
     * @param createdBy
     * @return
     */
    private Company createAdminCompany(User createdBy) {
        final String adminCompanyServices = "SOCRATIC is a set of tools that allows a company to simply and effectively set-up, monitor and follow-up a business innovation process. SOCRATIC is targeted to industrial SMEs and is made up of two key elements, a great and user-friendly web-based platform, and a comprehensive set of methodologies and tools -compiled in an illustrated case-based workbook-, from which any SME can get inspiration and skills to manage their innovation process.";

        Company adminCompany = new Company();
        if (createdBy != null && createdBy.getEmail() != null && isEmailValid(createdBy.getEmail())) {
            if (companyService.countByFullName(ADMIN_COMPANY_FULL_NAME) == 0) {
                adminCompany = new Company();
                adminCompany.setShortName(ADMIN_COMPANY_SHORT_NAME);
                adminCompany.setActive(true);
                adminCompany.setCreatedBy(createdBy);
                adminCompany.setFullName(ADMIN_COMPANY_FULL_NAME);
                adminCompany.setServices(adminCompanyServices);
                adminCompany = companyService.create(adminCompany);
            } else {
                adminCompany = companyService.getByFullName(ADMIN_COMPANY_FULL_NAME);
            }
            cachedCompanies.put(ADMIN_COMPANY_SHORT_NAME, adminCompany);
        }
        return adminCompany;
    }

    private User getTestUser() {
        return userService.getByEmail(cachedUsers.keySet().iterator().next());
    }

    private Company getTestCompany() {
        return companyService.getAll().get(0);
    }

    /**
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private void createUsers() throws NoSuchAlgorithmException, InvalidKeySpecException {
        // check for non repeated users
        final String[] users = {"user-one@example.com", "user-two@example.com", "user-three@example.com"};
        for (String email : users) {
            if (isEmailValid(email)) {
                try {
                    final String name = email.split("@")[0];
                    createAndCacheUser(name, name, email, name);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
    }

    private User createAndCacheUser(String firstName, String lastName, String email, String password)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        // use string before @ sign as nick name
        user.setNickName(email.split("@")[0]);
        user.setEmail(email);

        byte[] salt = AuthenticationService.generateSalt();
        user.setPwSalt(salt);
        user.setPassword(AuthenticationService.getEncryptedPassword(password, salt));

        // set registration information
        user.setRegisteredThroughEFF(true);
        user.setRegistrationStatus(RegistrationStatus.CONFIRMED);
        user.setRegistrationDate(new Date());

        user = userService.create(user);
        cachedUsers.put(email, user);
        return user;
    }

    private void createAdminEmployment() {
        final User admin = cachedUsers.get(ADMIN_EMAIL);
        final Company adminCompany = cachedCompanies.get(ADMIN_COMPANY_SHORT_NAME);
        if (admin != null && adminCompany != null) {
            createSuperAdminEmployment(admin, adminCompany);
        }
    }

    private void createEmployments() {
        for (User user : cachedUsers.values()) {
            if (user != null && isEmailValid(user.getEmail())) {
                // in SOC all users are MANAGERS for now...
                createManagerEmployment(user, cachedCompanies.get(ADMIN_COMPANY_SHORT_NAME));
            }
        }
    }

    private Employment createSuperAdminEmployment(User user, Company company) {
        Employment employment = new Employment();
        if (isEmailValid(user.getEmail())) {
            employment = createEmployment(user, company, UserRole.SUPER_ADMIN);
        }
        return employment;
    }

    private Employment createManagerEmployment(User user, Company company) {
        return createEmployment(user, company, UserRole.MANAGER);
    }

    private Employment createEmployment(User user, Company company, UserRole role) {
        Employment employment = new Employment(user, company);
        employment.setRole(role);
        employment = employmentService.create(employment);
        user.getEmployments().add(employment);
        user = userService.update(user);
        return employment;
    }
}

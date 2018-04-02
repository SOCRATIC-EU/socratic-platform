/**
 *
 */
package de.atb.socratic.service.user;

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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.SingularAttribute;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Campaign_;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.Employment_;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.User_;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.model.scope.ScopeType;
import de.atb.socratic.model.scope.StaffScope;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.notification.RegistrationMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.provider.UrlProvider;
import org.hibernate.search.query.dsl.PhraseContext;
import org.hibernate.search.query.dsl.PhraseMatchingContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.WildcardContext;

/**
 * @author ATB
 */
@Stateless
public class UserService extends AbstractService<User> {

    /**
     *
     */
    private static final long serialVersionUID = -6586267160037257995L;

    @Inject
    CompanyService companyService;

    @EJB
    CampaignService campaignService;

    @EJB
    IdeaService ideaService;

    @EJB
    ActionService actionService;

    @EJB
    ActionIterationService actionIterationService;

    @EJB
    ActionBusinessModelService actionBusinessModelService;

    @EJB
    ActivityService activityService;

    @Inject
    EmploymentService employmentService;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    RegistrationMailService registrationMailService;

    @Inject
    UrlProvider urlProvider;

    @Inject
    ParticipateNotificationService participateNotifier;

    public UserService() {
        super(User.class);
    }

    public User updateWithEmployments(final User user, final Set<Company> selectedCompanies) {
        updateEmployments(user, selectedCompanies);
        return super.update(user);
    }

    /**
     *
     */
    private void updateEmployments(final User user, final Set<Company> selectedCompanies) {
        // first check if company has been removed
        Set<Employment> employments = user.getEmployments();
        Set<Employment> employmentsToRemove = new HashSet<>();
        for (Employment employment : employments) {
            if (!selectedCompanies.contains(employment.getCompany())) {
                employmentsToRemove.add(employment);
            }
        }
        if (!employmentsToRemove.isEmpty()) {
            user.getEmployments().removeAll(employmentsToRemove);
            employmentService.delete(employmentsToRemove);
        }
        // then add any new companies
        Set<Company> employmentCompanies = user.getEmploymentCompanies();
        if (!selectedCompanies.isEmpty()) {
            for (Company company : selectedCompanies) {
                if (!employmentCompanies.contains(company)) {
                    addEmployment(user, company);
                }
            }
        }
    }

    /**
     * @param company
     */
    private void addEmployment(final User user, final Company company) {
        Employment employment = new Employment();
        employment.setUser(user);
        employment.setCompany(company);
        employment = employmentService.create(employment);
        user.getEmployments().add(employment);
    }

    public List<User> getAllFromToByAscendingDueDate(int first, int count) {
        logger.infof("loading users ordered by ascending registration date ...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        criteria.orderBy(cb.asc(root.get(User_.registrationDate)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param first
     * @param count
     * @return
     */
    @SuppressWarnings("unused")
    public List<User> getAllFromTo(int first, int count) {
        logger.infof("loading %d users starting from %d ...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public List<User> getAllRegisteredUsers() {
        logger.info("loading all registered users ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.equal(root.get(User_.registrationStatus), RegistrationStatus.CONFIRMED));
        return em.createQuery(criteria).getResultList();
    }

    public List<String> getEmailsOfRegisteredUserByEmails(final Set<String> emails) {
        logger.infof("counting REGISTERED users with email %s ...", emails);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteria = cb.createQuery(String.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.and(cb.equal(root.get(User_.registrationStatus), RegistrationStatus.CONFIRMED), root.get(User_.email)
                .in(emails)));
        criteria.select(root.get(User_.email)).distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    public User getRegisteredUserByEmail(String email) {
        logger.info("finding registered users via email...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.and(cb.equal(root.get(User_.registrationStatus), RegistrationStatus.CONFIRMED),
                cb.equal(root.get(User_.email), email)));
        List<User> resultList = em.createQuery(criteria).setMaxResults(1).getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
    }

    public Long countAllRegisteredUsers() {
        logger.info("loading all registered users ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.equal(root.get(User_.registrationStatus), RegistrationStatus.CONFIRMED));
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param email
     * @return
     */
    public Long countByEmail(String email) {
        return countByStringAttribute(email, User_.email);
    }

    /**
     * This method checks if given nickName of the user is unique.
     *
     * @param user
     * @return
     */
    public User findUniqueNickNameForFBLIUsers(User user) {
        int iterationCount = 0;
        String nickName = user.getNickName();
        // loop until unique nickName is found
        while (true) {
            // check if user already exists
            Long count = countByNickName(nickName);
            if (count > 0) {
                nickName += iterationCount;
                iterationCount++;
            } else {
                break;
            }
        }
        user.setNickName(nickName);
        return user;
    }

    /**
     * @param nickName
     * @return
     */
    public Long countByNickName(String nickName) {
        return countByStringAttribute(nickName, User_.nickName);
    }

    public Long countByLDAPPrincipal(String login) {
        return countByStringAttribute(login, User_.ldapLogin);
    }

    public Long countByLinkedInId(String id) {
        return countByStringAttribute(id, User_.linkedInId);
    }

    public Long countByFacebookId(String id) {
        return countByStringAttribute(id, User_.facebookId);
    }

    private Long countByStringAttribute(String attributeValue, SingularAttribute<User, String> attribute) {
        logger.infof("counting users with %s %s ...", attribute.getName(), attributeValue);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.equal(root.get(attribute), attributeValue));
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param email
     * @return
     */
    public User getByEmail(String email) {
        return getByStringAttribute(email, User_.email);
    }

    public User getByLDAPLogin(String login) {
        return getByStringAttribute(login, User_.ldapLogin);
    }

    private User getByStringAttribute(String attributeValue, SingularAttribute<User, String> attribute) {
        logger.infof("loading user with %s %s ...", attribute.getName(), attributeValue);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.equal(root.get(attribute), attributeValue));
        User user = null;
        try {
            user = em.createQuery(criteria).getSingleResult();
        } catch (NoResultException e) {
            logger.error("No such user is found with given attribute");
            return null;
        }
        return user;
    }

    public User getByMailNullIfNotFound(String email) {
        return getUserByStringAttributeNullIfNotFound(email, User_.email);
    }

    public User getByNickNameNullIfNotFound(String nickName) {
        return getUserByStringAttributeNullIfNotFound(nickName, User_.nickName);
    }

    public User getByLDAPLoginNullIfNotFound(String login) {
        return getUserByStringAttributeNullIfNotFound(login, User_.ldapLogin);
    }

    public User getByLinkedInIdNullIfNotFound(String id) {
        return getUserByStringAttributeNullIfNotFound(id, User_.linkedInId);
    }

    public User getByFacebookIdNullIfNotFound(String id) {
        return getUserByStringAttributeNullIfNotFound(id, User_.facebookId);
    }

    private User getUserByStringAttributeNullIfNotFound(String attributeValue, SingularAttribute<User, String> attribute) {
        logger.infof("loading user with %s %s ...", attribute.getName(), attributeValue);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.equal(root.get(attribute), attributeValue));
        List<User> resultList = em.createQuery(criteria).setMaxResults(1).getResultList();
        if (resultList.size() >= 1) {
            return resultList.get(0);
        } else {
            return null;
        }
    }

    /**
     * this method checks if there is already nickName exists with other users. It excludes given loggeedIn user. In case of
     * register page it does not consider loggedInuser.
     *
     * @param loggedInUser
     * @return
     */
    public boolean isOtherUsersWithNickNameExists(User loggedInUser, String nickName) {
        logger.infof("checking users with given nickName %s excluding given loggedInUser %s ...", nickName, loggedInUser);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        if (loggedInUser != null) {
            criteria.where(cb.and(cb.equal(root.get(User_.nickName), nickName), cb.notEqual(root.get(User_.id), loggedInUser.getId())));
        } else {
            criteria.where(cb.equal(root.get(User_.nickName), nickName));
        }
        return em.createQuery(criteria).getResultList().size() >= 1 ? true : false;
    }

    /**
     * @param email
     * @param registrationStatus
     * @return
     */
    public User getByEmailAndStatus(String email, RegistrationStatus registrationStatus) {
        logger.infof("loading user with email %s and registrationStatus %s ...", email, registrationStatus);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.and(cb.equal(root.get(User_.email), email),
                cb.equal(root.get(User_.registrationStatus), registrationStatus)));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param registrationToken
     * @return
     */
    public User getByRegistrationTokenPending(String registrationToken) {
        logger.infof("loading user with registrationToken %s ...", registrationToken);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.and(cb.equal(root.get(User_.registrationToken), registrationToken),
                cb.equal(root.get(User_.registrationStatus), RegistrationStatus.PENDING)));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param resetPWRequestToken
     * @return
     */
    public User getByResetPWRequestToken(String resetPWRequestToken) {
        logger.infof("loading user with resetPWRequestToken %s ...", resetPWRequestToken);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        criteria.where(cb.equal(root.get(User_.resetPWRequestToken), resetPWRequestToken));
        return em.createQuery(criteria).getSingleResult();
    }

    public Iterator<? extends Company> getAllUserCompanies(User user) {

        // Set<Company> employmentCompanies = user.getEmploymentCompanies();
        // System.out.println("company " + employmentCompanies.toString());
        // return employmentCompanies.iterator();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, Company> join = root.join(Employment_.company);
        criteria.select(join);
        criteria.where(cb.equal(root.get(Employment_.user), user), cb.equal(root.get(Employment_.deleted), false));
        criteria.distinct(true);
        List<Company> companies = em.createQuery(criteria).getResultList();
        return companies.iterator();
    }

    public long getCountAllUserCompanies(User user) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, Company> join = root.join(Employment_.company);
        criteria.where(cb.equal(root.get(Employment_.user), user), cb.equal(root.get(Employment_.deleted), false));
        criteria.select(cb.countDistinct(join));
        Long companies = em.createQuery(criteria).getSingleResult();
        return companies;
    }

    public List<Company> getAllUserCompaniesForDropDown(User user) {
        Set<Employment> allUseremployments = user.getEmployments();
        List<Employment> mainList = new ArrayList<Employment>(allUseremployments);
        List<Company> companyList = new ArrayList<Company>();

        for (Employment p : mainList) {
            Company employmentCompany = new Company();
            long count = companyService.getEmploymentCompanyCount(p.getId());
            if (count != 0) {
                employmentCompany = companyService.getEmploymentCompany(p.getId());
                companyList.add(employmentCompany);
            }
        }
        ;
        return companyList;
    }

    public boolean userInScope(User user, Campaign campaign) {
        if (user.hasAnyRoles(UserRole.SUPER_ADMIN)) {
            return true;
        } else if (campaign != null && campaign.getScope() != null) {
            return userInScope(user, campaign.getScope());
        }
        return false;
    }

    protected boolean userInCollection(Collection<User> users, User user) {
        return users != null && !users.isEmpty() && users.contains(user);
    }

    public boolean userInScope(User user, Scope scope) {
        // user is admin, allow everything
        if (user.hasAnyRoles(UserRole.SUPER_ADMIN)) {
            return true;
        }
        ScopeType type = scope.getScopeType();
        if (type == ScopeType.OPEN) {
            // scope is open, user is allowed to see
            return true;
        } else if (type == ScopeType.STAFF_ALL || type == ScopeType.STAFF_DEPARTMENTS || type == ScopeType.STAFF_USERS) {
            // check if user belongs to staff
            return userInStaffScope(user, (StaffScope) scope);
        }
        return false;
    }

    private boolean userInStaffScope(User user, StaffScope scope) {
        // user is admin, allow everything
        if (user.hasAnyRoles(UserRole.SUPER_ADMIN)) {
            return true;
        }

        // user is manager of the company of the scope, so allow to see
        if (user.hasAnyRoles(UserRole.MANAGER, UserRole.ADMIN) && scope.getCompany() != null
                && scope.getCompany().equals(user.getCurrentCompany())) {
            return true;
        }

        if (scope.getScopeType() == ScopeType.STAFF_ALL) {
            // see if the current company of the user is company of scope
            return user.getCurrentCompany().equals(scope.getCompany());
        } else if (scope.getScopeType() == ScopeType.STAFF_USERS) {
            // user is contained in the list of scoped users, allow
            return scope.getUsers().contains(user);
        } else {
            // check if users current department is set, if so it has to be contained in the scope's department list
            return user.getCurrentDepartment() != null && scope.getDepartments().contains(user.getCurrentDepartment());
        }
    }

    public List<User> getFiltered(Collection<User> searchUser, Collection<Company> searchCompany, int offset, int count) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        Join<User, Employment> empl = root.join(User_.employments, JoinType.LEFT);
        Join<Employment, Company> comp = empl.join(Employment_.company, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();
        if (searchUser != null && !searchUser.isEmpty()) {
            predicates.add(root.in(searchUser));
        }
        if (searchCompany != null && !searchCompany.isEmpty()) {
            predicates.add(comp.in(searchCompany));
        }
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        criteria.distinct(true);
        criteria.select(root);
        return em.createQuery(criteria).setFirstResult(offset).setMaxResults(count).getResultList();
    }

    public long countFiltered(Collection<User> searchUser, Collection<Company> searchCompany) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<User> root = criteria.from(User.class);
        Join<User, Employment> empl = root.join(User_.employments, JoinType.LEFT);
        Join<Employment, Company> comp = empl.join(Employment_.company, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (searchUser != null && !searchUser.isEmpty()) {
            predicates.add(root.in(searchUser));
        }
        if (searchCompany != null && !searchCompany.isEmpty()) {
            predicates.add(comp.in(searchCompany));
        }
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    public void notifyAboutSuccessfullRegistrationOfInvitedContact(User usr, String email) {
        participateNotifier.addParticipationNotification(email, usr, NotificationType.PLATFORM_PARTICIPATE);
    }

    public List<User> getForPIPFNNotifications(int offset, int count, User user) {
        StringBuilder jpql = new StringBuilder(
                "SELECT c FROM Notification n LEFT JOIN n.user c WHERE n.user=:user AND DTYPE=:dType AND creationDate > :lastWeek");
        TypedQuery<User> q = em.createQuery(jpql.toString(), User.class);
        q.setParameter("user", user);
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        q.setParameter("lastWeek", cal.getTime());
        q.setParameter("dType", "PIPFN");
        List<User> ret = q.setFirstResult(offset).setMaxResults(count).getResultList();
        return ret;
    }

    /**
     * @param challenge
     * @param followerId
     * @return
     */
    public User addChallengeToFollowedChallegesList(Campaign challenge, Long followerId) {
        User follower = getById(followerId);
        if (!follower.getFollowedCampaigns().contains(challenge)) {
            follower.getFollowedCampaigns().add(challenge);
            follower = update(follower);
        }
        logger.infof("follower %s is added to campaign %s ...", follower, challenge);
        return follower;
    }

    /**
     * @param challenge
     * @param followerId
     * @return
     */
    public User removeChallengeFromFollowedChallegesList(Campaign challenge, Long followerId) {
        User follower = getById(followerId);
        if (follower.getFollowedCampaigns().contains(challenge)) {
            follower.getFollowedCampaigns().remove(challenge);
            follower = update(follower);
        }
        logger.infof("follower %s is removed from campaign %s ...", follower, challenge);
        return follower;
    }

    /**
     * @param challenge
     * @param followerId
     * @return
     */
    public boolean isUserFollowsGivenChallenge(Campaign challenge, Long followerId) {
        User follower = getById(followerId);
        return follower.getFollowedCampaigns().contains(challenge);
    }

    /**
     * @param challenge
     * @return
     */
    public int countAllUsersByGivenFollowedChallenge(Campaign challenge) {
        logger.infof("counting all users following given challenge %s ...,", challenge);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, Campaign> join = root.join(User_.followedCampaigns);
        query.where(join.in(challenge));
        List<User> resultList = em.createQuery(query).getResultList();
        return resultList.size();
    }

    /**
     * @param challenge
     * @return
     */
    public List<User> getAllUsersByGivenFollowedChallenge(Campaign challenge, int offset, int count) {
        logger.infof("loading all users following given challenge %s ...,", challenge);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, Campaign> join = root.join(User_.followedCampaigns);
        query.where(join.in(challenge));
        query.distinct(true);
        if (offset != Integer.MAX_VALUE && count != Integer.MAX_VALUE) {
            return em.createQuery(query).setFirstResult(offset).setMaxResults(count).getResultList();
        }
        return em.createQuery(query).getResultList();
    }

    /**
     * @param idea
     * @param followerId
     * @return
     */
    public User addIdeaToFollowedIdeasList(Idea idea, Long followerId) {
        User follower = getById(followerId);
        if (!follower.getFollowedIdeas().contains(idea)) {
            follower.getFollowedIdeas().add(idea);
            follower = update(follower);
        }
        logger.infof("follower %s is added to idea %s ...", follower, idea);
        return follower;
    }

    /**
     * @param idea
     * @param followerId
     * @return
     */
    public User removeIdeaFromFollowedIdeasList(Idea idea, Long followerId) {
        User follower = getById(followerId);
        if (follower.getFollowedIdeas().contains(idea)) {
            follower.getFollowedIdeas().remove(idea);
            follower = update(follower);
        }
        logger.infof("follower %s is removed from idea %s ...", follower, idea);
        return follower;
    }

    /**
     * @param idea
     * @param followerId
     * @return
     */
    public boolean isUserFollowsGivenIdea(Idea idea, Long followerId) {
        User follower = getById(followerId);
        return follower.getFollowedIdeas().contains(idea);
    }

    /**
     * @param idea
     * @return
     */
    public int countAllUsersByGivenFollowedIdea(Idea idea) {
        logger.infof("counting all users following given idea %s ...,", idea);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, Idea> join = root.join(User_.followedIdeas);
        query.where(join.in(idea));
        List<User> resultList = em.createQuery(query).getResultList();
        return resultList.size();
    }

    /**
     * @param idea
     * @return
     */
    public List<User> getAllUsersByGivenFollowedIdea(Idea idea, int offset, int count) {
        logger.infof("loading all users following given idea %s ...,", idea);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, Idea> join = root.join(User_.followedIdeas);
        query.where(join.in(idea));
        if (offset != Integer.MAX_VALUE && count != Integer.MAX_VALUE) {
            return em.createQuery(query).setFirstResult(offset).setMaxResults(count).getResultList();
        }
        return em.createQuery(query).getResultList();
    }

    /**
     * @param user
     * @return total no of challenges, ideas and action user has posted or created
     */
    public long getTotalNoOfLeadEntitiesByUser(User user) {
        long returnVal = 0;
        returnVal += campaignService.countCampaignsForUser(user);

        returnVal += ideaService.countIdeasForUser(user);

        returnVal += actionService.countActionsForUser(user);

        return returnVal;
    }

    /**
     * @param user
     * @return total no of LikesReceived by challenge, idea, action, action iteration and business model posted or created by
     * given user
     */
    public long getTotalNoOfLikesReceivedByUser(User user) {
        long returnVal = 0;
        returnVal += campaignService.countTotalLikesForCampaignCreatedByUser(user);

        returnVal += ideaService.countTotalLikesForIdeaCreatedByUser(user);

        returnVal += actionService.countTotalLikesForActionCreatedByUser(user);

        returnVal += actionIterationService.countTotalLikesForActionIterationCreatedByUser(user);

        returnVal += actionBusinessModelService.countTotalLikesForActionBusinessModelCreatedByUser(user);

        return returnVal;
    }

    /**
     * @param user
     * @return total no of LikesGiven by user to challenge, ideas, action, action iteration and business model
     */
    public long getTotalNoOfLikesGivenByUser(User user) {
        long returnVal = 0;
        returnVal += campaignService.countTotalLikesGivenByUserForCampaign(user);

        returnVal += ideaService.countTotalLikesGivenByUserForAllIdeas(user);

        returnVal += actionService.countTotalLikesGivenByUserForAllActions(user);

        returnVal += actionIterationService.countTotalLikesGivenByUserForAllActionIterations(user);

        returnVal += actionBusinessModelService.countTotalLikesGivenByUserForAllActionBusinessModels(user);

        return returnVal;
    }

    /**
     * @param action
     * @param followerId
     * @return
     */
    public User addActionToFollowedActionsList(Action action, Long followerId) {
        User follower = getById(followerId);
        if (!follower.getFollowedActions().contains(action)) {
            follower.getFollowedActions().add(action);
            follower = update(follower);
        }
        logger.infof("follower %s is added to action %s ...", follower, action);
        return follower;
    }

    /**
     * @param action
     * @param followerId
     * @return
     */
    public User removeActionFromFollowedActionsList(Action action, Long followerId) {
        User follower = getById(followerId);
        if (follower.getFollowedActions().contains(action)) {
            follower.getFollowedActions().remove(action);
            follower = update(follower);
        }
        logger.infof("follower %s is removed from action %s ...", follower, action);
        return follower;
    }

    /**
     * @param action
     * @param followerId
     * @return
     */
    public boolean isUserFollowsGivenAction(Action action, Long followerId) {
        User follower = getById(followerId);
        return follower.getFollowedActions().contains(action);
    }

    /**
     * @param action
     * @return
     */
    public int countAllUsersByGivenFollowedAction(Action action) {
        logger.infof("counting all users following given action %s ...,", action);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Join<User, Action> join = root.join(User_.followedActions);
        query.where(join.in(action));
        List<User> resultList = em.createQuery(query).getResultList();
        return resultList.size();
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersByFollowingAllChallenges() {
        logger.infof("loading all followers following all challenges...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Expression<List<Campaign>> followedCampaigns = root.get(User_.followedCampaigns);
        Predicate predicate = cb.isNotEmpty(followedCampaigns);
        query.where(cb.and(predicate));
        return em.createQuery(query).getResultList();
    }

    /**
     * @param
     * @return
     */
    public List<User> getChallengeFollowersWithInnovationStatusOrderedByAscNickName(InnovationStatus innovationStatus,
                                                                                    Collection<Campaign> challenges) {
        logger.infof("loading all followers following all challenges with given innovation status %d ...", innovationStatus);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Expression<List<Campaign>> followedCampaigns = root.get(User_.followedCampaigns);
        Join<User, Campaign> joinCampaign = root.join(User_.followedCampaigns);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.isNotEmpty(followedCampaigns));

        if (challenges != null && !challenges.isEmpty()) {
            predicates.add(joinCampaign.in(challenges));
        }

        if (innovationStatus != null) {
            predicates.add(cb.equal(joinCampaign.get(Campaign_.innovationStatus), innovationStatus));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        query.orderBy(cb.asc(root.get(User_.nickName)));
        query.distinct(true);
        return em.createQuery(query).getResultList();
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersByFollowingAllIdeas() {
        logger.infof("loading all followers following all ideas...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Expression<List<Idea>> followedIdeas = root.get(User_.followedIdeas);
        Predicate predicate = cb.isNotEmpty(followedIdeas);
        query.where(cb.and(predicate));
        return em.createQuery(query).getResultList();
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersByFollowingGivenIdeas(Collection<Idea> ideas) {
        logger.infof("loading all followers following given ideas...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Expression<List<Idea>> followedIdeas = root.get(User_.followedIdeas);
        Join<User, Idea> join = root.join(User_.followedIdeas);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.isNotEmpty(followedIdeas));
        if (ideas != null && !ideas.isEmpty()) {
            predicates.add(join.in(ideas));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        query.orderBy(cb.asc(root.get(User_.nickName)));
        return em.createQuery(query).getResultList();
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersByFollowingGivenActions(Collection<Action> actions) {
        logger.infof("loading all followers following given actions...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Expression<List<Action>> followedActions = root.get(User_.followedActions);
        Join<User, Action> join = root.join(User_.followedActions);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.isNotEmpty(followedActions));
        if (actions != null && !actions.isEmpty()) {
            predicates.add(join.in(actions));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        query.orderBy(cb.asc(root.get(User_.nickName)));
        return em.createQuery(query).getResultList();
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersByFollowingAllActions() {
        logger.infof("loading all followers following all actions...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        Expression<List<Action>> followedActions = root.get(User_.followedActions);
        Predicate predicate = cb.isNotEmpty(followedActions);
        query.where(cb.and(predicate));
        return em.createQuery(query).getResultList();
    }

    /**
     * @param
     * @return
     */
    public int countAllUsersFollowingAllProcesses(Date startDate, Date endDate) {
        logger.infof("counting all followers which are registered between %s and %s following all processes...", startDate,
                endDate);
        List<User> challengeFollowers = getAllUsersByFollowingAllChallenges();
        List<User> ideaFollowers = getAllUsersByFollowingAllIdeas();
        List<User> actionFollowers = getAllUsersByFollowingAllActions();

        List<User> totalUsers = new LinkedList<>();
        if (!challengeFollowers.isEmpty() && challengeFollowers != null) {
            totalUsers.addAll(challengeFollowers);
        }
        if (!ideaFollowers.isEmpty() && ideaFollowers != null) {
            totalUsers.addAll(ideaFollowers);
        }

        if (!actionFollowers.isEmpty() && actionFollowers != null) {
            totalUsers.addAll(actionFollowers);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // check for all processes
        if (totalUsers != null && !totalUsers.isEmpty()) {
            predicates.add(root.in(totalUsers));
            // check for dates
            if (startDate != null && endDate != null) {
                predicates.add((cb.between(root.get(User_.registrationDate), startDate, endDate)));
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            }

            query.distinct(true);
            return em.createQuery(query).getResultList().size();
        }

        return 0;
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersFollowingAllProcesses(String first, String count, Date startDate, Date endDate) {
        logger.infof("loading %s followers starting from %s which are registered between %s and %s following all processes...",
                count, first, startDate, endDate);
        List<User> challengeFollowers = getAllUsersByFollowingAllChallenges();
        List<User> ideaFollowers = getAllUsersByFollowingAllIdeas();
        List<User> actionFollowers = getAllUsersByFollowingAllActions();

        List<User> totalUsers = new LinkedList<>();
        if (!challengeFollowers.isEmpty() && challengeFollowers != null) {
            totalUsers.addAll(challengeFollowers);
        }
        if (!ideaFollowers.isEmpty() && ideaFollowers != null) {
            totalUsers.addAll(ideaFollowers);
        }

        if (!actionFollowers.isEmpty() && actionFollowers != null) {
            totalUsers.addAll(actionFollowers);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // check for all processes
        if (totalUsers != null && !totalUsers.isEmpty()) {
            predicates.add(root.in(totalUsers));
            // check for dates
            if (startDate != null && endDate != null) {
                predicates.add((cb.between(root.get(User_.registrationDate), startDate, endDate)));
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            }

            query.distinct(true);
            if (first != null && count != null) {
                return em.createQuery(query).setFirstResult(Integer.parseInt(first)).setMaxResults(Integer.parseInt(count))
                        .getResultList();
            } else {
                return em.createQuery(query).getResultList();
            }

        }

        return new LinkedList<>();

    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersFollowingAllProcesses() {
        logger.infof("loading all followers following all processes...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        List<Predicate> predicates = new ArrayList<Predicate>();

        // check for campaigns
        Expression<List<Campaign>> followedCampaigns = root.get(User_.followedCampaigns);
        predicates.add(cb.isNotEmpty(followedCampaigns));

        // check for ideas
        Expression<List<Idea>> followedIdeas = root.get(User_.followedIdeas);
        predicates.add(cb.isNotEmpty(followedIdeas));

        // check for actions
        Expression<List<Action>> followedActions = root.get(User_.followedActions);
        predicates.add(cb.isNotEmpty(followedActions));

        if (!predicates.isEmpty()) {
            query.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
        }

        query.distinct(true);
        return em.createQuery(query).getResultList();
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersLeadingAllProcesses(String first, String count, Date startDate, Date endDate) {
        logger.infof("loading %s leaders starting from %s, registered between %s and %s, leading all processes...", count,
                first, startDate, endDate);
        List<User> challengeLeaders = campaignService.getAllChallengeLeadersByAscNickName();
        List<User> ideaLeaders = ideaService.getAllIdeaLeadersByAscNickName();
        List<User> actionLeaders = actionService.getAllActionLeadersByAscNickName();

        List<User> totalUsers = new LinkedList<>();
        if (!challengeLeaders.isEmpty() && challengeLeaders != null) {
            totalUsers.addAll(challengeLeaders);
        }
        if (!ideaLeaders.isEmpty() && ideaLeaders != null) {
            totalUsers.addAll(ideaLeaders);
        }

        if (!actionLeaders.isEmpty() && actionLeaders != null) {
            totalUsers.addAll(actionLeaders);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // check for all processes
        if (totalUsers != null && !totalUsers.isEmpty()) {
            predicates.add(root.in(totalUsers));

            // check for dates
            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get(User_.registrationDate), startDate, endDate));
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

            }

            query.distinct(true);
            if (first != null && count != null) {
                return em.createQuery(query).setFirstResult(Integer.parseInt(first)).setMaxResults(Integer.parseInt(count)).getResultList();
            } else {
                return em.createQuery(query).getResultList();
            }

        }
        return new LinkedList<>();

    }

    /**
     * @param
     * @return
     */
    public int countAllUsersLeadingAllProcesses(Date startDate, Date endDate) {
        logger.infof("counting all leaders registered between %s and %s leading all processes...", startDate, endDate);

        List<User> challengeLeaders = campaignService.getAllChallengeLeadersByAscNickName();
        List<User> ideaLeaders = ideaService.getAllIdeaLeadersByAscNickName();
        List<User> actionLeaders = actionService.getAllActionLeadersByAscNickName();

        List<User> totalUsers = new LinkedList<>();
        if (!challengeLeaders.isEmpty() && challengeLeaders != null) {
            totalUsers.addAll(challengeLeaders);
        }
        if (!ideaLeaders.isEmpty() && ideaLeaders != null) {
            totalUsers.addAll(ideaLeaders);
        }

        if (!actionLeaders.isEmpty() && actionLeaders != null) {
            totalUsers.addAll(actionLeaders);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // check for all processes
        if (totalUsers != null && !totalUsers.isEmpty()) {
            predicates.add(root.in(totalUsers));

            // check for dates
            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get(User_.registrationDate), startDate, endDate));
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            }

            query.distinct(true);
            return em.createQuery(query).getResultList().size();
        }

        return 0;
    }

    /**
     * @param
     * @return
     */
    public List<User> getAllUsersLeadingAllProcesses() {
        logger.infof("loading all leaders leading all processes...");
        List<User> challengeLeaders = campaignService.getAllChallengeLeadersByAscNickName();
        List<User> ideaLeaders = ideaService.getAllIdeaLeadersByAscNickName();
        List<User> actionLeaders = actionService.getAllActionLeadersByAscNickName();

        List<User> totalUsers = new LinkedList<>();
        if (!challengeLeaders.isEmpty() && challengeLeaders != null) {
            totalUsers.addAll(challengeLeaders);
        }
        if (!ideaLeaders.isEmpty() && ideaLeaders != null) {
            totalUsers.addAll(ideaLeaders);
        }

        if (!actionLeaders.isEmpty() && actionLeaders != null) {
            totalUsers.addAll(actionLeaders);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // check for all processes
        if (totalUsers != null && !totalUsers.isEmpty()) {
            predicates.add(root.in(totalUsers));

            if (!predicates.isEmpty()) {
                query.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
            }

            query.distinct(true);
            return em.createQuery(query).getResultList();
        }
        return new LinkedList<>();

    }

    /**
     * This method will check for all leaders, followers and contributors and then selects users which are not one of them.
     *
     * @param
     * @return
     */
    public List<User> getAllUsersWithNoActionYetForAllProcesses(String first, String count, Date startDate, Date endDate) {
        logger.infof(
                "loading %s users starting from %s with no action yet which are registered between %s and %s for all processes...",
                count, first, startDate, endDate);

        List<User> allLeaders = getAllUsersLeadingAllProcesses();
        List<User> allContributors = activityService.getAllContributorsFromAllProcesses();
        List<User> allFollowers = getAllUsersFollowingAllProcesses();

        List<User> totalUsers = new LinkedList<>();
        if (!allLeaders.isEmpty() && allLeaders != null) {
            totalUsers.addAll(allLeaders);
        }
        if (!allContributors.isEmpty() && allContributors != null) {
            totalUsers.addAll(allContributors);
        }

        if (!allFollowers.isEmpty() && allFollowers != null) {
            totalUsers.addAll(allFollowers);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // check for all processes
        if (totalUsers != null && !totalUsers.isEmpty()) {
            predicates.add(cb.not(root.in(totalUsers)));

            // check for dates
            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get(User_.registrationDate), startDate, endDate));
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            }

            query.distinct(true);
            if (first != null && count != null) {
                // return users for pagination
                return em.createQuery(query).setFirstResult(Integer.parseInt(first)).setMaxResults(Integer.parseInt(count)).getResultList();
            } else {
                // return all users
                return em.createQuery(query).getResultList();
            }
        } else {
            return getAllUsersByRegisteredDate(first, count, startDate, endDate);
        }
    }

    /**
     * @param
     * @return
     */
    public int countAllUsersWithNoActionYetForAllProcesses(Date startDate, Date endDate) {
        logger.infof("counting all users with no action yet which are registered between %s and %s for all processes...",
                startDate, endDate);

        List<User> allLeaders = getAllUsersLeadingAllProcesses();
        List<User> allContributors = activityService.getAllContributorsFromAllProcesses();
        List<User> allFollowers = getAllUsersFollowingAllProcesses();

        List<User> totalUsers = new LinkedList<>();
        if (!allLeaders.isEmpty() && allLeaders != null) {
            totalUsers.addAll(allLeaders);
        }
        if (!allContributors.isEmpty() && allContributors != null) {
            totalUsers.addAll(allContributors);
        }

        if (!allFollowers.isEmpty() && allFollowers != null) {
            totalUsers.addAll(allFollowers);
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        // check for all processes
        if (totalUsers != null && !totalUsers.isEmpty()) {
            predicates.add(cb.not(root.in(totalUsers)));

            // check for dates
            if (startDate != null && endDate != null) {
                predicates.add(cb.between(root.get(User_.registrationDate), startDate, endDate));
            }

            if (!predicates.isEmpty()) {
                query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            }

            query.distinct(true);
            return em.createQuery(query).getResultList().size();
        } else {
            return (int) countAllUsersByRegisteredDate(startDate, endDate);
        }
    }

    public List<User> getAllUsersByRegisteredDate(String first, String count, Date startDate, Date endDate) {
        logger.infof("get %s users starting from %s registered between %s and %s...", count, first, startDate, endDate);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);
        // check for dates
        if (startDate != null && endDate != null) {
            criteria.where(cb.between(root.get(User_.registrationDate), startDate, endDate));
        }
        criteria.distinct(true);
        if (first != null && count != null) {
            return em.createQuery(criteria).setFirstResult(Integer.parseInt(first)).setMaxResults(Integer.parseInt(count)).getResultList();
        } else {
            return em.createQuery(criteria).getResultList();
        }
    }

    public long countAllUsersByRegisteredDate(Date startDate, Date endDate) {
        logger.infof("counting all users registered between %s and %s...", startDate, endDate);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<User> root = criteria.from(User.class);
        // check for dates
        if (startDate != null && endDate != null) {
            criteria.where(cb.between(root.get(User_.registrationDate), startDate, endDate));
        }
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * this method will increase counter of noOfCampaignsLeads for given user
     *
     * @param user
     */
    public void increaseNoOfCampaignsLeads(final User user) {
        logger.infof("total no of campaigns leads is increased by one for given user with id %d ...", user.getId());
        user.setNoOfCampaignsLeads(user.getNoOfCampaignsLeads() + 1);
        update(user);
    }

    /**
     * this method will decrease counter of noOfCampaignsLeads for given user
     *
     * @param user
     */
    public void decreaseNoOfCampaignsLeads(final User user) {
        logger.infof("total no of campaigns leads is decreased by one for given user with id %d ...", user.getId());
        if (user.getNoOfCampaignsLeads() <= 1) {
            user.setNoOfCampaignsLeads(0);
        } else {
            user.setNoOfCampaignsLeads(user.getNoOfCampaignsLeads() + 1);
        }

        update(user);
    }

    /**
     * this method will increase counter of noOfIdeasLeads for given user
     *
     * @param user
     */
    public void increaseNoOfIdeasLeads(final User user) {
        logger.infof("total no of ideas leads is increased by one for given user with id %d ...", user.getId());
        user.setNoOfIdeasLeads(user.getNoOfIdeasLeads() + 1);
        update(user);
    }

    /**
     * this method will decrease counter of noOfIdeasLeads for given user
     * this method should be called when idea is deleted.
     *
     * @param user
     */
    public void decreaseNoOfIdeasLeads(final User user) {
        logger.infof("total no of ideas leads is decreased by one for given user with id %d ...", user.getId());
        if (user.getNoOfIdeasLeads() <= 1) {
            user.setNoOfIdeasLeads(0);
        } else {
            user.setNoOfIdeasLeads(user.getNoOfIdeasLeads() + 1);
        }

        update(user);
    }

    /**
     * this method will increase counter of noOfActionsLeads for given user
     *
     * @param user
     */
    public void increaseNoOfActionsLeads(final User user) {
        logger.infof("total no of actions leads is increased by one for given user with id %d ...", user.getId());
        user.setNoOfActionsLeads(user.getNoOfActionsLeads() + 1);
        update(user);
    }

    /**
     * this method will decrease counter of noOfActionsLeads for given user, when user deletes its own action
     * this method should be called when action is deleted.
     * Create action delete method first!
     *
     * @param user
     */
    public void decreaseNoOfActionsLeads(final User user) {
        logger.infof("total no of actions leads is decreased by one for given user with id %d ...", user.getId());
        if (user.getNoOfActionsLeads() <= 1) {
            user.setNoOfActionsLeads(0);
        } else {
            user.setNoOfActionsLeads(user.getNoOfActionsLeads() + 1);
        }

        update(user);
    }

    /**
     * this method will increase counter of CommentsPosts for given user
     *
     * @param user
     */
    public void increaseNoOfCommentsPosted(final User user) {
        logger.infof("total no of comments posted is increased by one for given user with id %d ...", user.getId());
        user.setNoOfCommentsPosts(user.getNoOfCommentsPosts() + 1);
        update(user);
    }

    /**
     * this method will decrease counter of CommentsPosts for given user, when user deletes its own comment
     * this method should be called when comment is deleted.
     *
     * @param user
     */
    public void decreaseNoOfCommentsPosted(final User user) {
        logger.infof("total no of comments posted is decreased by one for given user with id %d ...", user.getId());
        if (user.getNoOfCommentsPosts() <= 1) {
            user.setNoOfCommentsPosts(0);
        } else {
            user.setNoOfCommentsPosts(user.getNoOfCommentsPosts() + 1);
        }

        update(user);
    }

    /**
     * this method will increase counter of LikesReceived for given user
     *
     * @param user
     */
    public void increaseNoOfLikesReceived(final User user) {
        logger.infof("total no of likes received is increased by one for given user with id %d ...", user.getId());
        user.setNoOfLikesReceived(user.getNoOfLikesReceived() + 1);
        update(user);
    }

    /**
     * this method will decrease counter of LikesReceived for given user
     *
     * @param user
     */
    public void decreaseNoOfLikesReceived(final User user) {
        logger.infof("total no of likes received is decreased by one for given user with id %d ...", user.getId());
        if (user.getNoOfLikesReceived() <= 1) {
            user.setNoOfLikesReceived(0);
        } else {
            user.setNoOfLikesReceived(user.getNoOfLikesReceived() - 1);
        }

        update(user);
    }

    /**
     * this method will increase counter of LikesGiven for given user
     *
     * @param user
     */
    public void increaseNoOfLikesGiven(final User user) {
        logger.infof("total no of likes given is increased by one for given user with id %d ...", user.getId());
        user.setNoOfLikesGiven(user.getNoOfLikesGiven() + 1);
        update(user);
    }

    /**
     * this method will decrease counter of LikesGiven for given user
     *
     * @param user
     */
    public void decreaseNoOfLikesGiven(final User user) {
        logger.infof("total no of likes given is decreased by one for given user with id %d ...", user.getId());
        if (user.getNoOfLikesGiven() <= 1) {
            user.setNoOfLikesGiven(0);
        } else {
            user.setNoOfLikesGiven(user.getNoOfLikesGiven() - 1);
        }

        update(user);
    }

    /**
     * this method will return list of users which has given tag either as skill or interests
     *
     * @param tags
     * @return
     */
    public List<User> getAllUsersByTag(Set<Tag> tags) {
        logger.infof("loading users by given tags...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        List<Predicate> predicates = new ArrayList<Predicate>();

        for (Tag tag : tags) {
            predicates.add(cb.isMember(tag, root.get(User_.interests)));
            predicates.add(cb.isMember(tag, root.get(User_.skills)));
        }

        if (!predicates.isEmpty()) {
            criteria.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    @Override
    protected TermMatchingContext setFullTextSearchFields(WildcardContext wc) {
        return wc.onField("interests")
                .andField("skills")
                .andField("firstName")
                .andField("lastName")
                .andField("nickName")
                .andField("email")
                .andField("country")
                .andField("city");
    }

    @Override
    protected PhraseMatchingContext setFullTextSearchFields(PhraseContext wc) {
        return wc.onField("interests")
                .andField("skills")
                .andField("firstName")
                .andField("lastName")
                .andField("nickName")
                .andField("country")
                .andField("city");
    }
}

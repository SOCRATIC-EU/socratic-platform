/**
 *
 */
package de.atb.socratic.service.inception;

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

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.Campaign_;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Comment_;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.Employment_;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.Idea_;
import de.atb.socratic.model.InnovationObjective;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.Network;
import de.atb.socratic.model.Network_;
import de.atb.socratic.model.Reply;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.User_;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.scope.NetworkScope;
import de.atb.socratic.model.scope.NetworkScope_;
import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.model.scope.ScopeType;
import de.atb.socratic.model.scope.Scope_;
import de.atb.socratic.model.scope.StaffScope;
import de.atb.socratic.model.scope.StaffScope_;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.inception.CampaignsPage;
import de.atb.socratic.web.provider.UrlProvider;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.hibernate.search.query.dsl.PhraseContext;
import org.hibernate.search.query.dsl.PhraseMatchingContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.WildcardContext;
import org.jboss.seam.transaction.TransactionPropagation;
import org.jboss.seam.transaction.Transactional;
import org.joda.time.DateTime;

/**
 * @author ATB
 */
@Stateless
public class CampaignService extends AbstractService<Campaign> {

    private static final long serialVersionUID = 4634952642942688535L;

    @Inject
    UrlProvider urlProvider;

    @Inject
    Event<Campaign> campaignEventSrc;

    @Inject
    IdeaService ideaService;

    @Inject
    UserService userService;

    @Inject
    ScopeService scopeService;

    @Inject
    CommentService commentService;

    @Inject
    ParticipateNotificationService participateNotifier;

    public CampaignService() {
        super(Campaign.class);
    }

    public Campaign getFreeFormCampaign() {
        logger.infof("loading free form campaign ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        criteria.where(cb.and(cb.equal(root.get(Campaign_.campaignType),
                CampaignType.FREE_FORM), cb.not(root.get(Campaign_.deleted))));
        return em.createQuery(criteria).getSingleResult();
    }

    public boolean freeFormCampaignExists() {
        logger.infof("checking if free form campaign exists ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        criteria.where(cb.and(cb.equal(root.get(Campaign_.campaignType),
                CampaignType.FREE_FORM), cb.not(root.get(Campaign_.deleted))));
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult() > 0;
    }

    @Override
    protected TermMatchingContext setFullTextSearchFields(WildcardContext wc) {
        return wc.onField("name")
                .andField("socialChallenge")
                .andField("beneficiaries")
                .andField("potentialImpact")
                .andField("levelOfSupport")
                .andField("ideasProposed")
                .andField("referencesTitle")
                .andField("referencesDescription")
                .andField("referencesLink")
                .andField("description")
                .andField("createdBy.firstName")
                .andField("createdBy.lastName")
                .andField("createdBy.nickName")
                .andField("createdBy.email")                // does not work
                .andField("comments.commentText")
                .andField("comments.postedBy.firstName")
                .andField("comments.postedBy.lastName")
                .andField("comments.postedBy.email")        // does not work
                .andField("tags");
    }

    @Override
    protected PhraseMatchingContext setFullTextSearchFields(PhraseContext wc) {
        return wc.onField("name")
                .andField("socialChallenge")
                .andField("beneficiaries")
                .andField("potentialImpact")
                .andField("levelOfSupport")
                .andField("ideasProposed")
                .andField("referencesTitle")
                .andField("referencesDescription")
                .andField("referencesLink")
                .andField("description")
                .andField("createdBy.firstName")
                .andField("createdBy.lastName")
                .andField("createdBy.nickName")
                .andField("createdBy.email")                // does not work
                .andField("comments.commentText")
                .andField("comments.postedBy.firstName")
                .andField("comments.postedBy.lastName")
                .andField("comments.postedBy.email")        // does not work
                .andField("tags");
    }

    @Override
    protected List<Campaign> filterFullTextSearchResults(List<Campaign> results, final User loggedInUser) {
        if (loggedInUser == null) {
            return new ArrayList<Campaign>(0);
        } else {
            filter(results.iterator(), loggedInUser);
            return results;
        }
    }

    public Iterator<Campaign> filter(Iterator<Campaign> itemsToBeFiltered, User user) {
        while (itemsToBeFiltered.hasNext()) {
            Campaign c = itemsToBeFiltered.next();
            if (c.getDeleted()) {
                itemsToBeFiltered.remove();
            }
        }
        return itemsToBeFiltered;
    }

    public Long countCommentsForCampaign(Campaign campaign) {
        logger.infof("counting comments for campaign %s (id=%d) ...",
                campaign.getName(), campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        ListJoin<Campaign, Idea> idea = root.join(Campaign_.ideas, JoinType.LEFT);
        ListJoin<Idea, Comment> join = idea.join(Idea_.comments, JoinType.LEFT);
        criteria.where(cb.equal(root.get(Campaign_.id), campaign.getId()), cb.not(idea.get(Idea_.deleted)));
        criteria.select(cb.countDistinct(join));
        return em.createQuery(criteria).getSingleResult();
    }

    @Transactional(TransactionPropagation.REQUIRED)
    public float getIdeaDensity(Campaign campaign) {
        float density = 0f;
        Scope scope = campaign.getScope();
        if (scope != null) {
            Long ideasInCampaign = countIdeasForCampaign(campaign.getId());
            if (scope.getScopeType() != ScopeType.OPEN) {
                Long usersInScope = scopeService
                        .countAllExplicitUsersInScope(scope);
                density = getDensity(usersInScope, ideasInCampaign);
            } else {
                density = ideasInCampaign.floatValue();
            }
        }
        return density;
    }

    @Transactional(TransactionPropagation.REQUIRED)
    public synchronized float getCommentDensity(Campaign campaign) {
        float density = 0f;
        Scope scope = campaign.getScope();
        if (scope != null) {
            Long commentsInCampaign = countCommentsForCampaign(campaign);
            if (scope.getScopeType() != ScopeType.OPEN) {
                Long usersInScope = scopeService
                        .countAllExplicitUsersInScope(scope);
                density = getDensity(usersInScope, commentsInCampaign);
            } else {
                density = commentsInCampaign.floatValue();
            }
        }
        return density;
    }

    protected float getDensity(Long users, Long objects) {
        if (users != null && users > 0 && objects != null) {
            return (float) objects / (float) users;
        } else if (objects != null && objects == 0) {
            return 0f;
        }
        return Float.POSITIVE_INFINITY;
    }

    protected List<Predicate> getUserScopeCampaignPredicates(
            CriteriaBuilder cb, CriteriaQuery<?> query, Root<Campaign> root,
            Join<Campaign, Scope> scope, final User loggedInUser) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (loggedInUser != null && loggedInUser.hasAnyRoles(UserRole.SUPER_ADMIN)) {
            return predicates;
        }
        Predicate isOpenScope = cb.equal(scope.get(Scope_.scopeType),
                ScopeType.OPEN);
        Predicate restrictToStaffAll = getUserBasedCampaignRestrictionPredicatesForStaffAll(
                cb, query, root, scope, loggedInUser);
        Predicate restrictToStaffDepartments = getUserBasedCampaignRestrictionPredicatesForStaffDepartments(
                cb, query, root, scope, loggedInUser);
        Predicate restrictToStaffMembers = getUserBasedCampaignRestrictionPredicatesForStaffMembers(
                cb, query, root, scope, loggedInUser);
        Predicate restrictToNetworkAll = getUserBasedCampaignRestrictionPredicatesForNetworkAll(
                cb, query, root, scope, loggedInUser);
        Predicate restrictToNetworkMembers = getUserBasedCampaignRestrictionPredicatesForNetworkMembers(
                cb, query, root, scope, loggedInUser);
        predicates.add(cb.or(isOpenScope, restrictToStaffAll,
                restrictToStaffMembers, restrictToStaffDepartments,
                restrictToNetworkAll, restrictToNetworkMembers));
        return predicates;
    }

    protected Set<Long> getPrioritisationAccessibleCampaignsIds(InnovationObjective innovationObjective, final User loggedInUser) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        Join<Idea, Campaign> campaign = root.join(Idea_.campaign);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(campaign.get(Campaign_.deleted)));
        if (innovationObjective != null) {
            predicates.add(cb.equal(campaign.get(Campaign_.innovationObjective),
                    innovationObjective));
        }
        predicates.add(cb.equal(campaign.get(Campaign_.innovationStatus), InnovationStatus.PRIORITISATION));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        List<Idea> ideas = em.createQuery(criteria).getResultList();
        Set<Long> campaignIds = new HashSet<Long>();
        for (Idea idea : ideas) {
            Campaign c = idea.getCampaign();
            if (c != null && !campaignIds.contains(c.getId())) {
                campaignIds.add(c.getId());
            }
        }
        return campaignIds;
    }

    protected Predicate getUserBasedCampaignRestrictionPredicatesForStaffAll(
            CriteriaBuilder cb, CriteriaQuery<?> query, Root<Campaign> root,
            Join<Campaign, Scope> scope, final User loggedInUser) {
        Subquery<Employment> employments = query.subquery(Employment.class);
        Root<Employment> employment = employments.from(Employment.class);
        Join<Employment, User> users = employment.join(Employment_.user);
        Predicate userIsPartOfEmployment = cb.and(
                cb.equal(users, loggedInUser),
                cb.equal(employment.get(Employment_.company),
                        root.get(Campaign_.company)));
        Predicate userIsManager = cb.and(cb.equal(root.get(Campaign_.company),
                loggedInUser.getCurrentCompany()), cb.equal(
                employment.get(Employment_.role), UserRole.MANAGER));
        Subquery<Employment> where = employments.select(employment).where(
                cb.or(userIsPartOfEmployment, userIsManager));
        return cb.and(
                cb.equal(scope.get(Scope_.scopeType), ScopeType.STAFF_ALL),
                cb.exists(where));
    }

    protected Predicate getUserBasedCampaignRestrictionPredicatesForStaffMembers(
            CriteriaBuilder cb, CriteriaQuery<?> query, Root<Campaign> root,
            Join<Campaign, Scope> scope, final User loggedInUser) {
        // ensure to operate on staff scope rather than scope!
        Subquery<StaffScope> staffScopeQuery = query.subquery(StaffScope.class);
        Root<StaffScope> staffScope = staffScopeQuery.from(StaffScope.class);

        Subquery<Employment> employmentQuery = query.subquery(Employment.class);
        Root<Employment> employment = employmentQuery.from(Employment.class);
        Join<Employment, User> user = employment.join(Employment_.user);

        Predicate userIsPartOfEmployment = cb.equal(user, loggedInUser);
        Predicate usersBelongsToSelectedMembers = cb.isMember(
                loggedInUser, staffScope.get(StaffScope_.users));

        Predicate userIsManager = cb.and(cb.equal(root.get(Campaign_.company),
                loggedInUser.getCurrentCompany()), cb.equal(
                employment.get(Employment_.role), UserRole.MANAGER));
        Predicate userIsManagerOrPartOfScope = cb.and(userIsManager,
                userIsPartOfEmployment);

        employmentQuery.select(employment).where(userIsManagerOrPartOfScope);
        staffScopeQuery.select(staffScope).where(usersBelongsToSelectedMembers);
        return cb.and(
                cb.equal(scope.get(Scope_.scopeType), ScopeType.STAFF_USERS),
                cb.or(cb.exists(employmentQuery),
                        cb.in(scope).value(staffScopeQuery)));
    }

    protected Predicate getUserBasedCampaignRestrictionPredicatesForStaffDepartments(
            CriteriaBuilder cb, CriteriaQuery<?> query, Root<Campaign> root,
            Join<Campaign, Scope> scope, final User loggedInUser) {
        // ensure to operate on staff scope rather than scope!
        Subquery<StaffScope> staffScopeQuery = query.subquery(StaffScope.class);
        Root<StaffScope> staffScope = staffScopeQuery.from(StaffScope.class);

        Subquery<Employment> employmentQuery = query.subquery(Employment.class);
        Root<Employment> employment = employmentQuery.from(Employment.class);
        Join<Employment, User> user = employment.join(Employment_.user);

        Predicate userIsPartOfEmployment = cb.equal(user, loggedInUser);
        Predicate usersBelongsToDepartment = cb.isMember(loggedInUser
                .getCurrentDepartment(), staffScope
                .get(StaffScope_.departments));

        Predicate userIsManager = cb.and(cb.equal(root.get(Campaign_.company),
                loggedInUser.getCurrentCompany()), cb.equal(
                employment.get(Employment_.role), UserRole.MANAGER));
        Predicate userIsManagerOrPartOfScope = cb.and(userIsManager,
                userIsPartOfEmployment);

        employmentQuery.select(employment).where(userIsManagerOrPartOfScope);
        staffScopeQuery.select(staffScope).where(usersBelongsToDepartment);
        return cb.and(cb.equal(scope.get(Scope_.scopeType),
                ScopeType.STAFF_DEPARTMENTS), cb.or(cb.exists(employmentQuery),
                cb.in(scope).value(staffScopeQuery)));
    }

    protected Predicate getUserBasedCampaignRestrictionPredicatesForNetworkAll(
            CriteriaBuilder cb, CriteriaQuery<?> query, Root<Campaign> root,
            Join<Campaign, Scope> scope, final User loggedInUser) {
        // ensure to operate on network scope rather than scope!
        Subquery<NetworkScope> networkScopeQuery = query
                .subquery(NetworkScope.class);
        Root<NetworkScope> networkScope = networkScopeQuery
                .from(NetworkScope.class);
        Subquery<Network> networkQuery = query.subquery(Network.class);
        Root<Network> network = networkQuery.from(Network.class);
        Predicate companyIsNetworkedWithScopeCompany = cb.and(cb.equal(network
                        .get(Network_.company), loggedInUser.getCurrentCompany()),
                cb.equal(scope.get(Scope_.company),
                        network.get(Network_.companyNetworked)));
        Predicate companyIsInverseNetworkedWithScopeCompany = cb.and(cb.equal(
                network.get(Network_.companyNetworked), loggedInUser
                        .getCurrentCompany()), cb.equal(
                scope.get(Scope_.company), network.get(
                        Network_.company)));
        Predicate companyIsNetworked = cb.or(
                companyIsNetworkedWithScopeCompany,
                companyIsInverseNetworkedWithScopeCompany);
        networkScopeQuery.select(networkScope);
        networkQuery.select(network).where(companyIsNetworked);
        return cb.and(
                cb.equal(scope.get(Scope_.scopeType), ScopeType.NETWORK_ALL),
                cb.exists(networkQuery), cb.in(scope).value(networkScopeQuery));
    }

    protected Predicate getUserBasedCampaignRestrictionPredicatesForNetworkMembers(
            CriteriaBuilder cb, CriteriaQuery<?> query, Root<Campaign> root,
            Join<Campaign, Scope> scope, final User loggedInUser) {
        // ensure to operate on network scope rather than scope!
        Subquery<NetworkScope> networkScopeQuery = query
                .subquery(NetworkScope.class);
        Root<NetworkScope> networkScope = networkScopeQuery
                .from(NetworkScope.class);
        SetJoin<NetworkScope, Company> networkedCompanies = networkScope
                .join(NetworkScope_.networkedCompanies);

        Subquery<Network> networkQuery = query.subquery(Network.class);
        Root<Network> network = networkQuery.from(Network.class);

        Predicate companyIsNetworkedWithScopeCompany = cb.and(cb.equal(network
                        .get(Network_.company), loggedInUser.getCurrentCompany()),
                cb.equal(scope.get(Scope_.company),
                        network.get(Network_.companyNetworked)));
        Predicate companyIsInverseNetworkedWithScopeCompany = cb.and(cb.equal(
                network.get(Network_.companyNetworked), loggedInUser
                        .getCurrentCompany()), cb.equal(
                scope.get(Scope_.company), network.get(
                        Network_.company)));
        Predicate companyIsNetworked = cb.or(
                companyIsNetworkedWithScopeCompany,
                companyIsInverseNetworkedWithScopeCompany);
        Predicate companyIsInMembersList = networkedCompanies
                .in(loggedInUser.getCurrentCompany());

        networkScopeQuery.select(networkScope).where(companyIsInMembersList);
        networkQuery.select(network).where(companyIsNetworked);
        return cb.and(cb.equal(scope.get(Scope_.scopeType),
                ScopeType.NETWORK_COMPANIES), cb.exists(networkQuery),
                cb.in(scope).value(networkScopeQuery));
    }

    public List<Campaign> getForPICNNotifications(int offset, int count, User user) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Notification n LEFT JOIN n.campaign c WHERE n.user=:user AND DTYPE=:dType AND creationDate > :lastWeek");
        TypedQuery<Campaign> q = em.createQuery(jpql.toString(), Campaign.class);
        q.setParameter("user", user);
        q.setParameter("dType", "PICN");
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        q.setParameter("lastWeek", cal.getTime());
        List<Campaign> ret = q.setFirstResult(offset).setMaxResults(count).getResultList();
        return ret;
    }

    public List<Campaign> getAllFromToByAscendingDueDate(
            int first,
            int count,
            InnovationObjective innovationObjective,
            Date minDate,
            Date maxDate,
            final User loggedInUser) {
        logger.infof(
                "loading %d campaigns starting from %d ordered by ascending due date ...",
                count,
                first);
        List<Long> idsAll = getAllIdsFromToByAscendingDueDate(innovationObjective, minDate, maxDate, loggedInUser);
        // if there is zero match then return from here.
        if (idsAll.size() == 0) {
            return new LinkedList<>();
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        criteria.where(root.get(Campaign_.id).in(idsAll));
        criteria.orderBy(cb.asc(root.get(Campaign_.dueDate)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public List<Campaign> getAllCampaignsByDescendingCreationDate(
            int first,
            int count) {
        logger.infof(
                "loading %d campaigns starting from %d ordered by ascending Creation date ...",
                count,
                first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        //criteria.where(root.get(Campaign_.id).in(ids));
        criteria.orderBy(cb.desc(root.get(Campaign_.createdOn)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public List<Campaign> getAllCampaignsWithInnovationStatusByDescendingCreationDate(
            int first, int count, InnovationStatus innovationStatus) {
        logger.infof(
                "loading %d campaigns starting from %d ordered by ascending creation date...",
                count,
                first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        // criteria.where(root.get(Campaign_.id).in(ids));
        criteria.where(cb.equal(root.get(Campaign_.innovationStatus), innovationStatus));
        criteria.orderBy(cb.desc(root.get(Campaign_.createdOn)));
        criteria.distinct(true);

        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();

    }

    public long countAllCampaignsWithInnovationStatus(InnovationStatus innovationStatus) {
        logger.infof("counting campaigns ordered by ascending creation date...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        // criteria.where(root.get(Campaign_.id).in(ids));
        if (innovationStatus != null) {
            criteria.where(cb.equal(root.get(Campaign_.innovationStatus), innovationStatus));
        }
        criteria.orderBy(cb.desc(root.get(Campaign_.createdOn)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    public List<Campaign> getAllCampaignsByDescendingCreationDateAndCreatedBy(
            int first, int count, User createdBy, EntitiySortingCriteria sortingCriteria) {
        logger.infof(
                "loading %d campaigns starting from %d ordered by ascending Creation date and by CreatedBy %s...",
                count,
                first,
                createdBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        //criteria.where(root.get(Campaign_.id).in(ids));
        criteria.where(cb.equal(root.get(Campaign_.createdBy), createdBy));
        if (sortingCriteria.equals(EntitiySortingCriteria.created)) {
            criteria.orderBy(cb.desc(root.get(Campaign_.createdOn)));
        } else {
            criteria.orderBy(cb.desc(root.get(Campaign_.lastModified)));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long countCampaignsForUser(User createdBy) {
        logger.infof("counting all campaigns for user %s ...", createdBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Campaign_.deleted)));
        if (createdBy != null) {
            predicates.add(cb.equal(root.get(Campaign_.createdBy), createdBy));
        }

        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
//      criteria.orderBy(cb.asc(root.get(Campaign_.name)));
        criteria.select(root.get(Campaign_.id));
        return em.createQuery(criteria).getResultList().size();
    }

    public long countTotalLikesForCampaignCreatedByUser(User user) {
        logger.infof("counting all likes for campaigns created by user %s ...", user);

        long returnVal = 0;
        for (Campaign cam : getAllCampaignsCreatedByUser(user)) {
            returnVal += cam.getNoOfUpVotes();
        }

        return returnVal;
    }

    /**
     * This method counts likes given by user for all challenges.
     *
     * @param user
     * @return
     */
    public long countTotalLikesGivenByUserForCampaign(User user) {
        logger.infof("counting all likes by given user %s for all campaigns  ...", user);

        long returnVal = 0;
        for (Campaign cam : getAll()) {
            if (cam.getUpVotes().contains(user.getId())) {
                returnVal += 1;
            }
        }

        return returnVal;
    }

    /**
     * This method counts likes for all challenges.
     *
     * @param
     * @return
     */
    public long countTotalLikesForAllChallenges() {
        logger.infof("counting all likes for all challenges...");

        long returnVal = 0;
        for (Campaign cam : getAll()) {
            returnVal += cam.getNoOfUpVotes();
        }

        return returnVal;
    }

    public List<Campaign> getAllCampaignsCreatedByUser(User createdBy) {
        logger.infof("getting all campaigns created by user %s ...", createdBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Campaign_.deleted)));
        if (createdBy != null) {
            predicates.add(cb.equal(root.get(Campaign_.createdBy), createdBy));
        }
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }


    public List<Campaign> getAllChallengesByDescendingCreationDateAndCommentedBy(int first, int count, User commentedBy) {
        logger.infof("loading %d challenges starting from %d ordered by ascending Creation date and commentedBy %s...", count,
                first, commentedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> queryCommentedBy = cb.createQuery(Campaign.class);
        Root<Campaign> root = queryCommentedBy.from(Campaign.class);
        Join<Campaign, Comment> comments = root.join(Campaign_.comments);
        Join<Comment, User> postedBy = comments.join(Comment_.postedBy);
        queryCommentedBy.where(cb.equal(postedBy, commentedBy));
        queryCommentedBy.distinct(true);
        return em.createQuery(queryCommentedBy).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long getCountForAllChallengesByDescendingCreationDateAndCommentedBy(User commentedBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> queryCommentedBy = cb.createQuery(Campaign.class);
        Root<Campaign> root = queryCommentedBy.from(Campaign.class);
        Join<Campaign, Comment> comments = root.join(Campaign_.comments);
        Join<Comment, User> postedBy = comments.join(Comment_.postedBy);
        queryCommentedBy.where(cb.equal(postedBy, commentedBy));
        queryCommentedBy.distinct(true);
        return em.createQuery(queryCommentedBy).getResultList().size();
    }

    public List<Campaign> getOpenCampaign() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        criteria.where(
                cb.equal(root.get(Campaign_.campaignType), CampaignType.FREE_FORM)
        );
        return em.createQuery(criteria).getResultList();
    }

    private List<Long> getAllIdsFromToByAscendingDueDate(
            InnovationObjective innovationObjective,
            Date minDate,
            Date maxDate,
            final User loggedInUser)
            throws IllegalArgumentException {
        logger.infof("loading campaign ids ordered by ascending due date ...");

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Scope> scope = root.join(Campaign_.scope, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.not(root.get(Campaign_.deleted)));
        if (innovationObjective != null) {
            predicates.add(cb.equal(root.get(Campaign_.innovationObjective), innovationObjective));
        }
        if (minDate != null) {
            predicates.add(cb.or(
                    cb.isNull(root.get(Campaign_.startDate)),
                    cb.greaterThanOrEqualTo(root.get(Campaign_.startDate), minDate)));
        }
        if (maxDate != null) {
            //Make sure that maxDate matches all the values with 23:59 hour time for entire day.
            maxDate = new DateTime(maxDate.getTime()).plusDays(1).withTimeAtStartOfDay().toDate();
            predicates.add(cb.and(
                    cb.isNotNull(root.get(Campaign_.dueDate)),
                    cb.lessThan(root.get(Campaign_.dueDate), maxDate)));
        }
        predicates.addAll(getUserScopeCampaignPredicates(cb, criteria, root, scope, loggedInUser));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        criteria.distinct(true);
        criteria.select(root.get(Campaign_.id));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @param first
     * @param count
     * @param innovationObjective OPTIONAL to filter by {@link InnovationObjective}
     * @return
     */
    public List<Campaign> getAllFromToByAscendingDueDate(int first, int count, InnovationObjective innovationObjective, final User loggedInUser) {
        return getAllFromToByAscendingDueDate(first, count, innovationObjective, null, null, loggedInUser);
    }


    public List<Campaign> getOpenFromCompanyByAscendingName(Company company, final User loggedInUser) {
        Set<Long> ids = new HashSet<Long>();
        ids.addAll(getOpenIdsFromCompanyByAscendingName(company, loggedInUser));
        ids.addAll(getPrioritisationAccessibleCampaignsIds(null, loggedInUser));
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Scope> scope = root.join(Campaign_.scope, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Campaign_.deleted)));
        if (company != null) {
            predicates.add(cb.equal(root.get(Campaign_.company), company));
        }
        predicates.add(cb.equal(scope.get(Scope_.scopeType), ScopeType.OPEN));
        predicates.add(root.get(Campaign_.id).in(ids));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.orderBy(cb.asc(root.get(Campaign_.name)));
        return em.createQuery(criteria).getResultList();
    }

    protected List<Long> getOpenIdsFromCompanyByAscendingName(Company company, final User loggedInUser) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Scope> scope = root.join(Campaign_.scope, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Campaign_.deleted)));
        if (company != null) {
            predicates.add(cb.equal(root.get(Campaign_.company), company));
        }
        predicates.add(cb.equal(scope.get(Scope_.scopeType), ScopeType.OPEN));
        predicates.addAll(getUserScopeCampaignPredicates(cb, criteria, root, scope, loggedInUser));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.select(root.get(Campaign_.id));
        return em.createQuery(criteria).getResultList();
    }

    public Long countOpenFromCompany(Company company, final User loggedInUser) {
        Set<Long> ids = new HashSet<Long>();
        ids.addAll(getOpenIdsFromCompanyByAscendingName(company, loggedInUser));
        return (long) ids.size();
    }

    public List<Campaign> getAllByAscendingName(
            InnovationObjective innovationObjective, final User loggedInUser) {
        logger.infof("loading all campaigns ordered by ascending name and innovation objective...");
        Set<Long> ids = new HashSet<Long>();
        ids.addAll(getAllIdsByAscendingName(innovationObjective, loggedInUser));
        ids.addAll(getPrioritisationAccessibleCampaignsIds(innovationObjective, loggedInUser));
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        criteria.where(root.get(Campaign_.id).in(ids));
        criteria.orderBy(cb.asc(root.get(Campaign_.name)));
        return em.createQuery(criteria).getResultList();
    }

    protected List<Long> getAllIdsByAscendingName(InnovationObjective innovationObjective, final User loggedInUser) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Scope> scope = root.join(Campaign_.scope, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Campaign_.deleted)));
        if (innovationObjective != null) {
            predicates.add(cb.equal(root.get(Campaign_.innovationObjective),
                    innovationObjective));
        }
        predicates.addAll(getUserScopeCampaignPredicates(cb, criteria, root, scope, loggedInUser));
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                .size()])));
        criteria.distinct(true);
        criteria.select(root.get(Campaign_.id));
        return em.createQuery(criteria).getResultList();
    }

    public List<Campaign> getCampaignsForFilterPanel(InnovationObjective innovationObjective, final User loggedInUser) {
        Set<Long> ids = new HashSet<Long>();
        ids.addAll(getAllIdsFromToByAscendingDueDate(null, null, null, loggedInUser));
        ids.addAll(getPrioritisationAccessibleCampaignsIds(innovationObjective, loggedInUser));
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        criteria.where(root.get(Campaign_.id).in(ids));
        criteria.orderBy(cb.asc(root.get(Campaign_.dueDate)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    protected List<Long> getCampaignIdssForFilterPanel(InnovationObjective innovationObjective, final User loggedInUser) {
        logger.infof("loading campaign ids for filter panel...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Campaign_.deleted)));

        // Requirement by ATB on 2013/10/08: Show innovation status
        // PRIORITISATION, FOLLOW_UP, IMPLEMENTATION
        predicates.add(cb.or(
                cb.equal(root.get(Campaign_.innovationStatus), InnovationStatus.PRIORITISATION),
                cb.equal(root.get(Campaign_.innovationStatus), InnovationStatus.FOLLOW_UP),
                cb.equal(root.get(Campaign_.innovationStatus), InnovationStatus.IMPLEMENTATION)));

        // Requirement by ATB on 2013/10/07: Normal logged in user can
        // only see campaigns he is invited to participate in. This does not
        // apply for admin users. This check is handled in
        // getUserScopeCampaignPredicates
        Join<Campaign, Scope> scope = root.join(Campaign_.scope, JoinType.LEFT);
        predicates.addAll(getUserScopeCampaignPredicates(cb, criteria, root, scope, loggedInUser));

        // Requirement by ATB on 2013/10/07: Logged in user can only
        // see his company's campaigns
        predicates.add(cb.equal(root.get(Campaign_.company), loggedInUser.getCurrentCompany()));

        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.distinct(true);
        criteria.select(root.get(Campaign_.id));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @param innovationObjective
     * @return
     */
    public long countAllFromToByAscendingDueDate(InnovationObjective innovationObjective, Date startDate, Date endDate, final User loggedInUser) {
        logger.infof("counting campaigns ordered by ascending due date ...");
        Set<Long> ids = new HashSet<>();
        ids.addAll(getAllIdsFromToByAscendingDueDate(innovationObjective, startDate, endDate, loggedInUser));
        ids.addAll(getPrioritisationAccessibleCampaignsIds(innovationObjective, loggedInUser));
        return ids.size();
    }

    /**
     * FIXME: compute actual similarity
     *
     * @return
     */
    public List<Campaign> getSimilarCampaigns(final User loggedInUser) {
        logger.infof("loading similar campaigns ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Scope> scope = root.join(Campaign_.scope, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<Predicate>(2);
        predicates.add(cb.not(root.get(Campaign_.active)));
        predicates.add(cb.isNotEmpty(root.get(Campaign_.ideas)));
        predicates.addAll(getUserScopeCampaignPredicates(cb, criteria, root,
                scope, loggedInUser));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.orderBy(cb.asc(root.get(Campaign_.dueDate)));
        return em.createQuery(criteria).setMaxResults(5).getResultList();
    }

    /**
     * @return
     */
    public long countSimilar(final User loggedInUser) {
        logger.info("counting similar campaigns ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> from = criteria.from(Campaign.class);
        Join<Campaign, Scope> scope = from.join(Campaign_.scope, JoinType.LEFT);
        List<Predicate> predicates = new ArrayList<Predicate>(2);
        predicates.add(cb.not(from.get(Campaign_.active)));
        predicates.add(cb.isNotEmpty(from.get(Campaign_.ideas)));
        predicates.addAll(getUserScopeCampaignPredicates(cb, criteria, from, scope, loggedInUser));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.select(cb.countDistinct(from));
        return em.createQuery(criteria).getSingleResult();
    }

    public Campaign getByScope(Scope scope) {
        logger.infof("Trying to find campaign for scope with id "
                + scope.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        criteria.where(cb.equal(root.get(Campaign_.scope), scope));
        criteria.orderBy(cb.asc(root.get(Campaign_.dueDate)));
        return em.createQuery(criteria).setMaxResults(1).getSingleResult();

    }

    /**
     * @param campaignId
     * @return
     */
    public boolean campaignExists(Long campaignId) {
        logger.info(String.format("checking if campaign with ID %d exists ...",
                campaignId));
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> from = criteria.from(Campaign.class);
        criteria.where(cb.equal(from.get(Campaign_.id), campaignId));
        criteria.select(cb.countDistinct(from));
        return (em.createQuery(criteria).getSingleResult().longValue() == 1);
    }

    /**
     * @param campaignId
     * @return
     */
    @Override
    public Campaign getById(Long campaignId) {
        Campaign campaign = super.getById(campaignId);
        if ((campaign != null) && campaign.getDeleted()) {
            throw new EntityNotFoundException(String.format(
                    "Campaign with id %d does not exist", campaignId));
        }
        return campaign;
    }

    /**
     * @param campaignId
     * @param postedAt
     * @param count
     * @return
     */
    public List<Idea> getIdeasForCampaignBeforeDateByDescendingPostDate(
            Long campaignId, Date postedAt, int count) {
        logger.infof(
                "loading %d ideas before %s ordered by descending post date for campaign with ID %d ...",
                count, postedAt, campaignId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Idea> join = root.join(Campaign_.ideas);
        criteria.select(join)
                .where(cb.and(
                        cb.equal(root.get(Campaign_.id), campaignId),
                        cb.lessThan(join.get(Idea_.postedAt), postedAt),
                        cb.not(join.get(Idea_.deleted))))
                .orderBy(cb.desc(join.get(Idea_.postedAt)));
        return em.createQuery(criteria).setMaxResults(count).getResultList();
    }

    /**
     * @param campaign
     * @param first
     * @param count
     * @return
     */
    public List<Idea> getIdeasForCampaignFromToByDescendingPostDate(final Campaign campaign, final int first, final int count) {
        logger.infof(
                "loading %d ideas starting from %d ordered by descending post date for campaign with ID %d ...",
                count, first, campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        criteria.select(root)
                .where(cb.and(
                        cb.equal(root.get(Idea_.campaign), campaign),
                        cb.not(root.get(Idea_.deleted))))
                .orderBy(cb.desc(root.get(Idea_.postedAt)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param campaign
     * @param first
     * @param count
     * @return
     */
    public List<Idea> getIdeasForCampaignFromToByDescendingNoOfComments(final Campaign campaign,
                                                                        final int first,
                                                                        final int count) {
        logger.infof(
                "loading %d ideas starting from %d ordered by descending no. of comments for campaign with ID %d ...",
                count, first, campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        ListJoin<Idea, Comment> join = root.join(Idea_.comments, JoinType.LEFT);
        criteria.select(root)
                .where(cb.and(
                        cb.equal(root.get(Idea_.campaign), campaign),
                        cb.not(root.get(Idea_.deleted))))
                .groupBy(root)
                .orderBy(cb.desc(cb.count(join)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param campaign
     * @param first
     * @param count
     * @return
     */
    public List<Idea> getIdeasForCampaignFromToByDescendingNoOfLikes(final Campaign campaign,
                                                                     final int first,
                                                                     final int count) {
        logger.infof(
                "loading %d ideas starting from %d ordered by descending no. of likes for campaign with ID %d ...",
                count, first, campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        criteria.select(root)
                .where(cb.and(
                        cb.equal(root.get(Idea_.campaign), campaign),
                        cb.not(root.get(Idea_.deleted))))
                .orderBy(cb.desc(cb.size(root.get(Idea_.upVotes))));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param campaign
     * @param first
     * @param count
     * @return
     */
    public List<Idea> getIdeasForCampaignFromToByDescendingFeasibility(final Campaign campaign, final int first, final int count) {
        logger.infof(
                "loading %d ideas starting from %d ordered by descending no. of feasibility votes for campaign with ID %d ...",
                count, first, campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        criteria.select(root).where(cb.and(cb.equal(root.get(Idea_.campaign), campaign), cb.not(root.get(Idea_.deleted))))
                .orderBy(cb.desc(root.get(Idea_.prioritisationDotFeasibilityVoteAVG)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param campaign
     * @param first
     * @param count
     * @return
     */
    public List<Idea> getIdeasForCampaignFromToByDescendingRelevancy(final Campaign campaign, final int first, final int count) {
        logger.infof(
                "loading %d ideas starting from %d ordered by descending no. of relevancy votes for campaign with ID %d ...",
                count, first, campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        criteria.select(root).where(cb.and(cb.equal(root.get(Idea_.campaign), campaign), cb.not(root.get(Idea_.deleted))))
                .orderBy(cb.desc(root.get(Idea_.prioritisationDotRelevanceVoteAVG)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param campaign
     * @param first
     * @param count
     * @return
     */
    public List<Idea> getIdeasForCampaignFromToByDescendingOverAllRating(final Campaign campaign, final int first, final int count) {
        logger.infof(
                "loading %d ideas starting from %d ordered by descending no. of over-all votes for campaign with ID %d ...",
                count, first, campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<Idea> root = criteria.from(Idea.class);
        criteria.select(root).where(cb.and(cb.equal(root.get(Idea_.campaign), campaign), cb.not(root.get(Idea_.deleted))))
                .orderBy(cb.desc(root.get(Idea_.prioritisationDotVoteAVG)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param campaignId
     * @return
     */
    public long countIdeasForCampaign(Long campaignId) {
        logger.infof("counting all ideas for campaign with ID %d ...",
                campaignId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Idea> join = root.join(Campaign_.ideas);
        criteria.select(cb.countDistinct(join))
                .where(cb.and(
                        cb.equal(root.get(Campaign_.id), campaignId),
                        cb.not(join.get(Idea_.deleted))));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param challenge
     * @param user
     * @return
     */
    public long countIdeasForCampaignByUser(Campaign challenge, User user) {
        logger.infof("counting all ideas for campaign %s postedBy user %s...", challenge, user);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Idea> join = root.join(Campaign_.ideas);

        Join<Idea, User> postedBy = join.join(Idea_.postedBy);

        criteria.select(cb.countDistinct(join))
                .where(cb.and(cb.equal(postedBy, user),
                        cb.equal(root.get(Campaign_.id), challenge.getId()),
                        cb.not(join.get(Idea_.deleted))));

        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param challenge
     * @param user
     * @return
     */
    public long countCommentsForCampaignByUser(Campaign challenge, User user) {
        logger.infof("counting all comments for campaign %s postedBy user %s...", challenge, user);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, Comment> join = root.join(Campaign_.comments);

        Join<Comment, User> postedBy = join.join(Comment_.postedBy);

        criteria.select(cb.countDistinct(join))
                .where(cb.and(cb.equal(postedBy, user),
                        cb.equal(root.get(Campaign_.id), challenge.getId())));

        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param ideaId
     * @return
     */
    public boolean ideaExists(Long ideaId) {
        logger.info(String.format("checking if idea with ID %d exists ...",
                ideaId));
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Idea> from = criteria.from(Idea.class);
        criteria.where(cb.and(cb.equal(from.get(Idea_.id), ideaId),
                cb.not(from.get(Idea_.deleted))));
        criteria.select(cb.countDistinct(from));
        return (em.createQuery(criteria).getSingleResult().longValue() == 1);
    }

    /**
     * @param campaign
     * @return
     */
    @Override
    public Campaign create(Campaign campaign) {
        campaign = createOrUpdate(campaign);
        logger.infof("persisted campaign %s ...", campaign);

        // update counter noOfCampaignsLeads for campaign leader
        userService.increaseNoOfCampaignsLeads(campaign.getCreatedBy());

        return campaign;
    }

    /**
     * @param campaign
     * @return
     */
    @Override
    public Campaign update(Campaign campaign) {
        campaign = createOrUpdate(campaign);
        logger.infof("updated campaign %s ...", campaign);
        return campaign;
    }

    /**
     * @param campaign
     * @return
     */
    public Campaign createOrUpdate(Campaign campaign) {
        campaign = em.merge(campaign);
        // send event to start or stop the automatic stop timer
        campaignEventSrc.fire(campaign);
        logger.infof("fired event to update timer for campaign %s ...", campaign.getName());
        return campaign;
    }

    public void startDefinitionPhase(final Campaign campaign) {
        campaign.setInnovationStatus(InnovationStatus.DEFINITION);
        campaign.setDefinitionActive(true);
        campaign.setIdeationActive(false);
        campaign.setSelectionActive(false);
        update(campaign);
    }

    public void stopDefinitionPhase(final Campaign campaign) {
        campaign.setDefinitionActive(false);
        campaign.setInnovationStatus(InnovationStatus.INCEPTION);
        campaign.setIdeationActive(true);
        campaign.setSelectionActive(false);
        update(campaign);
        // send campaign follower notification that campaign is advanced into next stage.
        List<User> followers = userService.getAllUsersByGivenFollowedChallenge(campaign, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (User follower : followers) {
            participateNotifier.addParticipationNotification(campaign, follower, NotificationType.CAMPAIGN_UPDATE_IDEATION);
        }
    }

    public void startIdeationPhase(final Campaign campaign) {
        campaign.setDefinitionActive(false);
        campaign.setInnovationStatus(InnovationStatus.INCEPTION);
        campaign.setIdeationActive(true);
        campaign.setSelectionActive(false);
        update(campaign);
    }

    public void stopIdeationPhase(final Campaign campaign) {
        campaign.setDefinitionActive(false);
        campaign.setIdeationActive(false);
        campaign.setInnovationStatus(InnovationStatus.PRIORITISATION);
        campaign.setSelectionActive(true);
        update(campaign);
        // send campaign follower notification that campaign is advanced into next stage.
        List<User> followers = userService.getAllUsersByGivenFollowedChallenge(campaign, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (User follower : followers) {
            participateNotifier.addParticipationNotification(campaign, follower, NotificationType.CAMPAIGN_UPDATE_SELECTION);
        }
    }

    public void startSelectionPhase(final Campaign campaign) {
        campaign.setDefinitionActive(false);
        campaign.setInnovationStatus(InnovationStatus.PRIORITISATION);
        campaign.setIdeationActive(false);
        campaign.setSelectionActive(true);
        update(campaign);
    }

    public void stopSelectionPhase(final Campaign campaign) {
        campaign.setDefinitionActive(false);
        campaign.setIdeationActive(false);
        campaign.setInnovationStatus(InnovationStatus.IMPLEMENTATION);
        campaign.setSelectionActive(false);
        update(campaign);
        // send campaign follower notification that campaign is advanced into next stage.
        List<User> followers = userService.getAllUsersByGivenFollowedChallenge(campaign, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (User follower : followers) {
            participateNotifier.addParticipationNotification(campaign, follower, NotificationType.CAMPAIGN_UPDATE_IMPLEMENTATION);
        }
    }

    /**
     * @param campaignId
     */
    public void stopCampaign(Long campaignId) {
        final Campaign campaign = getById(campaignId);
        if (campaign.getDefinitionActive()) {
            stopDefinitionPhase(campaign);
        } else if (campaign.getIdeationActive()) {
            stopIdeationPhase(campaign);
        } else if (campaign.getSelectionActive()) {
            stopSelectionPhase(campaign);
        }
    }

    /**
     * Mark campaign as deleted but do not really remove from DB.
     *
     * @param campaignId
     */
    public void softDelete(Long campaignId) {
        Campaign campaign = getById(campaignId);
        softDelete(campaign);
    }

    /**
     * Mark campaign as deleted but do not really remove from DB.
     *
     * @param campaign
     */
    public void softDelete(Campaign campaign) {
        for (Idea idea : campaign.getIdeas()) {
            softRemoveIdeaFromCampaign(idea);
        }
        campaign.setDeleted(true);

        // once campaign is deleted decrease NoOfCampaignLeads
        userService.decreaseNoOfCampaignsLeads(campaign.getCreatedBy());

        update(campaign);
    }

    /**
     * @param campaigns
     */
    public void softDelete(Collection<Campaign> campaigns) {
        for (Campaign campaign : campaigns) {
            softDelete(campaign);
        }
    }

    /**
     * @param campaignId
     * @param idea
     * @return
     */
    public Idea addIdea(Long campaignId, Idea idea) {
        Campaign campaign = getById(campaignId);
        idea = addIdeaToCampaign(campaign, idea);
        logger.infof("added idea %s to campaign with ID %d ...", idea,
                campaignId);
        return idea;
    }

    /**
     * @param campaign
     * @param idea
     * @return
     */
    public Idea addIdea(Campaign campaign, Idea idea) {
        campaign = em.merge(campaign);
        idea = addIdeaToCampaign(campaign, idea);
        logger.infof("added idea %s to campaign %s ...", idea, campaign);
        return idea;
    }

    /**
     * @param campaign
     * @param idea
     * @return
     */
    private Idea addIdeaToCampaign(Campaign campaign, Idea idea) {
        idea.setCampaign(campaign);

        //in order to be double sure, if new idea is being added to campaign
        if (idea.getId() == null) {
            idea = ideaService.create(idea);
        } else {
            idea = ideaService.createOrUpdate(idea);
        }

        campaign.addIdea(idea);
        em.merge(campaign);
        return idea;
    }

    /**
     * @param idea
     * @return
     */
    public Idea updateIdea(Idea idea) {
        return ideaService.update(idea);
    }

    /**
     * @param campaignId
     * @param ideaId
     */
    public void removeIdea(Long campaignId, Long ideaId) {
        getById(campaignId);
        Idea idea = ideaService.getById(ideaId);
        softRemoveIdeaFromCampaign(idea);
        logger.infof("removed idea with ID %d from campaign with ID %d ...",
                ideaId, campaignId);
    }

    /**
     * @param campaign
     * @param idea
     */
    public void removeIdea(Campaign campaign, Idea idea) {
        campaign = em.merge(campaign);
        idea = ideaService.createOrUpdate(idea);
        softRemoveIdeaFromCampaign(idea);
        logger.infof("removed idea %s from campaign %s ...", idea, campaign);
    }

    /**
     * @param idea
     */
    private void softRemoveIdeaFromCampaign(Idea idea) {
        ideaService.softDelete(idea);
    }

    public List<Campaign> getForPICPNNotifications(int offset, int count, User user) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Notification n LEFT JOIN n.campaign c WHERE n.user=:user AND DTYPE=:dType AND creationDate > :lastWeek");
        TypedQuery<Campaign> q = em.createQuery(jpql.toString(), Campaign.class);
        q.setParameter("user", user);
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        q.setParameter("lastWeek", cal.getTime());
        q.setParameter("dType", "PICPN");
        List<Campaign> ret = q.setFirstResult(offset).setMaxResults(count).getResultList();
        return ret;
    }

    public List<Campaign> getForNCNNotifications(int offset, int count, User user) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Notification n LEFT JOIN n.campaign c WHERE n.user=:user AND DTYPE=:dType AND creationDate > :lastWeek");
        TypedQuery<Campaign> q = em.createQuery(jpql.toString(), Campaign.class);
        q.setParameter("user", user);
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        q.setParameter("lastWeek", cal.getTime());
        q.setParameter("dType", "NCN");
        List<Campaign> ret = q.setFirstResult(offset).setMaxResults(count).getResultList();
        return ret;
    }

    public void notifyAboutParticipation(List<User> users, Campaign campaign) {
        try {
            String urlLink = urlProvider.urlFor(CampaignsPage.class, "id", String.valueOf(campaign.getId()));
            String logoLink = urlProvider.urlFor(new PackageResourceReference(BasePage.class, "img/soc_logo_124x32px_horizontal.png"), null);
            for (User user : users) {
                boolean send = true;
                //Don't send to campaign creator
                if (user.equals(campaign.getCreatedBy())) {
                    continue;
                }
                //Send only to active users
                for (Employment e : user.getEmployments()) {
                    if (e.getCompany().equals(campaign.getCompany()) && !e.getActive()) {
                        send = false;
                        break;
                    }
                }
                if (send) {
                    participateNotifier.sendParticipationMail(campaign, user, urlLink, logoLink, NotificationType.CAMPAIGN_PARTICIPATE);
                    participateNotifier.addParticipationNotification(campaign, user, NotificationType.CAMPAIGN_PARTICIPATE);
                }
            }
        } catch (NotificationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void notifyAboutParticipationBasedOnSkillsAndIntersts(List<User> users, Campaign campaign, NotificationType notificationType) {
        try {
            String urlLink = urlProvider.urlFor(CampaignsPage.class, "id", String.valueOf(campaign.getId()));
            String logoLink = urlProvider.urlFor(new PackageResourceReference(BasePage.class, "img/soc_logo_124x32px_horizontal.png"), null);
            for (User user : users) {
                boolean send = false;
                //Don't send to campaign creator
                if (user.equals(campaign.getCreatedBy())) {
                    continue;
                }
                for (Tag skill : user.getSkills()) {
                    for (Tag interest : user.getInterests()) {
                        if (campaign.getTags().contains(skill) || campaign.getTags().contains(interest)) {
                            send = true;
                            break;
                        }
                    }
                }
                if (send) {
                    participateNotifier.sendParticipationMail(campaign, user, urlLink, logoLink, notificationType);
                    participateNotifier.addParticipationNotification(campaign, user, notificationType);
                }
            }
        } catch (NotificationException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<Campaign> getActivePublicChallenges(int first, int count) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> campaign = criteria.from(Campaign.class);

        criteria.where(cb.not(campaign.get(Campaign_.deleted)));
        criteria.orderBy(cb.desc(campaign.get(Campaign_.createdOn)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public int countActivePublicChallenges() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<Campaign> campaign = criteria.from(Campaign.class);

        criteria.where(cb.not(campaign.get(Campaign_.deleted)));
        criteria.orderBy(cb.desc(campaign.get(Campaign_.createdOn)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * @param campaign
     * @param comment
     * @return
     */
    public Comment addComment(Campaign campaign, Comment comment) {
        campaign = em.merge(campaign);
        comment = addCommentToChallenge(campaign, comment);
        logger.infof("added comment %s to challenge %s ...", comment, campaign);
        return comment;
    }

    /**
     * @param campaign
     * @param comment
     * @return
     */
    private Comment addCommentToChallenge(Campaign campaign, Comment comment) {
        comment = commentService.create(comment);
        campaign.addComment(comment);
        em.merge(campaign);
        return comment;
    }

    /**
     * @param campaignId
     * @param comment
     */
    public void removeComment(Long campaignId, Comment comment) {
        Campaign campaign = getById(campaignId);
        removeCommentFromCampaign(campaign, comment);
        logger.infof("removed comment %s from challenge with ID %d ...", comment,
                campaignId);
    }

    /**
     * @param campaign
     * @param comment
     */
    public void removeComment(Campaign campaign, Comment comment) {
        campaign = em.merge(campaign);
        removeCommentFromCampaign(campaign, comment);
        logger.infof("removed comment %s from challenge %s ...", comment, campaign);
    }

    /**
     * @param campaign
     * @param comment
     */
    private void removeCommentFromCampaign(Campaign campaign, Comment comment) {
        comment = em.merge(comment);
        campaign.removeComment(comment);

        // once comment is removed/deleted, reduce NoOfCommentsPosted for given user
        userService.decreaseNoOfCommentsPosted(comment.getPostedBy());

        em.merge(campaign);
    }

    /**
     * @param replyId
     * @return
     */
    public Reply getReplyById(Long replyId) {
        logger.infof("loading reply with ID %d ...", replyId);
        Reply reply = em.find(Reply.class, replyId);
        if (reply == null) {
            throw new EntityNotFoundException(String.format(
                    "Reply with id %d does not exist", replyId));
        }
        return reply;
    }

    /**
     * @param reply
     * @return
     */
    public Reply detachReply(Reply reply) {
        em.detach(reply);
        return reply;
    }

    /**
     * @param commentId
     * @param reply
     * @return
     */
    public Reply addReply(Long commentId, Reply reply) {
        Comment comment = commentService.getCommentById(commentId);
        reply = addReplyToComment(comment, reply);
        logger.infof("added reply %s to comment with ID %d ...", reply, commentId);
        return reply;
    }

    /**
     * @param comment
     * @param reply
     * @return
     */
    public Reply addReply(Comment comment, Reply reply) {
        comment = em.merge(comment);
        reply = addReplyToComment(comment, reply);
        logger.infof("added reply %s to comment %s ...", reply, comment);
        return reply;
    }

    /**
     * @param comment
     * @param reply
     * @return
     */
    private Reply addReplyToComment(Comment comment, Reply reply) {
        reply = em.merge(reply);
        comment.addReply(reply);
        em.merge(comment);
        return reply;
    }

    /**
     * @param reply
     * @return
     */
    public Reply updateReply(Reply reply) {
        reply = em.merge(reply);
        logger.infof("updated reply %s", reply);
        return reply;
    }

    /**
     * @param commentId
     * @param reply
     */
    public void removeReply(Long commentId, Reply reply) {
        Comment comment = commentService.getCommentById(commentId);
        removeReplyFromComment(comment, reply);
        logger.infof("removed reply %s from comment with ID %d ...", reply,
                commentId);
    }

    /**
     * @param comment
     * @param reply
     */
    public void removeComment(Comment comment, Reply reply) {
        comment = em.merge(comment);
        removeReplyFromComment(comment, reply);
        logger.infof("removed reply %s from comment %s ...", reply, comment);
    }

    /**
     * @param comment
     * @param reply
     */
    private void removeReplyFromComment(Comment comment, Reply reply) {
        reply = em.merge(reply);
        comment.removeReply(reply);
        em.merge(comment);
    }

    public Campaign getCampaignFromComment(Comment comment) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> query = cb.createQuery(Campaign.class);
        Root<Campaign> root = query.from(Campaign.class);
        Join<Campaign, Comment> join = root.join(Campaign_.comments);
        query.where(join.in(comment));
        List<Campaign> resultList = em.createQuery(query).setMaxResults(1).getResultList();
        if (resultList.size() > 0) {
            return resultList.get(0);
        }
        return null;
    }

    public Campaign getCampaignFromComment(Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> query = cb.createQuery(Campaign.class);
        Root<Campaign> root = query.from(Campaign.class);
        Join<Campaign, Comment> join = root.join(Campaign_.comments);
        query.where(join.in(comment));
        List<Campaign> resultList = em.createQuery(query).setMaxResults(1).getResultList();
        if (resultList.size() > 0) {
            return resultList.get(0);
        }
        return null;
    }

    /**
     * @param campaign
     * @param userId
     * @return
     */
    public Campaign voteUp(Campaign campaign, Long userId) {
        // remove user id from up votes if present
        // if not present (= remove returns false), add to up votes
        if (!campaign.getUpVotes().remove(userId)) {
            campaign.getUpVotes().add(userId);
        }
        return update(campaign);
    }

    /**
     * This will return all leaders from all challenges
     *
     * @return
     */
    public List<User> getAllChallengeLeadersByAscNickName() {
        logger.infof("loading leaders for all challenges ordered by ascending users' nick name...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, User> join = root.join(Campaign_.createdBy);
        criteria.select(join);
        criteria.orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will return all leaders from selected challenges
     *
     * @return
     */
    public List<User> getChallengeLeadersWithInnovationStatusOrderedByAscNickName(InnovationStatus innovationStatus,
                                                                                  Collection<Campaign> challenges) {
        logger.infof(
                "loading leaders for selected challenges with given innovation status %d ordered by ascending users' nick name...",
                innovationStatus);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Campaign> root = criteria.from(Campaign.class);
        Join<Campaign, User> join = root.join(Campaign_.createdBy);

        List<Predicate> predicates = new ArrayList<Predicate>();

        if (challenges != null && !challenges.isEmpty()) {
            predicates.add(root.in(challenges));
        }

        if (innovationStatus != null) {
            predicates.add(cb.equal(root.get(Campaign_.innovationStatus), innovationStatus));
        }

        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.select(join).orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    public void notifyCampaignFollowersAboutCampaignUpdates(Campaign theCampaign) {
        List<User> campaignFollowers = userService.getAllUsersByGivenFollowedChallenge(theCampaign, Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (User user : campaignFollowers) {
            participateNotifier.addParticipationNotification(theCampaign, user, NotificationType.CAMPAIGN_UPDATE);
        }
    }

    public void notifyCampaignFollowersAboutCampaignCommentsAndLikes(Campaign theCampaign) {
        // at first check if total comments and likes exceeds certain threshold, ie in multiple of 10.
        int totalComments = 0, totalLikes = 0;
        int threshold;
        if (!theCampaign.getComments().isEmpty() && theCampaign.getComments() != null) {
            totalComments = theCampaign.getComments().size();
        }
        if (!theCampaign.getUpVotes().isEmpty() && theCampaign.getUpVotes() != null) {
            totalLikes = theCampaign.getNoOfUpVotes();
        }

        threshold = (totalComments + totalLikes) % 10;
        if (totalComments != 0 || totalLikes != 0) {
            if (threshold == 0) {
                List<User> campaignFollowers = userService.getAllUsersByGivenFollowedChallenge(theCampaign, Integer.MAX_VALUE, Integer.MAX_VALUE);
                for (User user : campaignFollowers) {
                    participateNotifier.addParticipationNotification(theCampaign, user, NotificationType.CAMPAIGN_COMMENTS_LIKES);
                }
            }
        }
    }
}

/**
 *
 */
package de.atb.socratic.service.implementation;

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
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionActivity;
import de.atb.socratic.model.ActionActivity_;
import de.atb.socratic.model.Action_;
import de.atb.socratic.model.Activity;
import de.atb.socratic.model.ActivityType;
import de.atb.socratic.model.Activity_;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Campaign_;
import de.atb.socratic.model.ChallengeActivity;
import de.atb.socratic.model.ChallengeActivity_;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaActivity;
import de.atb.socratic.model.IdeaActivity_;
import de.atb.socratic.model.Idea_;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.model.User_;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;

/**
 * @author ATB
 */
@Stateless
public class ActivityService extends AbstractService<Activity> {

    /**
     *
     */
    private static final long serialVersionUID = 4775753392214337433L;

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

    /**
     *
     */
    public ActivityService() {
        super(Activity.class);
    }

    /**
     * @return
     */
    public List<ChallengeActivity> getAllChallengeActivitiesByDescendingCreationDateAndCampaign(Campaign campaign, int first, int count) {
        logger.infof("loading %d activities starting from %d ordered by descending Creation date ...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ChallengeActivity> criteria = cb.createQuery(ChallengeActivity.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        criteria.where(cb.equal(root.get(ChallengeActivity_.campaign), campaign));
        criteria.orderBy(cb.desc(root.get(ChallengeActivity_.performedAt)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public List<ChallengeActivity> getlatestChallengeActivities(int limit) {
        logger.infof("loading %d activities ordered by descending Creation date ...", limit);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ChallengeActivity> criteria = cb.createQuery(ChallengeActivity.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        criteria.orderBy(cb.desc(root.get(ChallengeActivity_.performedAt)));
        return em.createQuery(criteria).setMaxResults(limit).getResultList();
    }

    /**
     * @param challenge
     * @return
     */
    public int countAllChallengeActivitiesByCampaign(Campaign challenge) {
        logger.infof("counting all activities following given challenge %s ...,", challenge);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ChallengeActivity> criteria = cb.createQuery(ChallengeActivity.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        criteria.where(cb.equal(root.get(ChallengeActivity_.campaign), challenge));
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * @return
     */
    public List<User> getAllChallengeContributorsByAscNickNameAndCampaign(Campaign campaign, int first, int count) {
        logger.infof("loading %d Users starting from %d ordered by ascending users' nick name...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        Join<ChallengeActivity, User> join = root.join(ChallengeActivity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.and(cb.equal(root.get(ChallengeActivity_.campaign), campaign),
                cb.notEqual(root.get(ChallengeActivity_.activityType), ActivityType.CHALLENGE_LIKE)));

        if (!predicates.isEmpty()) {
            criteria.select(join).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            criteria.orderBy(cb.asc(join.get(User_.nickName)));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public List<User> getAllChallengeParticipantsByAscNickNameAndCampaign(Campaign campaign, int first, int count) {
        logger.infof("loading Users by given campaign with id %d ordered by ascending users' nick name...,", campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        Join<ChallengeActivity, User> join = root.join(ChallengeActivity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(ChallengeActivity_.campaign), campaign));

        if (!predicates.isEmpty()) {
            criteria.select(join).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            criteria.orderBy(cb.asc(join.get(User_.nickName)));
        }

        criteria.distinct(true);
        if (first != Integer.MAX_VALUE && count != Integer.MAX_VALUE) {
            return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
        }
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    public int countAllChallengeParticipantsByCampaign(Campaign campaign) {
        logger.infof("counting participants by given campaign id %d...", campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        Join<ChallengeActivity, User> join = root.join(ChallengeActivity_.performedBy);
        criteria.select(join).where(cb.equal(root.get(ChallengeActivity_.campaign), campaign));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * This will return all contributors from given/all challenges
     *
     * @return
     */
    public List<User> getChallengesContributorsWithInnovationStatusOderedByAscNickName(
            InnovationStatus innovationStatus, Collection<Campaign> challenges) {
        logger
                .infof(
                        "loading contributors for all challenges with given innovation status %d phase ordered by ascending users' nick name...",
                        innovationStatus);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        Join<ChallengeActivity, User> join = root.join(ChallengeActivity_.performedBy);
        Join<ChallengeActivity, Campaign> joinCampaign = root.join(ChallengeActivity_.campaign);

        List<Predicate> predicates = new ArrayList<Predicate>();

        if (challenges != null && !challenges.isEmpty()) {
            predicates.add(joinCampaign.in(challenges));
        }

        if (innovationStatus != null) {
            predicates.add(cb.equal(joinCampaign.get(Campaign_.innovationStatus), innovationStatus));
        }

        // for all/selected challenges
        predicates.add(cb.notEqual(root.get(ChallengeActivity_.activityType), ActivityType.CHALLENGE_LIKE));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.select(join).orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will reurn all contributors from all ideas
     *
     * @return
     */
    public List<User> getAllIdeasContributorsByAscNickName(Collection<Idea> ideas) {
        logger.infof("loading contributors for given ideas ordered by ascending users' nick name...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, User> join = root.join(IdeaActivity_.performedBy);

        Join<IdeaActivity, Idea> joinIdea = root.join(IdeaActivity_.idea);
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (ideas != null && !ideas.isEmpty()) {
            predicates.add(cb.and(joinIdea.in(ideas)));
        }

        predicates.add(cb.and(cb.notEqual(root.get(IdeaActivity_.activityType), ActivityType.IDEA_LIKE),
                cb.notEqual(root.get(IdeaActivity_.activityType), ActivityType.IDEA_VOTE)));

        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }
        criteria.select(join).orderBy(cb.asc(join.get(User_.nickName)));

        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will return all contributors from all actions
     *
     * @return
     */
    public List<User> getActionsContributorsOderedByAscNickName(Collection<Action> actions) {
        logger.infof("loading contributors for all actions ordered by ascending users' nick name...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        Join<ActionActivity, User> join = root.join(ActionActivity_.performedBy);
        Join<ActionActivity, Action> joinAction = root.join(ActionActivity_.action);
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (actions != null && !actions.isEmpty()) {
            predicates.add(joinAction.in(actions));
        }

        predicates.add(cb.and(cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.ACTION_LIKE),
                cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.ITERATION_LIKE),
                cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.BUSINESS_MODEL_LIKE)));

        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.select(join).orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    public int countAllChallengeContributorsByCampaign(Campaign campaign) {
        logger.infof("counting contributors by given campaign id %d...", campaign.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        Join<ChallengeActivity, User> join = root.join(ChallengeActivity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.and(cb.equal(root.get(ChallengeActivity_.campaign), campaign),
                cb.notEqual(root.get(ChallengeActivity_.activityType), ActivityType.CHALLENGE_LIKE)));

        if (!predicates.isEmpty()) {
            criteria.select(join).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * @return
     */
    public List<IdeaActivity> getAllIdeaActivitiesByDescendingCreationDateAndIdea(Idea idea, int first, int count) {
        logger.infof("loading %d activities starting from %d ordered by descending activity creation date ...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<IdeaActivity> criteria = cb.createQuery(IdeaActivity.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        criteria.where(cb.equal(root.get(IdeaActivity_.idea), idea));
        criteria.orderBy(cb.desc(root.get(IdeaActivity_.performedAt)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param idea
     * @return
     */
    public int countAllIdeaActivitiesByIdea(Idea idea) {
        logger.infof("counting all activities following given idea %s ...,", idea);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<IdeaActivity> criteria = cb.createQuery(IdeaActivity.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        criteria.where(cb.equal(root.get(IdeaActivity_.idea), idea));
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * idea
     *
     * @return
     */
    public List<User> getAllIdeaActivityUsersByAscNickNameAndByIdea(Idea idea, int first, int count) {
        logger.infof("loading %d IdeaActivity Users starting from %d..", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, User> join = root.join(IdeaActivity_.performedBy);

        criteria.select(join).where(cb.equal(root.get(IdeaActivity_.idea), idea));
        criteria.orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * idea
     *
     * @return
     */
    public int countAllIdeaActivityUsersByIdea(Idea idea) {
        logger.infof("counting IdeaActivity Users by given Idea...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, User> join = root.join(IdeaActivity_.performedBy);
        criteria.select(join).where(cb.equal(root.get(IdeaActivity_.idea), idea));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    public List<User> getAllIdeaActivityUsersByAscNickNameAndByIdea(Idea idea) {
        logger.infof("loading all IdeaActivity Users..");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, User> join = root.join(IdeaActivity_.performedBy);

        criteria.select(join).where(cb.equal(root.get(IdeaActivity_.idea), idea));
        criteria.orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * At the moment Idea contributors are: Ideas' comment owners and Idea voters
     *
     * @return
     */
    public int countAllContributorsByIdea(Idea idea) {
        logger.infof("counting contributors by idea with id %d...", idea.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, User> join = root.join(IdeaActivity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.and(cb.equal(root.get(IdeaActivity_.idea), idea),
                cb.and(cb.notEqual(root.get(IdeaActivity_.activityType), ActivityType.IDEA_LIKE)),
                cb.notEqual(root.get(IdeaActivity_.activityType), ActivityType.IDEA_VOTE)));

        if (!predicates.isEmpty()) {
            criteria.select(join).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            criteria.orderBy(cb.asc(join.get(User_.nickName)));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * At the moment Idea contributors are: Ideas' comment owners and Idea voters
     *
     * @return
     */
    public List<User> getAllContributorsByIdea(Idea idea, int first, int count) {
        logger.infof("loading %d contributors starting from %d for idea given by id %d...", count, first, idea.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, User> join = root.join(IdeaActivity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.and(cb.equal(root.get(IdeaActivity_.idea), idea),
                cb.and(cb.notEqual(root.get(IdeaActivity_.activityType), ActivityType.IDEA_LIKE)),
                cb.notEqual(root.get(IdeaActivity_.activityType), ActivityType.IDEA_VOTE)));

        if (!predicates.isEmpty()) {
            criteria.select(join).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            criteria.orderBy(cb.asc(join.get(User_.nickName)));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public List<ActionActivity> getAllActionActivitiesByDescendingCreationDateAndAction(Action action, int first, int count) {
        logger.infof("loading %d activities starting from %d ordered by descending Creation date ...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActionActivity> criteria = cb.createQuery(ActionActivity.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        criteria.where(cb.equal(root.get(ActionActivity_.action), action));
        criteria.orderBy(cb.desc(root.get(ActionActivity_.performedAt)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @param action
     * @return
     */
    public int countAllActionActivitiesByAction(Action action) {
        logger.infof("counting all activities following given action %s ...,", action);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActionActivity> criteria = cb.createQuery(ActionActivity.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        criteria.where(cb.equal(root.get(ActionActivity_.action), action));
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * @return
     */
    public List<User> getAllActionContributorsBasedOnActionActivity(Action action, int first, int count) {
        logger.infof("loading %d contributors starting from %d for given action with id %d ...", count, first, action.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        Join<ActionActivity, User> join = root.join(ActionActivity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(ActionActivity_.action), action));

        predicates.add(cb.and(cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.ACTION_LIKE),
                cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.ITERATION_LIKE),
                cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.BUSINESS_MODEL_LIKE)));

        if (!predicates.isEmpty()) {
            criteria.select(join).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            criteria.orderBy(cb.asc(join.get(User_.nickName)));
        }

        criteria.distinct(true);
        if (first != Integer.MAX_VALUE && count != Integer.MAX_VALUE) {
            return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
        }
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    public int countAllActionContributorsBasedOnActionActivity(Action action) {
        logger.infof("counting Users by given action with id %d...", action.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        Join<ActionActivity, User> join = root.join(ActionActivity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(ActionActivity_.action), action));

        predicates.add(cb.and(cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.ACTION_LIKE),
                cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.ITERATION_LIKE),
                cb.notEqual(root.get(ActionActivity_.activityType), ActivityType.BUSINESS_MODEL_LIKE)));

        if (!predicates.isEmpty()) {
            criteria.select(join).where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            criteria.orderBy(cb.asc(join.get(User_.nickName)));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * @return
     */
    public List<Campaign> getAllChallengesByChallengeActivityCreator(User user, int first, int count, EntitiySortingCriteria sortingCriteria) {
        logger.infof("loading %d Challenges starting from %d given by challenge activity creator with id %d ...", count, first,
                user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        Join<ChallengeActivity, Campaign> join = root.join(ChallengeActivity_.campaign);
        criteria.select(join).where(cb.equal(root.get(ChallengeActivity_.performedBy), user));
        if (sortingCriteria.equals(EntitiySortingCriteria.created)) {
            criteria.orderBy(cb.desc(join.get(Campaign_.createdOn)));
        } else {
            criteria.orderBy(cb.desc(join.get(Campaign_.lastModified)));
        }
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public int countAllChallengesByChallengeActivityCreator(User user) {
        logger.infof("counting all Challenges by challenge activity creator with id %d ...", user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Campaign> criteria = cb.createQuery(Campaign.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        Join<ChallengeActivity, Campaign> join = root.join(ChallengeActivity_.campaign);
        criteria.select(join).where(cb.equal(root.get(ChallengeActivity_.performedBy), user));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }


    public long countChallengeActivities() {
        logger.infof("counting challenge Activities...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @return
     */
    public List<Campaign> getActiveChallengesByLatestChallengeActivity(int first, int count) {
        logger.infof("loading most active challenges by latest challenge activity...");
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Campaign> query = builder.createQuery(Campaign.class);
        final Root<ChallengeActivity> from = query.from(ChallengeActivity.class);
        query.groupBy(from.get(ChallengeActivity_.campaign));
        query.orderBy(builder.desc(builder.greatest(from.get(ChallengeActivity_.performedAt))));
        query.select(from.get(ChallengeActivity_.campaign));
        return em.createQuery(query).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public long countActiveChallengesByLatestChallengeActivity() {
        logger.infof("counting most active challenges by latest challenge activity...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<ChallengeActivity> root = criteria.from(ChallengeActivity.class);
        criteria.select(cb.countDistinct(root.get(ChallengeActivity_.campaign)));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @return
     */
    public List<Idea> getActiveIdeasByLatestIdeaActivity(int first, int count) {
        logger.infof("loading most active ideas by latest idea activity...");
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Idea> query = builder.createQuery(Idea.class);
        final Root<IdeaActivity> from = query.from(IdeaActivity.class);
        query.groupBy(from.get(IdeaActivity_.idea));
        query.orderBy(builder.desc(builder.greatest(from.get(IdeaActivity_.performedAt))));
        query.select(from.get(IdeaActivity_.idea));
        return em.createQuery(query).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public long countActiveIdeasByLatestIdeaActivity() {
        logger.infof("counting most active ideas by latest idea activity...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        criteria.select(cb.countDistinct(root.get(IdeaActivity_.idea)));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @return
     */
    public List<Action> getActiveActionsByLatestActionActivity(int first, int count) {
        logger.infof("loading most active actions by latest action activity...");
        final CriteriaBuilder builder = em.getCriteriaBuilder();
        final CriteriaQuery<Action> query = builder.createQuery(Action.class);
        final Root<ActionActivity> from = query.from(ActionActivity.class);
        query.groupBy(from.get(ActionActivity_.action));
        query.orderBy(builder.desc(builder.greatest(from.get(IdeaActivity_.performedAt))));
        query.select(from.get(ActionActivity_.action));
        return em.createQuery(query).setFirstResult(first).setMaxResults(count).getResultList();
    }


    /**
     * @return
     */
    public long countActiveActionsByLatestActionActivity() {
        logger.infof("counting most active actions by latest action activity...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        criteria.select(cb.countDistinct(root.get(ActionActivity_.action)));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @return
     */
    public List<Idea> getAllIdeasByIdeaActivityCreator(User user, int first, int count, EntitiySortingCriteria sortingCriteria) {
        logger.infof("loading %d Ideas starting from %d given by idea activity creator with id %d ...", count, first,
                user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, Idea> join = root.join(IdeaActivity_.idea);
        criteria.select(join).where(cb.equal(root.get(IdeaActivity_.performedBy), user));

        if (sortingCriteria.equals(EntitiySortingCriteria.created)) {
            criteria.orderBy(cb.desc(join.get(Idea_.postedAt)));
        } else {
            criteria.orderBy(cb.desc(join.get(Idea_.lastModified)));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public int countAllIdeasByIdeaActivityCreator(User user) {
        logger.infof("counting all ideas by idea activity creator with id %d ...", user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Idea> criteria = cb.createQuery(Idea.class);
        Root<IdeaActivity> root = criteria.from(IdeaActivity.class);
        Join<IdeaActivity, Idea> join = root.join(IdeaActivity_.idea);
        criteria.select(join).where(cb.equal(root.get(IdeaActivity_.performedBy), user));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * @return
     */
    public List<Action> getAllActionsByActionActivityCreator(User user, int first, int count, EntitiySortingCriteria sortingCriteria) {
        logger.infof("loading %d Actions starting from %d given by action activity creator with id %d ...", count, first,
                user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        Join<ActionActivity, Action> join = root.join(ActionActivity_.action);
        criteria.select(join).where(cb.equal(root.get(ActionActivity_.performedBy), user));
        if (sortingCriteria.equals(EntitiySortingCriteria.created)) {
            criteria.orderBy(cb.desc(join.get(Action_.postedAt)));
        } else {
            criteria.orderBy(cb.desc(join.get(Action_.lastModified)));
        }
        criteria.distinct(true);
        if (first != Integer.MAX_VALUE && count != Integer.MAX_VALUE) {
            return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
        }
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    public int countAllActionsByActionActivityCreator(User user) {
        logger.infof("counting all actions by action activity creator with id %d ...", user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<ActionActivity> root = criteria.from(ActionActivity.class);
        Join<ActionActivity, Action> join = root.join(ActionActivity_.action);
        criteria.select(join).where(cb.equal(root.get(ActionActivity_.performedBy), user));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    public int countTotalActivityPerformedByUser(User user) {
        int returnVal = 0;
        returnVal += countAllChallengesByChallengeActivityCreator(user);

        returnVal += countAllIdeasByIdeaActivityCreator(user);

        returnVal += countAllActionsByActionActivityCreator(user);

        return returnVal;
    }

    /**
     * @return
     */
    public List<Activity> getAllActivitiesByActivityType(List<ActivityType> activityTypes, Date startDate, Date endDate, String first, String count) {
        logger.infof("loading %s activities starting from %s by given activity types %s which is between %s and %S...", count,
                first, activityTypes, startDate, endDate);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Activity> criteria = cb.createQuery(Activity.class);
        Root<Activity> root = criteria.from(Activity.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (!activityTypes.isEmpty()) {
            for (ActivityType activityType : activityTypes) {
                predicates.add(cb.equal(root.get(Activity_.activityType), activityType));
            }

            if (!predicates.isEmpty()) {

                // check for dates
                if (startDate != null && endDate != null) {
                    criteria.where(cb.and(cb.or(predicates.toArray(new Predicate[predicates.size()])),
                            cb.between(root.get(Activity_.performedAt), startDate, endDate)));
                } else {
                    criteria.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
                }
                criteria.orderBy(cb.desc(root.get(Activity_.performedAt)));
            }

            criteria.distinct(true);
            if (first != null && count != null) {
                return em.createQuery(criteria).setFirstResult(Integer.parseInt(first)).setMaxResults(Integer.parseInt(count))
                        .getResultList();
            } else {
                return em.createQuery(criteria).getResultList();
            }

        }
        return null;
    }

    /**
     * @return
     */
    public int countAllActivitiesByActivityType(List<ActivityType> activityTypes, Date startDate, Date endDate) {
        logger.infof("counting all activities by given activity types %s which is between %s and %s...", activityTypes,
                startDate, endDate);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Activity> criteria = cb.createQuery(Activity.class);
        Root<Activity> root = criteria.from(Activity.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (!activityTypes.isEmpty()) {
            for (ActivityType activityType : activityTypes) {
                predicates.add(cb.equal(root.get(Activity_.activityType), activityType));
            }

            if (!predicates.isEmpty()) {

                // check for dates
                if (startDate != null && endDate != null) {
                    criteria.where(cb.and(cb.or(predicates.toArray(new Predicate[predicates.size()])),
                            cb.between(root.get(Activity_.performedAt), startDate, endDate)));
                } else {
                    criteria.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
                }
                criteria.orderBy(cb.desc(root.get(Activity_.performedAt)));
            }
            criteria.distinct(true);
            return em.createQuery(criteria).getResultList().size();
        }
        return 0;
    }

    /**
     * @return
     */
    public int countAllActivitiesPerformedByUser(User performedBy) {
        logger.infof("counting all activities performed by given user with id %d...", performedBy.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Activity> criteria = cb.createQuery(Activity.class);
        Root<Activity> root = criteria.from(Activity.class);
        criteria.where(cb.equal(root.get(Activity_.performedBy), performedBy));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * This will return list of all contributors from all processes(challenges, ideas, actions)
     * Currently, liking an entity or voting an idea is not considered as contribution
     *
     * @return
     */
    public List<User> getAllContributorsFromAllProcesses(String first, String count, Date startDate, Date endDate) {
        logger.infof("loading %s contributors starting from %s which are registered between %s and %s for all processes...",
                count, first, startDate, endDate);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        // for all processes
        Root<Activity> rootActivities = criteria.from(Activity.class);
        criteria.select(rootActivities.get(Activity_.performedBy)).where(
                cb.and(cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.CHALLENGE_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.IDEA_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.ACTION_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.ITERATION_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.BUSINESS_MODEL_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.IDEA_VOTE)));

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

    /**
     * This will return total no of all contributors from all processes(challenges, ideas, actions) registered between given two
     * dates
     * <p>
     * Currently, liking an entity or voting an idea is not considered as contribution
     *
     * @return
     */
    public int countAllContributorsFromAllProcesses(Date startDate, Date endDate) {
        logger.infof("counting all contributors which are registered between %s and %s for all processes...", startDate,
                endDate);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> root = criteria.from(User.class);

        // for all processes
        Root<Activity> rootActivities = criteria.from(Activity.class);
        criteria.select(rootActivities.get(Activity_.performedBy)).where(
                cb.and(cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.CHALLENGE_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.IDEA_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.ACTION_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.ITERATION_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.BUSINESS_MODEL_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.IDEA_VOTE)));

        // check for dates
        if (startDate != null && endDate != null) {
            criteria.where(cb.between(root.get(User_.registrationDate), startDate, endDate));
        }
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    /**
     * This will return list of all contributors from all processes(challenges, ideas, actions)
     *
     * @return
     */
    public List<User> getAllContributorsFromAllProcesses() {
        logger.infof("loading all contributors for all processes...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);

        // for all processes
        Root<Activity> rootActivities = criteria.from(Activity.class);
        criteria.select(rootActivities.get(Activity_.performedBy)).where(
                cb.and(cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.CHALLENGE_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.IDEA_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.ACTION_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.ITERATION_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.BUSINESS_MODEL_LIKE),
                        cb.notEqual(rootActivities.get(Activity_.activityType), ActivityType.IDEA_VOTE)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will return list of activity creators from given activities
     *
     * @return
     */
    public List<User> getAllActivityCreatorsFromGivenActivities(List<Activity> activities) {
        logger.infof("loading all activity creators for given activites %s ...", activities);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);

        Root<Activity> root = criteria.from(Activity.class);
        Join<Activity, User> join = root.join(Activity_.performedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if (activities != null && !activities.isEmpty()) {
            // get all activity leaders manually
            List<User> activityLeader = new LinkedList<>();
            for (Activity act : activities) {
                activityLeader.add(act.getPerformedBy());
            }

            // find only those who are part of activity leaders list
            predicates.add(join.in(activityLeader));

            if (!predicates.isEmpty()) {
                criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
            }

            criteria.select(join);
            criteria.distinct(true);
            return em.createQuery(criteria).getResultList();
        }

        return new LinkedList<>();
    }

    /**
     * @param
     * @return total no of Likes received by challenge, idea, action, action iteration and business model
     */
    public long getTotalNoOfLikesForAllProcesses() {
        long returnVal = 0;
        returnVal += campaignService.countTotalLikesForAllChallenges();

        returnVal += ideaService.countTotalLikesForAllIdeas();

        returnVal += actionService.countTotalLikesForAllActions();

        returnVal += actionIterationService.countTotalLikesForAllActionIterations();

        returnVal += actionBusinessModelService.countTotalLikesForAllActionBusinessModels();

        return returnVal;
    }

}

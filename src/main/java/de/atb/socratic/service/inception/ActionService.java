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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.ActionIteration_;
import de.atb.socratic.model.Action_;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Comment_;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.model.User_;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import org.hibernate.search.query.dsl.PhraseContext;
import org.hibernate.search.query.dsl.PhraseMatchingContext;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.WildcardContext;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Stateless
public class ActionService extends AbstractService<Action> {

    private static final long serialVersionUID = -6777330638891953695L;

    @Inject
    Event<Action> actionEventSrc;

    @EJB
    UserService userService;

    @EJB
    CommentService commentService;

    @Inject
    ParticipateNotificationService participateNotifier;

    @Inject
    Logger logger;

    public ActionService() {
        super(Action.class);
    }

    public Action createOrUpdate(Action action) {
        action = em.merge(action);
        return action;
    }

    @Override
    public Action create(Action action) {
        action = createOrUpdate(action);
        logger.infof("persisted action '%s' ...", action);

        // update counter noOfActionsLeads for idea leader
        userService.increaseNoOfActionsLeads(action.getPostedBy());

        return action;
    }

    public List<Action> getAllActionsByDescendingCreationDateAndPostedBy(int first, int count, User postedBy, EntitiySortingCriteria sortingCriteria) {
        logger.infof("loading %d actions starting from %d ordered by ascending Creation date and postedBy %d...", count, first,
                postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<Action> root = criteria.from(Action.class);
        // criteria.where(root.get(Campaign_.id).in(ids));
        criteria.where(cb.equal(root.get(Action_.postedBy), postedBy));
        if (sortingCriteria.equals(EntitiySortingCriteria.created)) {
            criteria.orderBy(cb.desc(root.get(Action_.postedAt)));
        } else {
            criteria.orderBy(cb.desc(root.get(Action_.lastModified)));
        }
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public List<Action> getAllActionsByDescendingCreationDateAndCommentedBy(int first, int count, User commentedBy) {
        logger.infof("loading %d actions starting from %d ordered by ascending Creation date and commentedBy %d...", count,
                first, commentedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> queryCommentedBy = cb.createQuery(Action.class);
        Root<Action> root = queryCommentedBy.from(Action.class);
        Join<Action, Comment> comments = root.join(Action_.comments);
        Join<Comment, User> postedBy = comments.join(Comment_.postedBy);
        queryCommentedBy.where(cb.equal(postedBy, commentedBy));
        queryCommentedBy.distinct(true);
        return em.createQuery(queryCommentedBy).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public long countActionsForUser(User postedBy) {
        logger.infof("counting all actions for user %d ...", postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Action> root = criteria.from(Action.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Action_.deleted)));
        if (postedBy != null) {
            predicates.add(cb.equal(root.get(Action_.postedBy), postedBy));
        }

        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.select(root.get(Action_.id));
        return em.createQuery(criteria).getResultList().size();
    }

    public long getCountForAllActionsByDescendingCreationDateAndCommentedBy(User commentedBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> queryCommentedBy = cb.createQuery(Action.class);
        Root<Action> root = queryCommentedBy.from(Action.class);
        Join<Action, Comment> comments = root.join(Action_.comments);
        Join<Comment, User> postedBy = comments.join(Comment_.postedBy);
        queryCommentedBy.where(cb.equal(postedBy, commentedBy));
        queryCommentedBy.distinct(true);
        return em.createQuery(queryCommentedBy).getResultList().size();
    }

    /**
     * @return
     */
    public List<User> getAllTeamMembersByAscFirstNameAndByAction(Action action, int first, int count) {
        logger.infof("loading %d team members starting from %d based on Action...", count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, User> join = root.join(Action_.teamMembers);
        criteria.select(join).where(cb.equal(root.get(Action_.id), action.getId()));
        criteria.orderBy(cb.asc(join.get(User_.firstName)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public List<User> getAllTeamMembersByAscFirstNameAndByAction(Action action) {
        logger.infof("loading all team members for given Action with id %d...", action.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, User> join = root.join(Action_.teamMembers);
        criteria.select(join).where(cb.equal(root.get(Action_.id), action.getId()));
        criteria.orderBy(cb.asc(join.get(User_.firstName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    public long countTeamMembersByAscFirstNameAndByAction(Action action) {
        logger.infof("counting %d team members starting from %d based on Action...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, User> join = root.join(Action_.teamMembers);
        criteria.select(join).where(cb.equal(root.get(Action_.id), action.getId()));
        criteria.orderBy(cb.asc(join.get(User_.firstName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    public Action deleteTeamMemberFromList(Action action, User member) {
        if (action.getTeamMembers().contains(member)) {
            action.getTeamMembers().remove(member);
            action = update(action);
        }
        logger.infof("member %s is removed from action %s ...", member, action);
        return action;
    }

    public Action inviteUserToBeTeamMember(Action action, User potentialMember) {
        if (!action.getInvitedTeamMembers().contains(potentialMember)) {
            // send notification inviting potentialMember to team
            participateNotifier.addParticipationNotification(action, potentialMember, NotificationType.ACTION_TEAM_MEMBER_INVITATION);

            action.getInvitedTeamMembers().add(potentialMember);
            action = update(action);
            logger.infof("Potential member %s is added to Invited Team Members List for action %s ...", potentialMember, action);
        }
        return action;
    }

    public Action addTeamMemberToList(Action action, User member) {
        if (!action.getTeamMembers().contains(member)) {
            action.getTeamMembers().add(member);
            action = update(action);
            logger.infof("member %s is added to action %s ...", member, action);
        }
        return action;
    }

    public Action addActionIterationToList(Action action, ActionIteration actionIteration) {
        if (!action.getActionIterations().contains(actionIteration)) {
            action.getActionIterations().add(actionIteration);
            action = update(action);
            logger.infof("member %s is added to action %s ...", actionIteration, action);
        }

        return action;
    }

    /**
     * @param actionId
     * @param comment
     */
    public void removeComment(Long actionId, Comment comment) {
        Action action = getById(actionId);
        removeCommentFromAction(action, comment);
        logger.infof("removed comment %s from action with ID %d ...", comment, actionId);
    }

    /**
     * @param action
     * @param comment
     */
    private void removeCommentFromAction(Action action, Comment comment) {
        comment = em.merge(comment);
        action.removeComment(comment);

        // once comment is removed/deleted, reduce NoOfCommentsPosted for given user
        userService.decreaseNoOfCommentsPosted(comment.getPostedBy());
        em.merge(action);
    }

    /**
     * @param action
     * @param comment
     * @return
     */
    public Comment addComment(Action action, Comment comment) {
        action = em.merge(action);
        comment = addCommentToAction(action, comment);
        logger.infof("added comment %s to action %s ...", comment, action);
        return comment;
    }

    /**
     * @param action
     * @param comment
     * @return
     */
    private Comment addCommentToAction(Action action, Comment comment) {
        comment = commentService.create(comment);
        action.addComment(comment);
        em.merge(action);
        return comment;
    }

    /**
     * @param action
     * @param userId
     * @return
     */
    public Action voteUp(Action action, Long userId) {
        // remove user id from up votes if present
        // if not present (= remove returns false), add to up votes
        if (!action.getUpVotes().remove(userId)) {
            action.getUpVotes().add(userId);
        }
        return update(action);
    }

    public List<ActionIteration> getAllActionIterationsByDescendingCreationDate(Long actionId, int first, int count) {
        logger.infof("loading %d iterations starting from %d ordered by descending Creation date with actionId %d...", count,
                first, actionId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActionIteration> criteria = cb.createQuery(ActionIteration.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, ActionIteration> join = root.join(Action_.actionIterations);
        criteria.select(join).where(cb.equal(root.get(Action_.id), actionId))
                .orderBy(cb.desc(join.get(ActionIteration_.postedAt)));
        criteria.distinct(true);
        if (first != Integer.MAX_VALUE && count != Integer.MAX_VALUE) {
            return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
        }
        return em.createQuery(criteria).getResultList();
    }

    public int countAllActionIterationsByDescendingCreationDate(Long actionId) {
        logger.infof("counting all iterations with actionId %d...", actionId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActionIteration> criteria = cb.createQuery(ActionIteration.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, ActionIteration> join = root.join(Action_.actionIterations);
        criteria.select(join).where(cb.equal(root.get(Action_.id), actionId))
                .orderBy(cb.desc(join.get(ActionIteration_.postedAt)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    public List<Action> getAllActionsByDescendingCreationDate(int first, int count) {
        logger.infof("loading %d actions starting from %d ordered by ascending Creation date...", count,
                first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<Action> root = criteria.from(Action.class);
        criteria.orderBy(cb.desc(root.get(Action_.postedAt)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    public int countAllActionsByDescendingCreationDate() {
        logger.infof("counting all actions ordered by ascending Creation date...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<Action> root = criteria.from(Action.class);
        criteria.orderBy(cb.desc(root.get(Action_.postedAt)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    public ActionIteration getLatestIterationOfAction(Long actionId) {
        logger.infof("getting iteration ordered by descending Creation date with actionId %d...", actionId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActionIteration> criteria = cb.createQuery(ActionIteration.class);
        Root<ActionIteration> root = criteria.from(ActionIteration.class);
        final Join<ActionIteration, Action> actionJoin = root.join(ActionIteration_.theAction);
        criteria.where(cb.equal(actionJoin.get(Action_.id), actionId));
        criteria.orderBy(cb.desc(root.get(ActionIteration_.postedAt)));
        final List<ActionIteration> resultList = em.createQuery(criteria).setMaxResults(1).getResultList();
        return (resultList.isEmpty()) ? null : resultList.get(0);
    }

    public Action getActionFromBusinessModel(BusinessModel businessModel) {
        logger.infof("getting action from given business model with id %d...", businessModel.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<Action> root = criteria.from(Action.class);
        criteria.where(cb.equal(root.get(Action_.businessModel), businessModel));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param action
     * @param user
     * @return
     */
    public long countCommentsForActionByUser(Action action, User user) {
        logger.infof("counting all comments for action %s postedBy user %s...", action, user);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, Comment> join = root.join(Action_.comments);

        Join<Comment, User> postedBy = join.join(Comment_.postedBy);

        criteria.select(cb.countDistinct(join))
                .where(cb.and(cb.equal(postedBy, user),
                        cb.equal(root.get(Action_.id), action.getId())));

        return em.createQuery(criteria).getSingleResult();
    }

    public int getIterationNumber(Long actionId, ActionIteration iteration) {
        logger
                .infof(
                        "getting position of Iteration with id %d from Action's Iteratin list with id %d ordered by ascending Creation date ...",
                        iteration.getId(), actionId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActionIteration> criteria = cb.createQuery(ActionIteration.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, ActionIteration> join = root.join(Action_.actionIterations);
        criteria.select(join).where(cb.equal(root.get(Action_.id), actionId))
                .orderBy(cb.asc(join.get(ActionIteration_.postedAt)));

        criteria.distinct(true);
        List<ActionIteration> iterations = em.createQuery(criteria).getResultList();

        int index = 1;
        for (ActionIteration itr : iterations) {
            if (itr.equals(iteration)) {
                return index;
            }
            index++;
        }
        return index;
    }

    public long countTotalLikesForActionCreatedByUser(User user) {
        logger.infof("counting all likes for action postedBy user %d ...",
                user);
        long returnVal = 0;
        for (Action action : getAllActionsCreatedByUser(user)) {
            returnVal += action.getNoOfUpVotes();
        }

        return returnVal;
    }

    /**
     * This method counts likes given by user for all actions.
     *
     * @param user
     * @return
     */
    public long countTotalLikesGivenByUserForAllActions(User user) {
        logger.infof("counting all likes by given user %s for all ideas  ...", user);

        long returnVal = 0;
        for (Action action : getAll()) {
            if (action.getUpVotes().contains(user.getId())) {
                returnVal += 1;
            }
        }

        return returnVal;
    }

    /**
     * This method counts likes for all actions.
     *
     * @param
     * @return
     */
    public long countTotalLikesForAllActions() {
        logger.infof("counting all likes for all actions...");

        long returnVal = 0;
        for (Action action : getAll()) {
            returnVal += action.getNoOfUpVotes();
        }

        return returnVal;
    }

    public List<Action> getAllActionsCreatedByUser(User postedBy) {
        logger.infof("getting all actions postedBy user %d ...", postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<Action> root = criteria.from(Action.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Action_.deleted)));
        if (postedBy != null) {
            predicates.add(cb.equal(root.get(Action_.postedBy), postedBy));
        }
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will return all leaders from all Actions
     *
     * @return
     */
    public List<User> getAllActionLeadersByAscNickName(Collection<Action> actions) {
        logger.infof("loading leaders for all actions ordered by ascending users' nick name...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, User> join = root.join(Action_.postedBy);

        List<Predicate> predicates = new ArrayList<Predicate>();

        if (actions != null && !actions.isEmpty()) {
            predicates.add(root.in(actions));
        }

        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.select(join).orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * This will return all leaders from all actions
     *
     * @return
     */
    public List<User> getAllActionLeadersByAscNickName() {
        logger.infof("loading leaders for all actions ordered by ascending users' nick name...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Action> root = criteria.from(Action.class);
        Join<Action, User> join = root.join(Action_.postedBy);
        criteria.select(join);
        criteria.orderBy(cb.asc(join.get(User_.nickName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    public void notifyActionFollowersAboutActionUpdates(Action action) {
        // send action follower notification that action is updated.
        Collection<Action> actions = new LinkedList<>();
        actions.add(action);
        List<User> followers = userService.getAllUsersByFollowingGivenActions(actions);
        for (User follower : followers) {
            participateNotifier.addParticipationNotification(action, follower, NotificationType.ACTION_UPDATE);
        }
    }

    public void notifyActionFollowersAboutActionCommentsAndLikes(Action theAction) {
        // at first check if total comments and likes exceeds certain threshold, ie in multiple of 10.
        int totalComments = 0, totalLikes = 0;
        int threshold;
        if (!theAction.getComments().isEmpty() && theAction.getComments() != null) {
            totalComments = theAction.getComments().size();
        }
        if (!theAction.getUpVotes().isEmpty() && theAction.getUpVotes() != null) {
            totalLikes = theAction.getNoOfUpVotes();
        }

        if (totalComments != 0 || totalLikes != 0) {
            threshold = (totalComments + totalLikes) % 10;
            if (threshold == 0) {
                Collection<Action> actions = new LinkedList<>();
                actions.add(theAction);
                List<User> actionFollowers = userService.getAllUsersByFollowingGivenActions(actions);
                for (User user : actionFollowers) {
                    participateNotifier.addParticipationNotification(theAction, user, NotificationType.ACTION_COMMENTS_LIKES);
                }
            }
        }
    }

    /**
     * this method will return list of actions which has given tag either as skill or keywords
     *
     * @param tags
     * @return
     */
    public List<Action> getAllActionsByTag(Set<Tag> tags) {
        logger.infof("loading actions by given tags...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Action> criteria = cb.createQuery(Action.class);
        Root<Action> root = criteria.from(Action.class);

        List<Predicate> predicates = new ArrayList<Predicate>();

        for (Tag tag : tags) {
            predicates.add(cb.isMember(tag, root.get(Action_.keywords)));
            predicates.add(cb.isMember(tag, root.get(Action_.skills)));
        }

        if (!predicates.isEmpty()) {
            criteria.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
        }

        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    @Override
    protected TermMatchingContext setFullTextSearchFields(WildcardContext wc) {
        return wc.onField("shortText")
                .andField("description")
                .andField("elevatorPitch")
                .andField("beneficiaries")
                .andField("valueForBeneficiaries")
                .andField("impactStakeholders")
                .andField("resourcesForActionImplementation")
                .andField("implementationPlan")
                .andField("location")
                .andField("reasonForBringingActionForward")
                .andField("relatedInnovations")
                .andField("postedBy.firstName")
                .andField("postedBy.lastName")
                .andField("postedBy.nickName")
                .andField("postedBy.email")             // does not work
                .andField("keywords")
                .andField("skills")
                .andField("comments.commentText")
                .andField("comments.postedBy.firstName")
                .andField("comments.postedBy.lastName")
                .andField("comments.postedBy.nickName")
                .andField("comments.postedBy.email")
                .andField("callToAction");   // does not work
    }

    @Override
    protected PhraseMatchingContext setFullTextSearchFields(PhraseContext wc) {
        return wc.onField("shortText")
                .andField("description")
                .andField("elevatorPitch")
                .andField("beneficiaries")
                .andField("valueForBeneficiaries")
                .andField("impactStakeholders")
                .andField("resourcesForActionImplementation")
                .andField("implementationPlan")
                .andField("location")
                .andField("reasonForBringingActionForward")
                .andField("relatedInnovations")
                .andField("postedBy.firstName")
                .andField("postedBy.lastName")
                .andField("postedBy.nickName")
                .andField("postedBy.email")             // does not work
                .andField("keywords")
                .andField("skills")
                .andField("comments.commentText")
                .andField("comments.postedBy.firstName")
                .andField("comments.postedBy.lastName")
                .andField("comments.postedBy.nickName")
                .andField("comments.postedBy.email")
                .andField("callToAction");   // does not work
    }
}

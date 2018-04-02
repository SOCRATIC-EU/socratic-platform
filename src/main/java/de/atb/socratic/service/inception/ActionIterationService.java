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
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.ActionIteration_;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.User;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.user.UserService;

/**
 * @author ATB
 */
@Stateless
public class ActionIterationService extends AbstractService<ActionIteration> {

    private static final long serialVersionUID = -6777330638891953695L;

    @EJB
    UserService userService;

    @EJB
    CommentService commentService;

    public ActionIterationService() {
        super(ActionIteration.class);
    }

    /**
     * @param iterationId
     * @param comment
     */
    public void removeComment(Long iterationId, Comment comment) {
        ActionIteration actionIteration = getById(iterationId);
        removeCommentFromAction(actionIteration, comment);
        logger.infof("removed comment %s from action with ID %d ...", comment, iterationId);
    }

    /**
     * @param iteration
     * @param comment
     */
    private void removeCommentFromAction(ActionIteration iteration, Comment comment) {
        comment = em.merge(comment);
        iteration.removeComment(comment);

        // once comment is removed/deleted, reduce NoOfCommentsPosted for given user
        userService.decreaseNoOfCommentsPosted(comment.getPostedBy());
        em.merge(iteration);
    }

    /**
     * @param iterationId
     * @param comment
     * @return
     */
    public Comment addComment(Long iterationId, Comment comment) {
        ActionIteration iteration = getById(iterationId);
        comment = addCommentToAction(iteration, comment);
        logger.infof("added comment %s to action with ID %d ...", comment, iterationId);
        return comment;
    }

    /**
     * @param iteration
     * @param comment
     * @return
     */
    public Comment addComment(ActionIteration iteration, Comment comment) {
        iteration = em.merge(iteration);
        comment = addCommentToAction(iteration, comment);
        logger.infof("added comment %s to action %s ...", comment, iteration);
        return comment;
    }

    /**
     * @param iteration
     * @param comment
     * @return
     */
    private Comment addCommentToAction(ActionIteration iteration, Comment comment) {
        comment = commentService.create(comment);
        iteration.addComment(comment);
        em.merge(iteration);
        return comment;
    }

    /**
     * @param iteration
     * @param userId
     * @return
     */
    public ActionIteration voteUp(ActionIteration iteration, Long userId) {
        // remove user id from up votes if present
        // if not present (= remove returns false), add to up votes
        if (!iteration.getUpVotes().remove(userId)) {
            iteration.getUpVotes().add(userId);
        }
        return update(iteration);
    }

    public List<ActionIteration> getAllActionIterationsCreatedByUser(User postedBy) {
        logger.infof("getting all actions postedBy user %d ...", postedBy);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ActionIteration> criteria = cb.createQuery(ActionIteration.class);
        Root<ActionIteration> root = criteria.from(ActionIteration.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        //predicates.add(cb.not(root.get(ActionIteration_.deleted)));
        if (postedBy != null) {
            predicates.add(cb.equal(root.get(ActionIteration_.postedBy), postedBy));
        }
        criteria.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    public long countTotalLikesForActionIterationCreatedByUser(User user) {
        logger.infof("counting all likes for action iteration postedBy user %d ...",
                user);
        long returnVal = 0;
        for (ActionIteration actionIteration : getAllActionIterationsCreatedByUser(user)) {
            returnVal += actionIteration.getNoOfUpVotes();
        }

        return returnVal;
    }

    /**
     * This method counts likes given by user for all action iterations.
     *
     * @param user
     * @return
     */
    public long countTotalLikesGivenByUserForAllActionIterations(User user) {
        logger.infof("counting all likes by given user %s for all action iterations ...", user);

        long returnVal = 0;
        for (ActionIteration actionIteration : getAll()) {
            if (actionIteration.getUpVotes().contains(user.getId())) {
                returnVal += 1;
            }
        }

        return returnVal;
    }

    /**
     * This method counts likes for all actionIterations.
     *
     * @param
     * @return
     */
    public long countTotalLikesForAllActionIterations() {
        logger.infof("counting all likes for all actionIterations...");

        long returnVal = 0;
        for (ActionIteration actionIteration : getAll()) {
            returnVal += actionIteration.getNoOfUpVotes();
        }

        return returnVal;
    }
}

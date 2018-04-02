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

import javax.ejb.EJB;
import javax.ejb.Stateless;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.User;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.user.UserService;

/**
 * @author ATB
 */
@Stateless
public class ActionBusinessModelService extends AbstractService<BusinessModel> {

    @EJB
    ActionService actionService;

    @EJB
    UserService userService;

    @EJB
    CommentService commentService;

    private static final long serialVersionUID = -6777330638891953695L;

    public ActionBusinessModelService() {
        super(BusinessModel.class);
    }

    /**
     * @param businessModelId
     * @param comment
     */
    public void removeComment(Long businessModelId, Comment comment) {
        BusinessModel businessModel = getById(businessModelId);
        removeCommentFromBusinessModel(businessModel, comment);
        logger.infof("removed comment %s from businessModel with ID %d ...", comment, businessModelId);
    }

    /**
     * @param businessModel
     * @param comment
     */
    private void removeCommentFromBusinessModel(BusinessModel businessModel, Comment comment) {
        comment = em.merge(comment);
        businessModel.removeComment(comment);

        // once comment is removed/deleted, reduce NoOfCommentsPosted for given user
        userService.decreaseNoOfCommentsPosted(comment.getPostedBy());
        em.merge(businessModel);
    }

    /**
     * @param businessModelId
     * @param comment
     * @return
     */
    public Comment addComment(Long businessModelId, Comment comment) {
        BusinessModel businessModel = getById(businessModelId);
        comment = addCommentToBusinessModel(businessModel, comment);
        logger.infof("added comment %s to businessModel with ID %d ...", comment, businessModelId);
        return comment;
    }

    /**
     * @param businessModel
     * @param comment
     * @return
     */
    public Comment addComment(BusinessModel businessModel, Comment comment) {
        businessModel = em.merge(businessModel);
        comment = addCommentToBusinessModel(businessModel, comment);
        logger.infof("added comment %s to businessModel %s ...", comment, businessModel);
        return comment;
    }

    /**
     * @param businessModel
     * @param comment
     * @return
     */
    private Comment addCommentToBusinessModel(BusinessModel businessModel, Comment comment) {
        comment = commentService.create(comment);
        businessModel.addComment(comment);
        em.merge(businessModel);
        return comment;
    }

    /**
     * @param businessModel
     * @param userId
     * @return
     */
    public BusinessModel voteUp(BusinessModel businessModel, Long userId) {
        // remove user id from up votes if present
        // if not present (= remove returns false), add to up votes
        if (!businessModel.getUpVotes().remove(userId)) {
            businessModel.getUpVotes().add(userId);
        }
        return update(businessModel);
    }

    public long countTotalLikesForActionBusinessModelCreatedByUser(User user) {
        logger.infof("counting all likes for action businessModel postedBy user %d ...", user);
        long returnVal = 0;
        for (Action action : actionService.getAllActionsCreatedByUser(user)) {
            returnVal += action.getBusinessModel().getNoOfUpVotes();
        }

        return returnVal;
    }

    /**
     * This method counts likes given by user for all action BM.
     *
     * @param user
     * @return
     */
    public long countTotalLikesGivenByUserForAllActionBusinessModels(User user) {
        logger.infof("counting all likes by given user %s for all action Business Models ...", user);

        long returnVal = 0;
        for (BusinessModel businessModel : getAll()) {
            if (businessModel.getUpVotes().contains(user.getId())) {
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
    public long countTotalLikesForAllActionBusinessModels() {
        logger.infof("counting all likes for all actionIterations...");

        long returnVal = 0;
        for (BusinessModel actionBusinessModel : getAll()) {
            returnVal += actionBusinessModel.getNoOfUpVotes();
        }

        return returnVal;
    }

}

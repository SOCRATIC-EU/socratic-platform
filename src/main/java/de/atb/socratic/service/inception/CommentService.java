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
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Comment_;
import de.atb.socratic.model.User;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.user.UserService;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Stateless
public class CommentService extends AbstractService<Comment> {

    private static final long serialVersionUID = -6777330638891953695L;

    @EJB
    UserService userService;

    @Inject
    Logger logger;

    public CommentService() {
        super(Comment.class);
    }

    /**
     * @param comment
     * @return
     */
    public Comment detachComment(Comment comment) {
        em.detach(comment);
        return comment;
    }

    /**
     * @param comment
     * @return
     */
    public Comment updateComment(Comment comment) {
        comment = em.merge(comment);
        logger.infof("updated comment %s", comment);
        return comment;
    }

    /**
     * @param commentId
     * @return
     */
    public Comment getCommentById(Long commentId) {
        logger.infof("loading comment with ID %d ...", commentId);
        Comment comment = em.find(Comment.class, commentId);
        if (comment == null) {
            throw new EntityNotFoundException(String.format(
                    "Comment with id %d does not exist", commentId));
        }
        return comment;
    }

    /**
     * @return
     */
    public int countTotalNoOfCommentsByUser(User user) {
        logger.infof("counting all comments posted by user with id %d ...", user.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Comment> criteria = cb.createQuery(Comment.class);
        Root<Comment> root = criteria.from(Comment.class);
        criteria.where(cb.equal(root.get(Comment_.postedBy), user));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList().size();
    }

    @Override
    public Comment create(Comment comment) {
        logger.infof("persisted comment %s ...", comment);
        comment = em.merge(comment);

        // update counter noOfCommentsPosted for comment writer
        userService.increaseNoOfCommentsPosted(comment.getPostedBy());
        return comment;
    }

}

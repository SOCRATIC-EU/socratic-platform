package de.atb.socratic.web.inception.comment;

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

import java.util.Collection;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.authorization.strategies.metadata.EffMetaDataRoleAuthorizationStrategy;
import de.atb.socratic.authorization.strategies.metadata.IAuthorizationCondition;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.util.authorization.UserInScopeCondition;
import de.atb.socratic.web.components.Effects;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class CommentListPanel extends GenericPanel<Idea> {

    private static final long serialVersionUID = -3815630728682252957L;

    /**
     * Indicates whether the panel contains a form to post new ideas. In
     * Inception phase, this shall be set to true, in Prioritisation to false.
     */
    private boolean allowCommenting = true;

    // indicating in which innovation phase this panel is used.
    protected InnovationStatus innovationStatus;

    // indicating comments of which phase are allowed to be displayed
    private Collection<InnovationStatus> commentDisplayFilter;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject the EJB for managing ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    private final DataView<Comment> commentsRepeater;

    private Label commentsNumber;

    private final WebMarkupContainer commentsContainer;

    public CommentListPanel(final String id, final IModel<Idea> model, final boolean allowCommenting,
                            final InnovationStatus innovationStatus, Collection<InnovationStatus> commentDisplayFilter) {
        super(id, model);
        this.setOutputMarkupId(true);
        this.allowCommenting = allowCommenting;
        this.innovationStatus = innovationStatus;
        this.commentDisplayFilter = commentDisplayFilter;

        this.commentsNumber = new Label("commentsNumber", Model.of(model.getObject().getComments(commentDisplayFilter).size()));
        this.commentsNumber.setOutputMarkupId(true);

        commentsRepeater = newCommentsRepeater();

        // add list of comments
        commentsContainer = newCommentsContainer(commentsRepeater);
        add(commentsContainer);
        // add a form to post a new comment
        add(newCommentFormFragment());
    }

    /**
     * Builds a component to display a comment and appends it to the list of
     * comments.
     *
     * @param target  the target
     * @param comment the comment
     */
    private void appendCommentToList(AjaxRequestTarget target, final Comment comment) {

        // build new item for comment
        Component item = buildItem(comment);

        // append comment to list
        Effects.appendAndFadeIn(target, item, "div", commentsContainer.getMarkupId());
    }

    /**
     * @return the fragment
     */
    private Fragment newCommentFormFragment() {
        if (!this.allowCommenting) {
            return new Fragment("commentForm", "emptyFragment", this);
        }
        if (InnovationStatus.INCEPTION.equals(innovationStatus)) {
            Fragment commentFormFragment = new Fragment("commentForm", "commentFormFragment", this);
            commentFormFragment.add(newCommentFormPanel("commentFormPanel", getModel(), null));
            commentFormFragment.setOutputMarkupId(true);

            // remove scope
            Scope scope = getModelObject().getCampaign().getScope();
            IAuthorizationCondition cond = UserInScopeCondition.get(scope);
            EffMetaDataRoleAuthorizationStrategy.authorizeIf(cond, commentFormFragment, RENDER, UserRole.USER);
            EffMetaDataRoleAuthorizationStrategy.authorize(commentFormFragment, RENDER, UserRole.ADMIN, UserRole.MANAGER, UserRole.SUPER_ADMIN);
            return commentFormFragment;
        }
        if (InnovationStatus.PRIORITISATION.equals(innovationStatus)) {
            final Fragment commentFormFragment = new Fragment("commentForm", "commentFormFragment", this);
            commentFormFragment.add(newCommentFormPanel("commentFormPanel", getModel(), null));
            commentFormFragment.setOutputMarkupId(true);
            return commentFormFragment;
        }
        return new Fragment("commentForm", "emptyFragment", this);
    }

    /**
     * @param id    the id
     * @param model the model
     * @return the comment form panel
     */
    private CommentFormPanel newCommentFormPanel(final String id, final IModel<Idea> model, final Comment comment) {
        return new CommentFormPanel(id, model, comment, innovationStatus) {
            private static final long serialVersionUID = 6288306520583049323L;

            @Override
            protected void onAfterCreate(AjaxRequestTarget target, Comment comment) {
                appendCommentToList(target, comment);
                if (InnovationStatus.PRIORITISATION.equals(innovationStatus)) {
                    target.add(commentsNumber.setDefaultModelObject("("
                            + getModelObject().getComments(commentDisplayFilter)
                            .size() + ")"));
                } else {
                    target.add(commentsNumber.setDefaultModelObject("(" + getModelObject().getComments().size() + ")"));
                }
            }

            @Override
            protected void onAfterUpdate(AjaxRequestTarget target, Comment comment, Component component) {
                replaceFormWithPanel(target, comment, component);
            }

            @Override
            protected void onAfterUpdateCancelled(AjaxRequestTarget target, Comment comment, Component component) {
                replaceFormWithPanel(target, comment, component);
            }
        };
    }

    /**
     * @param target    the target
     * @param comment   the comment
     * @param component the component
     */
    private void replaceFormWithPanel(AjaxRequestTarget target, Comment comment, Component component) {
        component = component.replaceWith(newCommentPanel(component.getId(), comment));
        Effects.replaceWithFading(target, component);
    }

    /**
     * @param commentsRepeater the comments repeater
     * @return the container
     */
    private WebMarkupContainer newCommentsContainer(final DataView<Comment> commentsRepeater) {
        // the list of existing comments
        final WebMarkupContainer commentsContainer = new WebMarkupContainer("commentsContainer");
        commentsContainer.add(commentsRepeater);
        commentsContainer.setOutputMarkupPlaceholderTag(true);
        return commentsContainer;
    }

    /**
     * @return the data view
     */
    private DataView<Comment> newCommentsRepeater() {
        return new DataView<Comment>("comments", new ListDataProvider<>(getModelObject().getComments(
                this.commentDisplayFilter))) {
            private static final long serialVersionUID = 8977421159867492698L;

            @Override
            protected void populateItem(final Item<Comment> item) {
                CommentListPanel.this.populateItem(item, item.getModelObject());
            }
        };
    }

    /**
     * @param item    the item
     * @param comment the comment
     */
    private void populateItem(final WebMarkupContainer item, final Comment comment) {
        item.add(newCommentPanel("commentPanel", comment));
    }

    /**
     * @param id      the id
     * @param comment the comment
     * @return the comment panel
     */
    private CommentPanel newCommentPanel(final String id, final Comment comment) {
        return new CommentPanel(id, Model.of(comment), allowCommenting) {
            private static final long serialVersionUID = 3807070161673518305L;

            @Override
            protected boolean isEditLinkVisible() {
                return CommentListPanel.this.isEditLinkVisible(comment);
            }

            @Override
            protected boolean isDeleteLinkVisible() {
                return CommentListPanel.this.isDeleteLinkVisible(comment);
            }

            @Override
            protected void editLinkOnClick(AjaxRequestTarget target, Component component) {
                if (allowCommenting && loggedInUser.equals(comment.getPostedBy())) {
                    component = component.replaceWith(newCommentFormPanel(component.getId(),
                            CommentListPanel.this.getModel(), comment));
                    Effects.replaceWithFading(target, component);
                }
            }

            @Override
            protected void deleteLinkOnClick(AjaxRequestTarget target, Component component) {
                if (allowCommenting && loggedInUser.equals(comment.getPostedBy())) {
                    deleteComment(comment);
                    Effects.fadeOutAndRemove(target, component.getMarkupId());
                    if (InnovationStatus.PRIORITISATION.equals(innovationStatus)) {
                        target.add(commentsNumber
                                .setDefaultModelObject("("
                                        + CommentListPanel.this.getModelObject()
                                        .getComments(commentDisplayFilter)
                                        .size() + ")"));
                    } else {
                        target.add(commentsNumber.setDefaultModelObject("("
                                + CommentListPanel.this.getModelObject().getComments().size() + ")"));
                    }
                }
            }
        };
    }

    /**
     * @param comment the comment
     * @return true if link is visible, false otherwise
     */
    private boolean isEditLinkVisible(final Comment comment) {
        return allowCommenting && loggedInUser.equals(comment.getPostedBy());
    }

    /**
     * @param comment the comment
     * @return true if link is visible, false otherwise
     */
    private boolean isDeleteLinkVisible(final Comment comment) {
        return allowCommenting && loggedInUser.equals(comment.getPostedBy());
    }

    /**
     * @param comment the comment
     * @return a component
     */
    private Component buildItem(final Comment comment) {
        WebMarkupContainer item = new WebMarkupContainer(commentsRepeater.newChildId());
        item.setOutputMarkupPlaceholderTag(true);

        commentsRepeater.add(item);

        populateItem(item, comment);

        return item;
    }

    /**
     * @param comment the comment
     */
    private void deleteComment(final Comment comment) {
        ideaService.removeComment(getModelObject(), comment);
    }

    public Label getCommentsNumber() {
        return commentsNumber;
    }
}

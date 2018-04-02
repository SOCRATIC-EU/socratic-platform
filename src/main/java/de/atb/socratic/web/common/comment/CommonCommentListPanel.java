package de.atb.socratic.web.common.comment;

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

import java.util.List;

import javax.inject.Inject;

import de.atb.socratic.model.Comment;
import de.atb.socratic.model.User;
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
 * @param <T>
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CommonCommentListPanel<T> extends GenericPanel<T> {
    /**
     * Indicates whether the panel contains a form to post new ideas. In Inception phase, this shall be set to true, in
     * Prioritisation to false.
     */
    protected boolean allowCommenting = true;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    private final DataView<Comment> commentsRepeater;
    private Label commentsNumberTopOnTheButton, commentsNumberLeftSideOfTheButton;
    private final WebMarkupContainer commentsContainer;

    public CommonCommentListPanel(final String id, final IModel<T> model, final boolean allowCommenting) {
        super(id, model);
        this.setOutputMarkupId(true);
        this.allowCommenting = allowCommenting;

        int commentSize = getTotalCommentSize();
        if (commentSize <= 1) {
            this.commentsNumberTopOnTheButton = new Label("commentsNumberTopOnTheButton", Model.of(commentSize + " comment"));
        } else {
            this.commentsNumberTopOnTheButton = new Label("commentsNumberTopOnTheButton", Model.of(commentSize + " comments"));
        }

        this.commentsNumberTopOnTheButton.setOutputMarkupId(true);

        this.commentsNumberLeftSideOfTheButton = new Label("commentsNumberLeftSideOfTheButton", Model.of(commentSize));
        this.commentsNumberLeftSideOfTheButton.setOutputMarkupId(true);

        commentsRepeater = newCommentsRepeater();

        // add list of comments
        commentsContainer = newCommentsContainer(commentsRepeater);
        add(commentsContainer);
        // add a form to post a new comment
        add(newCommentFormFragment());
    }

    protected abstract int getTotalCommentSize();

    protected abstract List<Comment> getListOfComments();

    protected abstract void deleteComment(final Comment comment);

    protected abstract void onCommentListChanged(AjaxRequestTarget target);

    /**
     * Builds a component to display a comment and appends it to the list of comments.
     *
     * @param target
     * @param comment
     */
    protected void appendCommentToList(AjaxRequestTarget target, final Comment comment) {
        // build new item for comment
        Component item = buildItem(comment);

        // append comment to list
        Effects.appendAndFadeIn(target, item, "div", commentsContainer.getMarkupId());
    }

    /**
     * @return
     */
    private Fragment newCommentFormFragment() {
        if (!this.allowCommenting) {
            return new Fragment("commentForm", "emptyFragment", this);
        } else {
            Fragment commentFormFragment = null;
            commentFormFragment = new Fragment("commentForm", "commentFormFragment", this);
            commentFormFragment.add(newCommentFormPanel("commentFormPanel", getModel(), null));
            commentFormFragment.setOutputMarkupId(true);
            return commentFormFragment;
        }
    }

    /**
     * @param id
     * @param model
     * @return
     */
    private CommonCommentFormPanel<T> newCommentFormPanel(final String id, final IModel<T> model, final Comment comment) {
        return new CommonCommentFormPanel<T>(id, model, comment) {
            private static final long serialVersionUID = 6288306520583049323L;

            @Override
            protected void onAfterCreate(AjaxRequestTarget target, Comment comment) {
                appendCommentToList(target, comment);
                int commentSize = getTotalCommentSize();
                if (commentSize <= 1) {
                    target.add(commentsNumberTopOnTheButton.setDefaultModelObject(commentSize + " comment"));
                } else {
                    target.add(commentsNumberTopOnTheButton.setDefaultModelObject(commentSize + " comments"));
                }
                target.add(commentsNumberLeftSideOfTheButton.setDefaultModelObject(commentSize));

                // call onCommentListChanged method once comment is created..
                onCommentListChanged(target);
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
     * @param target
     * @param comment
     * @param component
     */
    private void replaceFormWithPanel(AjaxRequestTarget target, Comment comment, Component component) {
        component = component.replaceWith(newCommentPanel(component.getId(), comment));
        Effects.replaceWithFading(target, component);
    }

    /**
     * @param commentsRepeater
     * @return
     */
    private WebMarkupContainer newCommentsContainer(final DataView<Comment> commentsRepeater) {
        // the list of existing comments
        final WebMarkupContainer commentsContainer = new WebMarkupContainer("commentsContainer");
        commentsContainer.add(commentsRepeater);
        commentsContainer.setOutputMarkupPlaceholderTag(true);
        return commentsContainer;
    }

    /**
     * @return
     */
    private DataView<Comment> newCommentsRepeater() {
        return new DataView<Comment>("comments", new ListDataProvider<>(getListOfComments())) {
            private static final long serialVersionUID = 8977421159867492698L;

            @Override
            protected void populateItem(final Item<Comment> item) {
                CommonCommentListPanel.this.populateItem(item, item.getModelObject());
            }
        };
    }

    /**
     * @param item
     * @param comment
     */
    private void populateItem(final WebMarkupContainer item, final Comment comment) {
        item.add(newCommentPanel("commentPanel", comment));
    }

    /**
     * @param id
     * @param comment
     * @return
     */
    private CommonCommentPanel newCommentPanel(final String id, final Comment comment) {
        return new CommonCommentPanel(id, Model.of(comment)) {
            private static final long serialVersionUID = 3807070161673518305L;

            @Override
            protected boolean isEditLinkVisible() {
                return CommonCommentListPanel.this.isEditLinkVisible(comment);
            }

            @Override
            protected boolean isDeleteLinkVisible() {
                return CommonCommentListPanel.this.isDeleteLinkVisible(comment);
            }

            @Override
            protected void editLinkOnClick(AjaxRequestTarget target, Component component) {
                if (allowCommenting && loggedInUser.equals(comment.getPostedBy())) {
                    component = component.replaceWith(
                            newCommentFormPanel(
                                    component.getId(),
                                    CommonCommentListPanel.this.getModel(),
                                    comment));
                    Effects.replaceWithFading(target, component);
                }
            }

            @Override
            protected void deleteLinkOnClick(AjaxRequestTarget target, Component component) {
                if (allowCommenting && loggedInUser.equals(comment.getPostedBy())) {
                    deleteComment(comment);
                    Effects.fadeOutAndRemove(target, component.getMarkupId());
                    int commentSize = getTotalCommentSize();
                    if (commentSize <= 1) {
                        commentsNumberTopOnTheButton.setDefaultModelObject(commentSize + " comment");
                    } else {
                        commentsNumberTopOnTheButton.setDefaultModelObject(commentSize + " comments");
                    }
                    target.add(commentsNumberTopOnTheButton);
                    commentsNumberLeftSideOfTheButton.setDefaultModelObject(commentSize);
                    target.add(commentsNumberLeftSideOfTheButton);

                    // call onCommentListChanged method once comment is deleted..
                    onCommentListChanged(target);
                }
            }
        };
    }

    /**
     * @param comment
     * @return
     */
    private boolean isEditLinkVisible(final Comment comment) {
        return allowCommenting && loggedInUser != null && loggedInUser.equals(comment.getPostedBy());
    }

    /**
     * @param comment
     * @return
     */
    private boolean isDeleteLinkVisible(final Comment comment) {
        return allowCommenting && loggedInUser != null && loggedInUser.equals(comment.getPostedBy());
    }

    /**
     * @param comment
     * @return
     */
    private Component buildItem(final Comment comment) {
        WebMarkupContainer item = new WebMarkupContainer(commentsRepeater.newChildId());
        item.setOutputMarkupPlaceholderTag(true);
        commentsRepeater.add(item);
        populateItem(item, comment);
        return item;
    }

    public Label getCommentsNumberTopOnTheButton() {
        return commentsNumberTopOnTheButton;
    }

    public Label getCommentsNumberLeftSideOfTheButton() {
        return commentsNumberLeftSideOfTheButton;
    }


}

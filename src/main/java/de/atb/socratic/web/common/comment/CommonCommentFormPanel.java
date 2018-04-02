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

import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionActivity;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.ChallengeActivity;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaActivity;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.CommentService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.components.TinyMCETextArea;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import wicket.contrib.tinymce4.ajax.TinyMceAjaxSubmitLink;

/**
 * Created by Spindler on 24.10.2016.
 *
 * @param <T>
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CommonCommentFormPanel<T> extends GenericPanel<T> {

    private static final long serialVersionUID = -7597559886310183699L;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
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
    CommentService commentService;

    @EJB
    UserService userService;

    @EJB
    ActivityService activityService;

    @Inject
    ParticipateNotificationService participateNotifier;

    // Form to add/edit comment
    private final Form<Comment> commentForm;
    private TextArea<String> commentText;
    // the comment to add / edit
    private Comment theComment;
    private IModel<T> model;

    public CommonCommentFormPanel(final String id, final IModel<T> model, final Comment comment) {
        super(id, model);
        setOutputMarkupId(true);
        if (comment == null) {
            theComment = new Comment();
        } else {
            theComment = comment;
        }

        this.model = model;
        add(commentForm = newCommentForm(model.getObject()));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.markup.html.WebPage#onAfterRender()
     */
    @Override
    protected void onAfterRender() {
        super.onAfterRender();
        theComment = commentService.detachComment(theComment);

    }

    /**
     * @return
     */
    private Form<Comment> newCommentForm(final T model) {
        // the form to add a new comment
        final Form<Comment> commentForm = new InputValidationForm<>("commentForm");
        commentForm.setOutputMarkupId(true);
        commentText = new TinyMCETextArea("commentText", new PropertyModel<String>(theComment, "commentText"));
        commentText.setOutputMarkupId(true);
        commentForm.add(new OnEventInputBeanValidationBorder<>("commentTextValidationBorder", commentText, HtmlEvent.ONCHANGE));

        // add submit link
        final AjaxSubmitLink submitLink = new TinyMceAjaxSubmitLink("submit", commentForm) {
            private static final long serialVersionUID = 3245359953433341297L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                add(new AttributeModifier("value", new StringResourceModel((theComment.getId() == null) ? "submit.text"
                        : "edit.text", CommonCommentFormPanel.this, null)));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (theComment.getId() == null) {
                    createComment(target);
                    // notify about updates
                } else {
                    updateComment(target);
                    // notify about updates
                    // campaignService.notifyAboutCampaignUpdates(model);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(CommonCommentFormPanel.this.commentForm);
            }
        };
        commentForm.add(submitLink.setOutputMarkupId(true));

        // add a cancel link
        final AjaxLink<Void> cancelLink = new AjaxLink<Void>("cancel") {
            private static final long serialVersionUID = 3671533968219564745L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (theComment.getId() == null) {
                    cancelCreate(target);
                } else {
                    cancelUpdate(target);
                }
            }
        };
        commentForm.add(cancelLink.setOutputMarkupId(true));

        return commentForm;
    }

    /**
     * Override this to do ajax updates after comment has been created.
     */
    protected abstract void onAfterCreate(AjaxRequestTarget target, Comment comment);

    /**
     * Override this to do ajax updates after comment has been update.
     */
    protected abstract void onAfterUpdate(AjaxRequestTarget target, Comment comment, Component component);

    /**
     * Override this to do ajax updates after comment update has been cancelled.
     */
    protected abstract void onAfterUpdateCancelled(AjaxRequestTarget target, Comment comment, Component component);

    /**
     * @param target
     */
    private void createComment(AjaxRequestTarget target) {
        // create comment
        final Comment comment = addComment();

        // update form
        resetForm();
        target.add(commentForm);

        // update comment list
        onAfterCreate(target, comment);
    }

    /**
     * @param target
     */
    private void updateComment(AjaxRequestTarget target) {
        Comment comment = commentService.updateComment(theComment);
        onAfterUpdate(target, comment, this);
    }

    /**
     * @param target
     */
    private void cancelCreate(AjaxRequestTarget target) {
        resetForm();
        target.add(commentForm);
    }

    /**
     * @param target
     */
    private void cancelUpdate(AjaxRequestTarget target) {
        Comment comment = commentService.getCommentById(theComment.getId());
        onAfterUpdateCancelled(target, comment, this);
    }

    private void resetForm() {
        commentForm.modelChanging();
        commentText.modelChanging();
        theComment = new Comment();
        commentText.setDefaultModelObject(theComment.getCommentText());
        commentText.modelChanged();
        commentForm.modelChanged();
    }

    private Comment addComment() {
        Comment comment = null;
        theComment.setCommentText(commentText.getModelObject());
        theComment.setPostedAt(new Date());
        theComment.setPostedBy(loggedInUser);
        if (model.getObject() instanceof Campaign) {
            Campaign challenge = (Campaign) getModelObject();
            comment = campaignService.addComment(challenge, theComment);

            // once comment is added to challenge, create an activity related to it.
            activityService.create(ChallengeActivity.ofCommentAdd(comment, challenge));

            if (!userService.isUserFollowsGivenChallenge(challenge, loggedInUser.getId())) {
                // Add challenge to list of challenges followed for loggedInUser
                loggedInUser = userService.addChallengeToFollowedChallegesList(challenge, loggedInUser.getId());

                // send him/her notification that they now becomes follower of this idea. Do not send notification to CO.
                if (!loggedInUser.equals(challenge.getCreatedBy())) {
                    participateNotifier.addParticipationNotification(challenge, loggedInUser,
                            NotificationType.CAMPAIGN_FOLLOWED);
                }
            }
            // notify about campaign comments and likes
            campaignService.notifyCampaignFollowersAboutCampaignCommentsAndLikes(challenge);
        } else if (model.getObject() instanceof Idea) {
            Idea idea = (Idea) getModelObject();
            comment = ideaService.addComment(idea, theComment);

            // once comment is added to an idea, create an activity related to it.
            activityService.create(IdeaActivity.ofCommentAdd(comment, idea));

            if (!userService.isUserFollowsGivenIdea(idea, loggedInUser.getId())) {
                // Add idea to list of ideas followed for loggedInUser
                loggedInUser = userService.addIdeaToFollowedIdeasList(idea, loggedInUser.getId());

                // send him/her notification that they now becomes follower of this idea. Do not send notification to idea
                // leader.
                if (!loggedInUser.equals(idea.getPostedBy())) {
                    participateNotifier.addParticipationNotification(idea, loggedInUser, NotificationType.IDEA_FOLLOWED);
                }
            }
            // notify about idea comments and likes
            ideaService.notifyIdeaFollowersAboutIdeaCommentsAndLikes(idea);
        } else if (model.getObject() instanceof Action) {
            Action action = (Action) getModelObject();
            comment = actionService.addComment(action, theComment);

            // once comment is added to an action, create an activity related to it.
            activityService.create(ActionActivity.ofActionCommentAdd(comment, action));

            if (!userService.isUserFollowsGivenAction(action, loggedInUser.getId())) {
                // Add action to list of actions followed for loggedInUser
                loggedInUser = userService.addActionToFollowedActionsList(action, loggedInUser.getId());

                // send him/her notification that they now becomes follower of this action. Do not send notification to action
                // leader.
                if (!loggedInUser.equals(action.getPostedBy())) {
                    participateNotifier.addParticipationNotification(action, loggedInUser, NotificationType.ACTION_FOLLOWED);
                }
            }
            // notify about Action comments and likes
            actionService.notifyActionFollowersAboutActionCommentsAndLikes(action);
        } else if (model.getObject() instanceof ActionIteration) {
            ActionIteration iteration = (ActionIteration) getModelObject();
            comment = actionIterationService.addComment(iteration, theComment);

            // once comment is added to an Iteration, create an activity related to it.
            activityService.create(ActionActivity.ofActionIterationCommentAdd(comment, iteration.getAction(), iteration));

            if (!userService.isUserFollowsGivenAction(iteration.getAction(), loggedInUser.getId())) {
                // Add action of iteration to list of actions followed for loggedInUser
                userService.addActionToFollowedActionsList(iteration.getAction(), loggedInUser.getId());

                // send him/her notification that they now becomes follower of this action. Do not send notification to iteration
                // leader.
                if (!loggedInUser.equals(iteration.getPostedBy())) {
                    participateNotifier.addParticipationNotification(iteration.getAction(), loggedInUser,
                            NotificationType.ACTION_FOLLOWED);
                }
            }
        } else if (model.getObject() instanceof BusinessModel) {
            BusinessModel businessModel = (BusinessModel) getModelObject();
            comment = actionBusinessModelService.addComment(businessModel, theComment);

            Action action = actionService.getActionFromBusinessModel(businessModel);
            // once comment is added to an BusinessModel, create an activity related to it.
            activityService.create(ActionActivity.ofActionBusinessModelCommentAdd(comment, action, businessModel));

            if (!userService.isUserFollowsGivenAction(action, loggedInUser.getId())) {
                // Add action to list of actions followed for loggedInUser
                userService.addActionToFollowedActionsList(action, loggedInUser.getId());

                // send him/her notification that they now becomes follower of this action. Do not send notification to action
                // leader.
                if (!loggedInUser.equals(action.getPostedBy())) {
                    participateNotifier.addParticipationNotification(action, loggedInUser, NotificationType.ACTION_FOLLOWED);
                }
            }
        }

        return comment;
    }
}

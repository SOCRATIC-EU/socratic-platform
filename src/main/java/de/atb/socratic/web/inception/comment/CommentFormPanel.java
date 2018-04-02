/**
 *
 */
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

import java.util.Date;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Comment;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CommentService;
import de.atb.socratic.service.inception.IdeaService;
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
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CommentFormPanel extends GenericPanel<Idea> {

    private static final long serialVersionUID = 4901653493499250321L;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject the EJB for managing ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    @EJB
    CommentService commentService;

    // Form to add/edit comment
    private final Form<Comment> commentForm;
    private TextArea<String> commentText;
    // the comment to add / edit
    private Comment theComment;
    private InnovationStatus innovationStatus;

    /**
     * @param id
     * @param model
     * @param comment
     * @param innovationStatus
     */
    public CommentFormPanel(final String id,
                            final IModel<Idea> model,
                            final Comment comment,
                            final InnovationStatus innovationStatus) {
        super(id, model);
        setOutputMarkupId(true);

        this.innovationStatus = innovationStatus;

        if (comment == null) {
            theComment = new Comment();
        } else {
            theComment = comment;
        }

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
        // detach entity to avoid automatic update of changes in form.
        theComment = commentService.detachComment(theComment);
    }

    /**
     * @return
     */
    private Form<Comment> newCommentForm(final Idea ideaModel) {
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
                add(new AttributeModifier(
                        "value",
                        new StringResourceModel(
                                (theComment.getId() == null) ? "submit.text" : "edit.text",
                                CommentFormPanel.this,
                                null)));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (theComment.getId() == null) {
                    createComment(target);
                } else {
                    updateComment(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(CommentFormPanel.this.commentForm);
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
        final Comment comment = commentService.updateComment(theComment);
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
        final Comment comment = commentService.getCommentById(theComment.getId());
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
        theComment.setCommentText(commentText.getModelObject());
        theComment.setPostedAt(new Date());
        theComment.setPostedBy(loggedInUser);
        theComment.setInnovationStatus(innovationStatus);
        return ideaService.addComment(getModelObject(), theComment);
    }
}

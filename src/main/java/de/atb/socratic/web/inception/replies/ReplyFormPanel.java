/**
 *
 */
package de.atb.socratic.web.inception.replies;

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
import de.atb.socratic.model.Reply;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ReplyFormPanel extends GenericPanel<Comment> {

    private static final long serialVersionUID = 4901653493499250321L;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    IdeaService ideaService;
    @EJB
    CampaignService campaignService;

    private final Form<Reply> replyForm;
    private TextField<String> replyText;
    private Reply theReply;
    private Comment comment;
    private String ideaOrCampaign = "";

    /**
     * @param id
     * @param model
     * @param reply
     */
    public ReplyFormPanel(final String id, final IModel<Comment> model, final Reply reply, final String ideaOrCampaign) {
        super(id, model);
        setOutputMarkupId(true);

        this.ideaOrCampaign = ideaOrCampaign;

        comment = model.getObject();

        System.out.println("ideaOrCampaign: " + ideaOrCampaign);

        if (reply == null) {
            theReply = new Reply();
        } else {
            theReply = reply;
            if (ideaOrCampaign.equals("idea") || ideaOrCampaign == "idea") {
                ideaService.detachReply(reply);
            } else {
                campaignService.detachReply(reply);
            }

        }

        add(replyForm = newReplyForm());
    }

    /**
     * @return
     */
    private Form<Reply> newReplyForm() {
        // the form to add a new reply
        final Form<Reply> replyForm = new InputValidationForm<Reply>("replyForm");
        replyForm.setOutputMarkupId(true);
        replyText = new TextField<String>("replyText", new PropertyModel<String>(theReply, "replyText"));
        replyText.setOutputMarkupId(true);
        replyForm.add(new OnEventInputBeanValidationBorder<String>("replyTextValidationBorder", replyText,
                HtmlEvent.ONCHANGE));

        // add submit link
        final AjaxSubmitLink submitLink = new AjaxSubmitLink("submit", replyForm) {
            private static final long serialVersionUID = 3245359953433341297L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                add(new AttributeModifier("value", new StringResourceModel((theReply.getId() == null) ? "submit.text"
                        : "edit.text", ReplyFormPanel.this, null)));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (theReply.getId() == null) {
                    createReply(target);
                } else {
                    updateReply(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(ReplyFormPanel.this.replyForm);
            }
        };
        replyForm.add(submitLink.setOutputMarkupId(true));

        // add a cancel link
        final AjaxLink<Void> cancelLink = new AjaxLink<Void>("cancel") {
            private static final long serialVersionUID = 3671533968219564745L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (theReply.getId() == null) {
                    cancelCreate(target);
                } else {
                    cancelUpdate(target);
                }
            }
        };
        replyForm.add(cancelLink.setOutputMarkupId(true));

        return replyForm;
    }

    /**
     * Override this to do ajax updates after reply has been created.
     */
    protected abstract void onAfterCreate(AjaxRequestTarget target, Reply reply);

    /**
     * Override this to do ajax updates after reply has been update.
     */
    protected abstract void onAfterUpdate(AjaxRequestTarget target, Reply reply, Component component);

    /**
     * Override this to do ajax updates after reply update has been cancelled.
     */
    protected abstract void onAfterUpdateCancelled(AjaxRequestTarget target, Reply reply, Component component);

    /**
     * @param target
     */
    private void createReply(AjaxRequestTarget target) {
        // create reply
        final Reply reply = addReply();

        // update form
        resetForm();
        target.add(replyForm);

        // update reply list
        onAfterCreate(target, reply);
    }

    /**
     * @param target
     */
    private void updateReply(AjaxRequestTarget target) {
        if (ideaOrCampaign.equals("idea") || ideaOrCampaign == "idea") {
            final Reply reply = ideaService.updateReply(theReply);
            onAfterUpdate(target, reply, this);
        } else {
            final Reply reply = campaignService.updateReply(theReply);
            onAfterUpdate(target, reply, this);
        }
    }

    /**
     * @param target
     */
    private void cancelCreate(AjaxRequestTarget target) {
        resetForm();
        target.add(replyForm);
    }

    /**
     * @param target
     */
    private void cancelUpdate(AjaxRequestTarget target) {
        if (ideaOrCampaign.equals("idea") || ideaOrCampaign == "idea") {
            final Reply reply = ideaService.getReplyById(theReply.getId());
            onAfterUpdateCancelled(target, reply, this);
        } else {
            final Reply reply = campaignService.getReplyById(theReply.getId());
            onAfterUpdateCancelled(target, reply, this);
        }
    }

    private void resetForm() {
        replyForm.modelChanging();
        replyText.modelChanging();
        theReply = new Reply();
        replyText.setDefaultModelObject(theReply.getReplyText());
        replyText.modelChanged();
        replyText.modelChanged();
    }

    private Reply addReply() {
        if (ideaOrCampaign.equals("idea") || ideaOrCampaign == "idea") {
            theReply.setReplyText(replyText.getModelObject());
            theReply.setPostedAt(new Date());
            theReply.setPostedBy(loggedInUser);
            return ideaService.addReply(comment, theReply);
        } else {
            theReply.setReplyText(replyText.getModelObject());
            theReply.setPostedAt(new Date());
            theReply.setPostedBy(loggedInUser);
            return campaignService.addReply(comment, theReply);
        }
    }
}

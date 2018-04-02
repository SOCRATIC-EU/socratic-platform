package de.atb.socratic.web.action;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.authorization.strategies.annotations.AuthorizeInstantiation;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.action.detail.ActionIterationDetailPage;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@AuthorizeInstantiation({UserRole.MANAGER, UserRole.USER, UserRole.SUPER_ADMIN, UserRole.ADMIN})
public class ActionStatusAddPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private final Form<Void> actionForm;

    protected final InputBorder<String> callToActionValidationBorder;
    protected final TextArea<String> callToActionTextArea;

    // The action to edit/save
    private Action theAction;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    ActionService actionService;

    @EJB
    UserService userService;

    @Inject
    ParticipateNotificationService participateNotifier;

    @Inject
    Logger logger;

    public ActionStatusAddPage(PageParameters parameters) {
        super(parameters);

        // add form to create new action
        add(actionForm = newActionForm());

        // extract id parameter and set page title, header and action
        // depending on whether we are editing an existing action or creating
        // a new one
        loadAction(parameters.get("id"));

        // Add Basic fields (required)
        // add text field for name inside a border component that performs bean
        // validation
        actionForm.add(callToActionValidationBorder = newTextField("callToActionValidationBorder",
                callToActionTextArea = newTextArea("callToAction")));

        // Add all help texts for all the fields..
        callToActionValidationBorder.add(addToolTipWebMarkupContainer("callToActionHelpText", new StringResourceModel(
                "action.callToAction.desc.label", this, null), TooltipConfig.Placement.right));

        // add a button to create new status
        actionForm.add(newSubmitLink("publishButton"));
    }

    /**
     * @param actionId
     */
    protected void loadAction(final StringValue actionId) {
        if (actionId != null && !actionId.isEmpty()) {
            actionForm.add(new Label("header", new StringResourceModel("form.create.entre.header", this, null)));

            try {
                theAction = actionService.getById(actionId.toOptionalLong());
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }

    /**
     * @return
     */
    private Form<Void> newActionForm() {
        Form<Void> form = new InputValidationForm<Void>("form") {
            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
            }
        };
        form.setOutputMarkupId(true);
        return form;
    }

    /**
     * @return
     */
    private InputBorder<String> newTextField(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    private TextArea<String> newTextArea(String id) {
        return new TextArea<String>(id, new PropertyModel<String>(theAction, id));
    }

    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id, actionForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (theAction.getId() == null) {
                    this.add(new AttributeModifier("value", new StringResourceModel("callToAction.publish.button", this, null)
                            .getString()));
                } else {
                    this.add(new AttributeModifier("value", new StringResourceModel("callToAction.update.button", this, null)
                            .getString()));
                }
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                save(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                showErrors(target);
            }
        };
    }

    /**
     *
     */
    protected void save(AjaxRequestTarget target) {
        submitAction(target);
        // goto Latest Action Iteration details page.
        setResponsePage(
                ActionIterationDetailPage.class,
                new PageParameters().set("id", theAction.getId()).set("iterationId",
                        actionService.getLatestIterationOfAction(theAction.getId()).getId()));
    }

    private void submitAction(AjaxRequestTarget target) {
        updateAction(target);
    }

    /**
     * @param target
     */
    private void updateAction(AjaxRequestTarget target) {

        // update action
        updateActionFromForm(target);
        theAction = actionService.update(theAction);
        // send notification to all idea follower and challenge follower
        List<User> followers = userService.getAllUsersByGivenFollowedChallenge(theAction.getIdea().getCampaign(), Integer.MAX_VALUE, Integer.MAX_VALUE);
        followers.addAll(userService.getAllUsersByGivenFollowedIdea(theAction.getIdea(), Integer.MAX_VALUE, Integer.MAX_VALUE));
        Set<User> noRepeatedUsers = new HashSet<>(followers);
        for (User follower : noRepeatedUsers) {
            participateNotifier.addParticipationNotification(theAction, follower, NotificationType.ACTION_CREATION);
        }

        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     *
     */
    private void updateActionFromForm(AjaxRequestTarget target) {
        theAction.setCallToAction(callToActionTextArea.getModelObject());
        // once user creates an action, he/she will become follower of the parent challenge
        // userService.addChallengeToFollowedChallegesList(theCampaign, loggedInUser.getId());
    }


    /**
     * @param target
     */
    protected void showErrors(AjaxRequestTarget target) {
        // in case of errors (e.g. validation errors) show error
        // messages in form
        target.add(actionForm);
        target.add(callToActionTextArea);
        // also reload feedback panel
        target.add(feedbackPanel);

    }
}

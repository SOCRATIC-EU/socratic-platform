package de.atb.socratic.web.dashboard.iLead.action;

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
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.action.detail.ActionSolutionPage;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class AdminActionStatusEditPage extends AdminAction {
    private static final long serialVersionUID = -5559578453943490669L;

    private final Form<Void> actionForm;

    protected final InputBorder<String> callToActionValidationBorder;
    protected final TextArea<String> callToActionTextArea;

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

    public AdminActionStatusEditPage(PageParameters parameters) {
        super(parameters);

        // add form to create new action
        add(actionForm = newActionForm());

        actionForm.add(callToActionValidationBorder = newTextField("callToActionValidationBorder", callToActionTextArea = newTextArea("callToAction")));

        // Add all help texts for all the fields..
        callToActionValidationBorder.add(addToolTipWebMarkupContainer("callToActionHelpText", new StringResourceModel(
                "action.callToAction.desc.label", this, null), TooltipConfig.Placement.right));

        // add a button to create new status
        actionForm.add(newUpdateLink("updateButton"));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentSideTab(response, "statusTab");
    }

    private AjaxSubmitLink newUpdateLink(String id) {
        return new AjaxSubmitLink(id, actionForm) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                this.add(new AttributeModifier("value", new StringResourceModel("callToAction.update.button", this, null)
                        .getString()));
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
        // goto next page: Add Team to Action
        setResponsePage(ActionSolutionPage.class, new PageParameters().set("id", theAction.getId()));
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

        // set last modified date
        theAction.setLastModified(new Date());
        theAction = actionService.update(theAction);
        // send notification to all action follower about new call to action status
        List<Action> actions = new LinkedList<>();
        actions.add(theAction);
        List<User> followers = userService.getAllUsersByFollowingGivenActions(actions);
        for (User follower : followers) {
            participateNotifier.addParticipationNotification(theAction, follower, NotificationType.ACTION_STATUS_UPDATE);
        }
        // show feedback panel
        target.add(feedbackPanel);
    }

    /**
     *
     */
    private void updateActionFromForm(AjaxRequestTarget target) {
        theAction.setCallToAction(callToActionTextArea.getModelObject());
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

    /**
     * @return
     */
    private InputBorder<String> newTextField(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    private TextArea<String> newTextArea(String id) {
        return new TextArea<String>(id, new PropertyModel<String>(theAction, id));
    }

    /**
     * @return
     */
    private Form<Void> newActionForm() {
        Form<Void> form = new InputValidationForm<Void>("form") {
            private static final long serialVersionUID = 1263157040645140501L;

            @Override
            public void renderHead(IHeaderResponse response) {
                super.renderHead(response);
            }
        };
        form.setOutputMarkupId(true);
        return form;
    }
}

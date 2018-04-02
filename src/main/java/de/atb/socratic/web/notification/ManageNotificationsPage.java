package de.atb.socratic.web.notification;

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

import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.service.notification.InvitationMailService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by Spindler on 04.08.2016.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ManageNotificationsPage extends BasePage {

    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    UserService userService;

    @Inject
    InvitationMailService invitationMailService;

    @Inject
    UrlProvider urlProvider;

    private Form form;

    private AjaxCheckBox noNotificationsAtAll;
    private CheckBox challengeNotification, ideaNotification, actionNotification;

    public ManageNotificationsPage(final PageParameters parameters) {
        super(parameters);

        // add form
        form = new Form<>("form");
        add(form);


        form.add(noNotificationsAtAll = new AjaxCheckBox("notificationsAtAll", new PropertyModel<Boolean>(loggedInUser, "receiveNotifications")) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (getModelObject()) {
                    challengeNotification.setEnabled(true);
                    ideaNotification.setEnabled(true);
                    actionNotification.setEnabled(true);
                } else {
                    challengeNotification.setEnabled(false);
                    ideaNotification.setEnabled(false);
                    actionNotification.setEnabled(false);
                }
            }

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (getModelObject()) {
                    challengeNotification.setEnabled(true);
                    ideaNotification.setEnabled(true);
                    actionNotification.setEnabled(true);

                    challengeNotification.setModelObject(true);
                    ideaNotification.setModelObject(true);
                    actionNotification.setModelObject(true);

                    target.add(challengeNotification);
                    target.add(ideaNotification);
                    target.add(actionNotification);
                } else {
                    challengeNotification.setEnabled(false);
                    ideaNotification.setEnabled(false);
                    actionNotification.setEnabled(false);

                    challengeNotification.setModelObject(false);
                    ideaNotification.setModelObject(false);
                    actionNotification.setModelObject(false);

                    target.add(challengeNotification);
                    target.add(ideaNotification);
                    target.add(actionNotification);
                }
            }
        });
        challengeNotification = new CheckBox("challengeNotification", new PropertyModel<Boolean>(loggedInUser, "receiveChallengeNotifications"));
        ideaNotification = new CheckBox("ideaNotification", new PropertyModel<Boolean>(loggedInUser, "receiveIdeaNotifications"));
        actionNotification = new CheckBox("actionNotification", new PropertyModel<Boolean>(loggedInUser, "receiveActionNotifications"));

        challengeNotification.setOutputMarkupId(true);
        ideaNotification.setOutputMarkupId(true);
        actionNotification.setOutputMarkupId(true);
        noNotificationsAtAll.setOutputMarkupId(true);

        form.add(challengeNotification);
        form.add(ideaNotification);
        form.add(actionNotification);

        // submit
        form.add(new AjaxSubmitLink("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                updateUser(target);
                target.add(form);
            }
        });

    }

    private void updateUser(AjaxRequestTarget target) {

        loggedInUser.setReceiveChallengeNotifications(challengeNotification.getModelObject());
        loggedInUser.setReceiveIdeaNotifications(ideaNotification.getModelObject());
        loggedInUser.setReceiveActionNotifications(actionNotification.getModelObject());
        loggedInUser.setReceiveNotifications(noNotificationsAtAll.getModelObject());

        loggedInUser = userService.update(loggedInUser);

        getPage().success("Your notifications have been updated!");
        target.add(feedbackPanel);

        // clear form
        form.clearInput();
        form.modelChanged();
    }
}

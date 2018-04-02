package de.atb.socratic.web.dashboard.settings;

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

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.UUID;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.service.notification.InvitationMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.facebook.sendInvitations.FacebookSendInvitationPanel;
import de.atb.socratic.web.components.linkedin.share.LinkedInSharePanel;
import de.atb.socratic.web.dashboard.Dashboard;
import de.atb.socratic.web.invitations.InvitationPage;
import de.atb.socratic.web.notification.ManageNotificationsPage;
import de.atb.socratic.web.profile.EditUserPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.security.login.LoginPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class UserSettingsDashboardPage extends Dashboard {
    private static final long serialVersionUID = -2879283612964279750L;

    private Modal deleteUserNotificationModal;

    @Inject
    InvitationMailService invitationMailService;

    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject a logger
    @Inject
    Logger logger;

    @EJB
    UserService userService;

    public UserSettingsDashboardPage(PageParameters parameters) {
        super(parameters);

        // add edit user's basic properties link
        add(new BookmarkablePageLink<EditUserPage>("editUserLink", EditUserPage.class));

        // add edit user's password link
        add(new BookmarkablePageLink<ChangePasswordPage>("changePasswordLink", ChangePasswordPage.class));

        // add User Invitation
        add(new BookmarkablePageLink<InvitationPage>("invitationLink", InvitationPage.class, parameters));

        // facebook invitation
        FacebookSendInvitationPanel facebookSendInvitationPanel = new FacebookSendInvitationPanel("inviteFBFriends") {
            private static final long serialVersionUID = -2474634813184335412L;
        };

        facebookSendInvitationPanel.setOutputMarkupId(true);
        add(facebookSendInvitationPanel);

        LinkedInSharePanel<UserSettingsDashboardPage> linkedInSharePanel = new LinkedInSharePanel<UserSettingsDashboardPage>("inviteOnLinkedIn", Model.of(this), feedbackPanel) {

            /**
             *
             */
            private static final long serialVersionUID = -740165635168032012L;

            @Override
            protected String providePageLink() {
                String url = RequestCycle.get()
                        .getUrlRenderer()
                        .renderFullUrl(Url.parse(
                                RequestCycle.get().urlFor(LoginPage.class, new PageParameters()).toString()));

                return url;
            }
        };

        add(linkedInSharePanel);

        // add manage Notifications link
        add(new BookmarkablePageLink<ManageNotificationsPage>("manageNotificationsLink", ManageNotificationsPage.class));

        // add delete user profile link
        add(new AjaxLink<Void>("deleteUserProfileLink") {
            private static final long serialVersionUID = 5234170325894868654L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                // show confirmation dialogue box
                deleteUserNotificationModal.appendShowDialogJavaScript(target);
            }

        });

        // confirmation model dialogue asking user to confirm his decision.
        deleteUserNotificationModal = new DeleteUserNotificationModal("selectIdeaNotificationModal", new StringResourceModel(
                "deleteUser.notification.modal.header", this, null), new StringResourceModel("deleteUser.notification.modal.message", this,
                null), false) {
            private static final long serialVersionUID = 2096179879061520451L;

            @Override
            public void deleteUserClicked(AjaxRequestTarget target) {

                // get loggedIn user and set delete boolean to true and other parameters to default
                String userNickName = loggedInUser.getNickName(), userEmail = loggedInUser.getEmail();
                setCurrentUserToAnonymous();

                // send email to user for final time saying good-bye!
                try {
                    invitationMailService.sendGoodByeMessageToUser(userNickName, userEmail);
                } catch (NotificationException e) {
                    logger.error(e);
                    throw new RuntimeException("An unexpected error occurred!", e);
                }

                // close the model window
                deleteUserNotificationModal.appendCloseDialogJavaScript(target);

                // logged out automatically
                logout();
            }
        };

        add(deleteUserNotificationModal);
    }

    private void setCurrentUserToAnonymous() {

        // at first set delete boolean to true
        loggedInUser.setDeleted(true);

        // now set other parameters as default one
        loggedInUser.setFirstName("Anonymous");
        loggedInUser.setLastName("User");
        loggedInUser.setNickName("anonymous");

        // try to set password as random byte[]
        try {
            loggedInUser.setPassword(AuthenticationService.generateSalt());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            throw new RuntimeException("An unexpected error occurred!", e);
        }

        // prepend registration date with email to make it unique
        String modifiedEmail;
        modifiedEmail = loggedInUser.getRegistrationDate().getTime() + loggedInUser.getEmail();
        loggedInUser.setEmail(modifiedEmail);

        loggedInUser.setBirthDate(null);
        loggedInUser.setCity("");
        loggedInUser.setCountry("");
        loggedInUser.setLinkedInId(UUID.randomUUID().toString());
        loggedInUser.setLinkedInUrl("");
        loggedInUser.setFacebookId(UUID.randomUUID().toString());
        loggedInUser.setFacebookUrl("");
        loggedInUser.setTwitterUrl("");
        loggedInUser.setRegistrationStatus(RegistrationStatus.DELETED);
        loggedInUser.setSkills(new LinkedList<Tag>());
        loggedInUser.setInterests(new LinkedList<Tag>());

        // check if this works?
        loggedInUser.setProfilePictureFile(null);

        loggedInUser.setReceiveNotifications(false);
        loggedInUser.setReceiveChallengeNotifications(false);
        loggedInUser.setReceiveIdeaNotifications(false);
        loggedInUser.setReceiveActionNotifications(false);

        loggedInUser = userService.update(loggedInUser);
    }

    /**
     * logout user.
     */
    private void logout() {
        // clear session
        getSession().invalidateNow();
        // redirect to Login page
        setResponsePage(getApplication().getHomePage());
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "settingsTab");
    }

}

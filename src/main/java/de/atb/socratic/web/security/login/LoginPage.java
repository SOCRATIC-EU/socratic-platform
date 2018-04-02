package de.atb.socratic.web.security.login;

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

import java.io.File;
import java.io.IOException;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.exception.NoPendingRegistrationException;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.exception.UserRegistrationException;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.User;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.notification.RegistrationMailService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.EFFSession;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.LoginFormPanel;
import de.atb.socratic.web.components.facebook.FacebookAjaxSignInPanel;
import de.atb.socratic.web.components.facebook.FacebookProfile;
import de.atb.socratic.web.components.linkedin.LinkedInAjaxSignInPanel;
import de.atb.socratic.web.components.linkedin.LinkedInProfile;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.security.SecurityPage;
import de.atb.socratic.web.security.register.CancelRegistrationPage;
import de.atb.socratic.web.security.register.ConfirmRegistrationPage;
import de.atb.socratic.web.upload.FileUploadHelper;
import de.atb.socratic.web.upload.FileUploadHelper.UploadType;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class LoginPage extends SecurityPage {

    private static final long serialVersionUID = -4376306203067613308L;

    @EJB
    UserService userService;

    @EJB
    CompanyService companyService;

    @EJB
    EmploymentService employmentService;

    @Inject
    RegistrationMailService registrationMailService;

    @Inject
    UrlProvider urlProvider;

    private LoginFormPanel loginForm;
    private IndicatingAjaxLink<Void> resend;

    public LoginPage(final PageParameters parameters, AjaxRequestTarget target) {
        this(parameters);

        String msg = parameters.get("msg").toOptionalString();
        if (msg != null) {
            getPage().success(msg);
            target.add(feedbackPanel);
        }
    }

    public LoginPage(final PageParameters parameters) {
        super(parameters);

        // hide subNavContainer
        subNavContainer.setVisible(false);

        resend = new IndicatingAjaxLink<Void>("resend") {
            private static final long serialVersionUID = -5958845052833917370L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                resend.setVisible(false);
                resendRegistrationConfirmationEmail(target, loginForm.getEmail());
                target.add(resend);
            }
        };
        resend.setOutputMarkupId(true);

        feedbackPanel.add(new Behavior() {

            private static final long serialVersionUID = 9041522456721783008L;

            @Override
            public void onEvent(Component component, IEvent<?> event) {
                super.onEvent(component, event);
                if (event.getType() == Broadcast.BREADTH) {
                    feedbackPanel.getFeedbackMessages().clear();
                    resend.setVisible(false);
                    AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
                    target.add(resend);
                    target.add(feedbackPanel);
                }
            }
        });
        resend.setVisible(false);
        add(resend);

        // add login form with forgot password link
        add(loginForm = new LoginFormPanel("loginFormPanel", true) {

            private static final long serialVersionUID = 4562363069463056500L;

            @Override
            protected void doSubmit(final String email, final String password) {
                login(email, password);
            }
        });

        add(new LinkedInAjaxSignInPanel("linkedInSignin", feedbackPanel) {

            private static final long serialVersionUID = 6468646891818253574L;

            @Override
            public void authenticate(final LinkedInProfile profile, AjaxRequestTarget target) throws UserRegistrationException {
                User user = authenticationService.authenticateUserThroughLinkedIn(profile.getId());
                if (user != null) {
                    updateSessionAndRedirect(user);
                } else {
                    // check mail, if exsists -> exception
                    authenticationService.isEmailAlreadyRegistered(profile.getEMail());
                    // check ldap, if exsists -> exception
                    authenticationService.isLDAPPrincipalAlreadyRegistered(profile.getEMail());

                    // user does not exist yet
                    User tmpUser = profile.toEFFUser();
                    // check if nickName is unique
                    tmpUser = userService.findUniqueNickNameForFBLIUsers(tmpUser);
                    tmpUser = userService.create(tmpUser);
                    // set profile picture from LinkedIn property pictureUrl
                    try {
                        tmpUser = saveLinkedInPicture(tmpUser, profile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //set user Company to default company
                    authenticationService.addDefaultEmployment(tmpUser);

                    ((EFFSession) Session.get()).setLoggedInUserId(tmpUser.getId());

                    // send notification to user if there is no skill or interest specified
                    sendNotificationToUserAboutSkillsInterests(tmpUser);

                    PageParameters params = new PageParameters();
                    setResponsePage(UserProfileDetailsPage.class, params.set("id", tmpUser.getId()));
                }
            }

            @Override
            public void onError(AjaxRequestTarget target, Exception error) {
                error.printStackTrace();
                getPage().error(error.getMessage());
                target.add(feedbackPanel);
            }
        });

        //Facebook implementation
        FacebookAjaxSignInPanel facebookPanel = new FacebookAjaxSignInPanel("facebookSignin", feedbackPanel) {

            private static final long serialVersionUID = 6468646891818253574L;

            @Override
            public void authenticate(FacebookProfile profile, AjaxRequestTarget target) throws Exception {
                User user = authenticationService.authenticateUserThroughFacebook(profile.getId());
                if (user != null) {
                    updateSessionAndRedirect(user);
                } else {
                    // check mail, if exsists -> exception
                    authenticationService.isEmailAlreadyRegistered(profile.getEMail());
                    // check ldap, if exsists -> exception
                    authenticationService.isLDAPPrincipalAlreadyRegistered(profile.getEMail());

                    // user does not exist yet
                    User tmpUser = profile.toEFFUser();
                    // check if nickName is unique
                    tmpUser = userService.findUniqueNickNameForFBLIUsers(tmpUser);
                    tmpUser = userService.create(tmpUser);

                    // set profile picture from Facebook property pictureUrl
                    try {
                        tmpUser = saveFacebookPicture(tmpUser, profile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //set user Company to default company
                    authenticationService.addDefaultEmployment(tmpUser);

                    ((EFFSession) Session.get()).setLoggedInUserId(tmpUser.getId());

                    // send notification to user if there is no skill or interest specified
                    sendNotificationToUserAboutSkillsInterests(tmpUser);

                    PageParameters params = new PageParameters();
                    setResponsePage(UserProfileDetailsPage.class, params.set("id", tmpUser.getId()));
                }

            }

            @Override
            public void onError(AjaxRequestTarget target, Exception error) {
                error.printStackTrace();
                getPage().error(error.getMessage());
                target.add(feedbackPanel);

            }
        };

        facebookPanel.setOutputMarkupId(true);
        add(facebookPanel);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // adjust body padding to accommodate for variable navigation bar height
        response.render(OnDomReadyHeaderItem.forScript(JSTemplates.HIDE_SUBNAV));

        // for idea and actions share on facebook set these meta tags.
        final String titleString = new StringResourceModel("facebook.share.title", this, null).getString();
        final StringHeaderItem headerTitleItem = StringHeaderItem.forString("<meta property=\"og:title\" content=\""
                + titleString + "\" />");
        response.render(headerTitleItem);

        final String descriptionString = new StringResourceModel("facebook.share.description", this, null).getString();
        final StringHeaderItem headerDescriptionItem = StringHeaderItem
                .forString("<meta property=\"og:description\" content=\"" + descriptionString + "\" />");
        response.render(headerDescriptionItem);

        // set type meta tag
        final StringHeaderItem headerTypeItem = StringHeaderItem.forString("<meta property=\"og:type\" content=\"" + "website"
                + "\" />");
        response.render(headerTypeItem);

        // create image Link for given image
        final String pictureUrl = "https://socraticeitorg.files.wordpress.com/2016/03/socratic_logo1.jpg";

        final StringHeaderItem headerImageItem = StringHeaderItem.forString("<meta property=\"og:image\" content=\""
                + pictureUrl + "\" />");
        response.render(headerImageItem);
    }

    @Override
    protected void onRegistrationNotConfirmed() {
        // show a page with offer to send confirm registration email again
        resend.setVisible(true);
    }

    private void resendRegistrationConfirmationEmail(AjaxRequestTarget target, final String email) {
        try {
            // reset the registration information
            User user = authenticationService.resetRegistrationInformation(email);
            // send the confirm registration email again
            registrationMailService.sendRegistrationConfirmationMessage(
                    user,
                    urlProvider.urlFor(ConfirmRegistrationPage.class, "token", user.getRegistrationToken()),
                    urlProvider.urlFor(CancelRegistrationPage.class, "token", user.getRegistrationToken()),
                    urlProvider.urlFor(getApplication().getHomePage()));
            // continue to info page telling the user to confirm
            // registration
            success(new StringResourceModel("message.registration.confirmation.sent.again", this, null).getObject());
        } catch (NotificationException e) {
            error(e.getMessage());
        } catch (NoPendingRegistrationException e) {
            // user doesn't exist or has already confirmed/cancelled registration
            error(new StringResourceModel("message.resend.registration.failure", this, null).getObject());
        }
        target.add(feedbackPanel);
    }

    /**
     * @param user
     * @param profile
     * @return
     */
    private User saveLinkedInPicture(User user, LinkedInProfile profile) {
        if (StringUtils.isBlank(profile.getPictureUrl())) {
            // not pictureUrl given --> return
            return user;
        }

        FileInfo profilePicture = null;
        final File uploadFolder = new FileUploadHelper(UploadType.USER).createUploadFolderWithDir(user.getEmail(), user.getUploadCacheId());

        // create profile picture
        try {
            ProfilePictureResource.createProfilePictureFromLinkedIn(profile.getPictureUrl(), uploadFolder, user, PictureType.PROFILE);
        } catch (IOException e) {
            logger.error(e);
        }
        // create thumbnail of profile picture
        try {
            profilePicture = ProfilePictureResource.createProfilePictureFromLinkedIn(profile.getPictureUrl(), uploadFolder, user, PictureType.THUMBNAIL);
        } catch (IOException e) {
            logger.error(e);
        }
        // set profile picture for user
        if (profilePicture != null) {
            user.setProfilePictureFile(profilePicture);
            user = userService.update(user);
        }
        return user;
    }

    /**
     * @param user
     * @param profile
     * @return
     */
    private User saveFacebookPicture(User user, FacebookProfile profile) {
        if (StringUtils.isBlank(profile.getPictureUrl())) {
            // not pictureUrl given --> return
            return user;
        }

        FileInfo profilePicture = null;
        final File uploadFolder = new FileUploadHelper(UploadType.USER).createUploadFolderWithDir(user.getEmail(), user.getUploadCacheId());

        // create profile picture
        try {
            ProfilePictureResource.createProfilePictureFromFacebook(profile.getPictureUrl(), uploadFolder, user,
                    PictureType.PROFILE);
        } catch (IOException e) {
            logger.error(e);
        }
        // create thumbnail of profile picture
        try {
            profilePicture = ProfilePictureResource.createProfilePictureFromFacebook(profile.getPictureUrl(),
                    uploadFolder, user, PictureType.THUMBNAIL);
        } catch (IOException e) {
            logger.error(e);
        }
        // set profile picture for user
        if (profilePicture != null) {
            user.setProfilePictureFile(profilePicture);
            user = userService.update(user);
        }
        return user;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.socratic.web.BasePage#getPageTitleModel()
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

}

package de.atb.socratic.web.security;

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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.atb.socratic.exception.AutoCreatedLDAPUserException;
import de.atb.socratic.exception.AutoCreatedLDAPUserWithoutCompanyException;
import de.atb.socratic.exception.RegistrationNotConfirmedException;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.EFFSession;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Session;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@ApplicationScoped
public abstract class SecurityPage extends BasePage {

    // inject a logger
    @Inject
    protected Logger logger;

    @Inject
    protected AuthenticationService authenticationService;

    @LoggedInUser
    protected User loggedInUser;

    @Inject
    ParticipateNotificationService participateNotifier;

    /**
     * @param parameters
     */
    public SecurityPage(PageParameters parameters) {
        super(parameters);
    }

    /**
     * @param email
     * @param password
     */
    protected void login(final String email, final String password) {
        logger.info(String.format("Trying to login with email: %s", email));
        try {
            User user = authenticationService.authenticate(email, password);
            updateSessionAndRedirect(user);
            logger.info("The User with the email " + email + "logged into SOCRATIC.");
        } catch (RegistrationNotConfirmedException e) {
            onRegistrationNotConfirmed();
            error(new StringResourceModel("message.resend.registration.confirmation", this, null).getObject());
            logger.info("The User with the email " + email + "has not confirmed the registration yet!");
        } catch (UserAuthenticationException e) {
            error(e.getMessage());
            logger.info("The Login for the The User with the email " + email + "failed!");
        } catch (AutoCreatedLDAPUserException e) {
            User newUser = e.getAutoCreatedUser();
            if (newUser == null) {
                setResponsePage(ErrorPage.class);
            } else {
                updateSessionAndRedirect(e.getAutoCreatedUser());
            }
        } catch (AutoCreatedLDAPUserWithoutCompanyException e) {
            ((EFFSession) Session.get()).setLoggedInUserId(e.getAutoCreatedUser().getId());
            PageParameters params = new PageParameters();
            setResponsePage(UserProfileDetailsPage.class, params.set("id", loggedInUser.getId()));
        }
    }

    protected abstract void onRegistrationNotConfirmed();

    /**
     * @param user
     */
    protected void updateSessionAndRedirect(User user) {
        ((EFFSession) Session.get()).setLoggedInUserId(user.getId());
        continueToOriginalDestination();

        // send notification to user if there is no skill or interest specified
        sendNotificationToUserAboutSkillsInterests(user);

        // if we get here there was no previous request and we can continue
        // to home page
        setResponsePage(getApplication().getHomePage());
    }

    protected void sendNotificationToUserAboutSkillsInterests(User user) {
        // check if there is any skills or interest user has from his social media profile?
        if (user.getSkills() == null || user.getSkills().isEmpty() || user.getInterests() == null || user.getInterests().isEmpty()) {
            participateNotifier.addParticipationNotification(user, user, NotificationType.USER_SKILLS_INTEREST);
        }
    }
}

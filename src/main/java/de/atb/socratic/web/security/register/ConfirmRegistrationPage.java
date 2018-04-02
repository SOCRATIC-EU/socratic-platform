/**
 *
 */
package de.atb.socratic.web.security.register;

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

import javax.enterprise.event.Event;
import javax.inject.Inject;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.EFFSession;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ConfirmRegistrationPage extends CompleteRegistrationPage {

    private static final long serialVersionUID = -2107277710148109188L;

    @Inject
    UserService userService;

    @Inject
    Event<Boolean> userEventSrc;

    /**
     * @param parameters
     */
    public ConfirmRegistrationPage(final PageParameters parameters) {
        super(parameters);
        // hide subnav
        subNavContainer.setVisible(false);
        //if any user already logged-in - logout him
        if (loggedInUserProvider.getLoggedInUser() != null) {
            //clear session
            getSession().invalidateNow();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.socratic.web.security.register.CompleteRegistrationPage#
     * completeRegistration(java.lang.String, java.lang.String)
     */
    @Override
    protected void completeRegistration(final String email,
                                        final String password) {
        try {

            // after successful authentication cancel this user's registration
            User user = authenticationService.completeRegistration(email,
                    password, RegistrationStatus.CONFIRMED);

            // send confirmation email
            registrationMailService.sendRegistrationConfirmedMessage(user,
                    urlProvider.urlFor(getApplication().getHomePage()));

            // fire event that new user has been added to update list of all
            // users.
            userEventSrc.fire(true);

            //get all registered users and check if the confiming email is already in the list of invited contacts of some platform user
            List<User> allRegisteredUsers = userService.getAllRegisteredUsers();
            for (User usr : allRegisteredUsers) {
                List<String> invitedContacts = usr.getInvitedEmailContacts();
                if (invitedContacts.contains(email)) {
                    //send notification to user
                    userService.notifyAboutSuccessfullRegistrationOfInvitedContact(usr, email);
                }
            }

            // no exception so far --> user authenticated
            // set user id in session and continue to to home page, telling the
            // user on the next page that registration has been confirmed
            ((EFFSession) getSession()).setLoggedInUserId(user.getId());
            setResponsePage(
                    getApplication().getHomePage(),
                    new PageParameters().set(MESSAGE_PARAM,
                            getString("message.confirm.success")).set(
                            LEVEL_PARAM, FeedbackMessage.SUCCESS));
        } catch (UserAuthenticationException e) {
            error(e.getMessage());
        } catch (NotificationException e) {
            error(e.getMessage());
        }
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

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

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.User;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static de.atb.socratic.web.security.InfoPage.registrationCancelledMessage;

/**
 * @author ATB
 */
public class CancelRegistrationPage extends CompleteRegistrationPage {

    /**
     *
     */
    private static final long serialVersionUID = -3959626816622898188L;

    /**
     * @param parameters
     */
    public CancelRegistrationPage(final PageParameters parameters) {
        super(parameters);
        // hide subnav
        subNavContainer.setVisible(false);
    }

    /**
     *
     */
    protected void completeRegistration(final String email,
                                        final String password) {
        try {
            // after successful authentication cancel this user's registration
            User user = authenticationService.completeRegistration(email,
                    password, RegistrationStatus.CANCELLED);

            // send confirmation email
            registrationMailService.sendRegistrationCancelledMessage(user,
                    urlProvider.urlFor(getApplication().getHomePage()));

            // continue to info page telling the user that registration has been
            // completed
            setResponsePage(registrationCancelledMessage());
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

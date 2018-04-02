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

import javax.inject.Inject;

import de.atb.socratic.exception.NoPendingRegistrationException;
import de.atb.socratic.exception.RegistrationTimeoutException;
import de.atb.socratic.service.notification.RegistrationMailService;
import de.atb.socratic.service.security.AuthenticationService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.JSTemplates;
import de.atb.socratic.web.components.LoginFormPanel;
import de.atb.socratic.web.provider.UrlProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import static de.atb.socratic.web.security.InfoPage.registrationNotPendingMessage;
import static de.atb.socratic.web.security.InfoPage.registrationTimeoutMessage;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CompleteRegistrationPage extends BasePage {

    /**
     *
     */
    private static final long serialVersionUID = -3912243823578773987L;

    @Inject
    AuthenticationService authenticationService;

    @Inject
    RegistrationMailService registrationMailService;

    @Inject
    UrlProvider urlProvider;

    /**
     * @param parameters
     */
    public CompleteRegistrationPage(final PageParameters parameters) {

        super(parameters);

        String token = parameters.get("token").toString();
        try {
            // check if there is a pending registration for this token
            authenticationService.checkRegistrationStatus(token);
        } catch (NoPendingRegistrationException e) {
            // there's no user with this token and pending registration
            setResponsePage(registrationNotPendingMessage());
        } catch (RegistrationTimeoutException e) {
            // registration has been too long ago
            setResponsePage(registrationTimeoutMessage(Model.of(e.getEmail())));
        }

        // no exception --> add login form without forgot password link
        add(new LoginFormPanel("loginFormPanel", false) {

            private static final long serialVersionUID = 1440228565428118628L;

            @Override
            protected void doSubmit(final String email, final String password) {
                completeRegistration(email, password);
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // adjust body padding to accommodate for variable navigation bar height
        response.render(OnDomReadyHeaderItem.forScript(JSTemplates.HIDE_SUBNAV));
    }

    /**
     * @param email
     * @param password
     */
    protected abstract void completeRegistration(final String email,
                                                 final String password);

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

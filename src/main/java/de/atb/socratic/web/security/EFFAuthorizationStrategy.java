/**
 *
 */
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

import de.atb.socratic.authorization.strategies.EffRoleAuthorizationStrategy;
import de.atb.socratic.web.AboutPage;
import de.atb.socratic.web.ContactPage;
import de.atb.socratic.web.EFFSession;
import de.atb.socratic.web.NotLoggedInErrorPage;
import de.atb.socratic.web.action.detail.ActionsListPage;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.CampaignsPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.observatory.ObservatoryPage;
import de.atb.socratic.web.security.login.LoginPage;
import de.atb.socratic.web.security.register.CancelRegistrationPage;
import de.atb.socratic.web.security.register.ConfirmRegistrationPage;
import de.atb.socratic.web.security.register.RegisterPage;
import de.atb.socratic.web.security.resetpassword.RecoverPasswordPage;
import de.atb.socratic.web.security.resetpassword.ResetPasswordPage;
import de.atb.socratic.web.selection.SelectionPage;
import de.atb.socratic.web.welcome.WelcomePage;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.component.IRequestableComponent;

/**
 * @author ATB
 */
public class EFFAuthorizationStrategy extends EffRoleAuthorizationStrategy {

    public EFFAuthorizationStrategy() {
        super(new EFFRoleStrategy());
        add(new EffRedirectStrategy());
    }

    private static class EffRedirectStrategy implements IAuthorizationStrategy {
        /*
         * (non-Javadoc)
         *
         * @see org.apache.wicket.authorization.IAuthorizationStrategy#
         * isInstantiationAuthorized(java.lang.Class)
         */
        @Override
        public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
            // if it's not a wicket page --> allow
            if (!Page.class.isAssignableFrom(componentClass)) {
                return true;
            }
            // if it's a page that does not require authentication --> allow
            if (LoginPage.class.isAssignableFrom(componentClass)
                    || RecoverPasswordPage.class.isAssignableFrom(componentClass)
                    || WelcomePage.class.isAssignableFrom(componentClass)
                    || ObservatoryPage.class.isAssignableFrom(componentClass)
                    || CampaignsPage.class.isAssignableFrom(componentClass)
                    || IdeasPage.class.isAssignableFrom(componentClass)
                    || SelectionPage.class.isAssignableFrom(componentClass)
                    || ChallengeDefinitionPage.class.isAssignableFrom(componentClass)
                    || ActionsListPage.class.isAssignableFrom(componentClass)
                    || ResetPasswordPage.class.isAssignableFrom(componentClass)
                    || RegisterPage.class.isAssignableFrom(componentClass)
                    || InfoPage.class.isAssignableFrom(componentClass)
                    || NotLoggedInErrorPage.class.isAssignableFrom(componentClass)
                    || AboutPage.class.isAssignableFrom(componentClass)
                    || ContactPage.class.isAssignableFrom(componentClass)
                    || ConfirmRegistrationPage.class.isAssignableFrom(componentClass)
                    || CancelRegistrationPage.class.isAssignableFrom(componentClass)) {
                return true;
            }
            // if it's any other wicket page and user is not logged in -->
            // redirect to login page
            if (!((EFFSession) Session.get()).isAuthenticated()) {
                throw new RestartResponseAtInterceptPageException(LoginPage.class);
            }
            return true;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized
         * (org.apache.wicket.Component, org.apache.wicket.authorization.Action)
         */
        @Override
        public boolean isActionAuthorized(Component component, Action action) {
            return true;
        }
    }
}

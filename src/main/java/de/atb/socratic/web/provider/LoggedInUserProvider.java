/**
 *
 */
package de.atb.socratic.web.provider;

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

import java.io.Serializable;
import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import de.atb.socratic.authorization.authentication.UserEmploymentPrincipal;
import de.atb.socratic.model.User;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.EFFSession;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * @author ATB
 */
@SessionScoped
public class LoggedInUserProvider implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8975935355625648689L;

    @Inject
    UserService userService;

    @Inject
    HttpServletRequest httpRequest;

    private User loggedInUser;

    /**
     * @return the loggedInUser
     */
    @Produces
    @LoggedInUser
    public User getLoggedInUser() {
        if (loggedInUser == null) {
            retrieveLoggedInUser();
        }
        return loggedInUser;
    }

    /**
     * Updates the logged in user entity when it receives the respective event.
     * Whenever the logged in user changes his profile settings, the respective
     * event should be fired.
     *
     * @param user
     */
    public void onUserChanged(@Observes(notifyObserver = Reception.IF_EXISTS) final User user) {
        retrieveLoggedInUser();
    }

    /**
     * Gets the logged in user's email from the session and loads user from DB.
     */
    @PostConstruct
    public void retrieveLoggedInUser() {
        if (RequestCycle.get() == null) {
            if ((loggedInUser == null) && (httpRequest != null) && (httpRequest.getSession() != null)) {
                Object object = httpRequest.getSession().getAttribute(Principal.class.getName());
                if (object != null) {
                    UserEmploymentPrincipal principal = (UserEmploymentPrincipal) object;
                    loggedInUser = principal.getUser();
                    loggedInUser.setCurrentEmployment(principal.getEmployment());
                }
            }
        } else {
            if (((EFFSession) Session.get()).isAuthenticated()) {
                loggedInUser = userService.getById(((EFFSession) Session.get()).getLoggedInUserId());
            }
        }
    }

}

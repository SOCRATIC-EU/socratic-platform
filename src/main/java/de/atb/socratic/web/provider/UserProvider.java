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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.qualifier.AllUsers;

/**
 * @author ATB
 */
@SessionScoped
public class UserProvider implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1787696307265692605L;

    @Inject
    UserService userService;

    private List<User> allUsers;

    /**
     * @return all registered users
     */
    @Produces
    @AllUsers
    public List<User> getAllUsers() {
        return allUsers;
    }

    /**
     * Updates the list of registered users when it receives the respective
     * event. Whenever a user completes registration or deletes his profile the
     * respective event should be fired.
     *
     * @param changed
     */
    public void onUsersChanged(@Observes(notifyObserver = Reception.IF_EXISTS) final Boolean changed) {
        retrieveAllUsers();
    }

    /**
     * Loads all registered users from DB.
     */
    @PostConstruct
    public void retrieveAllUsers() {
        allUsers = userService.getAllRegisteredUsers();
    }

}

package de.atb.socratic.exception;

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

import de.atb.socratic.model.User;

public class AutoCreatedLDAPUserWithoutCompanyException extends Exception {

    private static final long serialVersionUID = 5888113687714957659L;

    private User autoCreatedUser;

    /**
     *
     */
    public AutoCreatedLDAPUserWithoutCompanyException(User autoCreatedUser) {
        super();
        this.setAutoCreatedUser(autoCreatedUser);
    }

    /**
     * @return the autoCreatedUser
     */
    public User getAutoCreatedUser() {
        return autoCreatedUser;
    }

    /**
     * @param autoCreatedUser the autoCreatedUser to set
     */
    public void setAutoCreatedUser(User autoCreatedUser) {
        this.autoCreatedUser = autoCreatedUser;
    }

}

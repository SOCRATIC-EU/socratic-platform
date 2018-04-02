package de.atb.socratic.authorization.authentication;

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

import java.security.Principal;

import de.atb.socratic.model.Employment;
import de.atb.socratic.model.User;

/**
 * UserEmploymentPricinpal
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class UserEmploymentPrincipal implements Principal {

    private User user;
    private Employment employment;
    private AuthenticationType authType;

    public UserEmploymentPrincipal() {

    }

    public UserEmploymentPrincipal(User user, Employment employment) {
        this.user = user;
        this.employment = employment;
    }

    public UserEmploymentPrincipal(User user, Employment employment, AuthenticationType authType) {
        this.user = user;
        this.employment = employment;
        this.authType = authType;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return user != null ? user.getEmail() : null;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the employment
     */
    public Employment getEmployment() {
        return employment;
    }

    /**
     * @param employment the employment to set
     */
    public void setEmployment(Employment employment) {
        this.employment = employment;
    }

    /**
     * @return the type
     */
    public AuthenticationType getAuthenticationType() {
        return authType;
    }

    /**
     * @param type the type to set
     */
    public void setAuthenticationType(AuthenticationType type) {
        this.authType = type;
    }

}

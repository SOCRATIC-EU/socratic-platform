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

import java.io.Serializable;
import java.util.EnumSet;

import javax.inject.Inject;

import de.atb.socratic.authorization.strategies.IEffRoleCheckingStrategy;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.web.provider.LoggedInUserProvider;
import org.apache.wicket.cdi.CdiContainer;

/**
 * EFFRoleStrategy
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class EFFRoleStrategy implements IEffRoleCheckingStrategy, Serializable {

    private static final long serialVersionUID = -3828272851230702393L;

    @Inject
    LoggedInUserProvider provider;

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.socratic.authorization.strategies.IEffRoleCheckingStrategy#hasAnyRole
     * (java.util.EnumSet)
     */
    @Override
    public boolean hasAnyRole(EnumSet<UserRole> roles) {
        return hasAnyRole(roles.toArray(new UserRole[roles.size()]));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.socratic.authorization.strategies.IEffRoleCheckingStrategy#hasAnyRole
     * (de.atb.socratic.model.UserRole[])
     */
    @Override
    public boolean hasAnyRole(UserRole... roles) {
        CdiContainer.get().getNonContextualManager().inject(this);
        if ((provider == null) || (provider.getLoggedInUser() == null)) {
            return false;
        }
        boolean authorized = provider.getLoggedInUser().hasAnyRoles(roles);
        return authorized;
    }
}

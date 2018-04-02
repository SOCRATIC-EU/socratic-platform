package de.atb.socratic.authorization.strategies;

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

import java.util.EnumSet;

import de.atb.socratic.model.UserRole;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.util.lang.Args;

/**
 * AbstractRoleAuthorizationStrategy
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public abstract class EffAbstractRoleAuthorizationStrategy implements IAuthorizationStrategy {
    /**
     * Role checking strategy.
     */
    private final IEffRoleCheckingStrategy roleCheckingStrategy;

    /**
     * Construct.
     *
     * @param roleCheckingStrategy the authorizer delegate
     */
    public EffAbstractRoleAuthorizationStrategy(IEffRoleCheckingStrategy roleCheckingStrategy) {
        Args.notNull(roleCheckingStrategy, "roleCheckingStrategy");
        this.roleCheckingStrategy = roleCheckingStrategy;
    }

    /**
     * Gets whether any of the given roles applies to the authorizer.
     *
     * @param roles the roles
     * @return whether any of the given roles applies to the authorizer
     */
    protected final boolean hasAny(UserRole... roles) {
        if ((roles == null) || (roles.length == 0)) {
            return true;
        } else {
            return roleCheckingStrategy.hasAnyRole(roles);
        }
    }

    /**
     * Gets whether any of the given roles applies to the authorizer.
     *
     * @param roles the roles
     * @return whether any of the given roles applies to the authorizer
     */
    protected final boolean hasAny(EnumSet<UserRole> roles) {
        if ((roles == null) || (roles.isEmpty())) {
            return true;
        } else {
            return roleCheckingStrategy.hasAnyRole(roles);
        }
    }

    /**
     * Conducts a check to see if the roles object is empty. Since the roles
     * object does not contain any null values and will always hold an empty
     * string, an extra test is required beyond roles.isEmpty().
     *
     * @param roles the Roles object to test
     * @return true if the object holds no real roles
     */
    protected final boolean isEmpty(UserRole... roles) {
        if ((roles == null) || (roles.length == 0)) {
            return true;
        }
        return false;
    }

    /**
     * Conducts a check to see if the roles object is empty. Since the roles
     * object does not contain any null values and will always hold an empty
     * string, an extra test is required beyond roles.isEmpty().
     *
     * @param roles the Roles object to test
     * @return true if the object holds no real roles
     */
    protected final boolean isEmpty(EnumSet<UserRole> roles) {
        if ((roles == null) || (roles.isEmpty())) {
            return true;
        }
        return false;
    }
}

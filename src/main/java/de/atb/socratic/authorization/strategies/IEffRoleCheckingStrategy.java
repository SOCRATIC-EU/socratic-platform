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

/**
 * IEffRoleCheckingStrategy
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public interface IEffRoleCheckingStrategy {

    /**
     * Whether any of the given roles matches. For example, if a user has role
     * USER and the provided roles are {USER, ADMIN} this method should return
     * true as the user has at least one of the roles that were provided.
     *
     * @param roles the roles
     * @return true if a user or whatever subject this implementation wants to
     * work with has at least on of the provided roles
     */
    boolean hasAnyRole(EnumSet<UserRole> roles);

    /**
     * Whether any of the given roles matches. For example, if a user has role
     * USER and the provided roles are {USER, ADMIN} this method should return
     * true as the user has at least one of the roles that were provided.
     *
     * @param roles the roles
     * @return true if a user or whatever subject this implementation wants to
     * work with has at least on of the provided roles
     */
    boolean hasAnyRole(UserRole... roles);
}

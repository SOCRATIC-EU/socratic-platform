package de.atb.socratic.authorization.strategies.metadata;

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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import de.atb.socratic.model.UserRole;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.util.io.IClusterable;

/**
 * EffActionPermissions
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class ActionPermissions implements IClusterable {

    private static final long serialVersionUID = -4603997458476902489L;

    /**
     * Map from an action to a set of role strings
     */
    private final Map<Action, EnumSet<UserRole>> rolesForAction = new HashMap<Action, EnumSet<UserRole>>();

    /**
     * Gives permission for the given roles to perform the given action
     *
     * @param action     The action
     * @param rolesToAdd The roles
     */
    public final void authorize(final Action action, UserRole... rolesToAdd) {
        if (action == null) {
            throw new IllegalArgumentException("Argument action cannot be null");
        }

        if (rolesToAdd == null) {
            throw new IllegalArgumentException("Argument rolesToAdd cannot be null");
        }

        EnumSet<UserRole> roles = rolesForAction.get(action);
        if (roles == null) {
            roles = EnumSet.noneOf(UserRole.class);
        }
        roles.addAll(Arrays.asList(rolesToAdd));
        rolesForAction.put(action, roles);
    }

    /**
     * Gives permission for the given roles to perform the given action
     *
     * @param action     The action
     * @param rolesToAdd The roles
     */
    public final void authorize(final Action action, EnumSet<UserRole> rolesToAdd) {
        this.authorize(action, rolesToAdd.toArray(new UserRole[rolesToAdd.size()]));
    }

    /**
     * Remove all authorization for the given action.
     *
     * @param action The action to clear
     */
    public final void authorizeAll(final Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Argument action cannot be null");
        }

        rolesForAction.remove(action);
    }

    /**
     * Gets the roles that have a binding for the given action.
     *
     * @param action The action
     * @return The roles authorized for the given action
     */
    public final EnumSet<UserRole> rolesFor(final Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Argument action cannot be null");
        }

        return rolesForAction.get(action);
    }

    /**
     * Remove the given authorized role from an action. Note that this is only
     * relevant if a role was previously authorized for that action. If no roles
     * where previously authorized the effect of the unauthorize call is that no
     * roles at all will be authorized for that action.
     *
     * @param action        The action
     * @param rolesToRemove The list of roles to remove
     */
    public final void unauthorize(final Action action, final UserRole... rolesToRemove) {
        if (action == null) {
            throw new IllegalArgumentException("Argument action cannot be null");
        }

        if (rolesToRemove == null) {
            throw new IllegalArgumentException("Argument rolesToRemove cannot be null");
        }

        EnumSet<UserRole> roles = rolesForAction.get(action);
        if (roles != null) {
            roles.removeAll(Arrays.asList(rolesToRemove));
        } else {
            roles = EnumSet.noneOf(UserRole.class);
        }

        // If we removed the last authorized role, we authorize the empty role
        // so that removing authorization can't suddenly open something up to
        // everyone.
        if (roles.size() == 0) {
            roles.add(UserRole.NO_ROLE);
        }
        rolesForAction.put(action, roles);
    }

    /**
     * Remove the given authorized role from an action. Note that this is only
     * relevant if a role was previously authorized for that action. If no roles
     * where previously authorized the effect of the unauthorize call is that no
     * roles at all will be authorized for that action.
     *
     * @param action        The action
     * @param rolesToRemove The list of roles to remove
     */
    public final void unauthorize(final Action action, final EnumSet<UserRole> rolesToRemove) {
        this.unauthorize(action, rolesToRemove.toArray(new UserRole[rolesToRemove.size()]));
    }
}

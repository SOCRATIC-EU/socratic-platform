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
import org.apache.wicket.Component;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.util.io.IClusterable;

/**
 * InstantiationPermissions
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class InstantiationPermissions implements IClusterable {

    private static final long serialVersionUID = -4211876902041766241L;

    /**
     * Holds roles objects for component classes
     */
    private final Map<Class<? extends Component>, EnumSet<UserRole>> rolesForComponentClass = new HashMap<Class<? extends Component>, EnumSet<UserRole>>();

    /**
     * Gives the given role permission to instantiate the given class.
     *
     * @param <T>
     * @param componentClass The component class
     * @param rolesToAdd     The roles to add
     */
    public final <T extends Component> void authorize(final Class<T> componentClass, final UserRole... rolesToAdd) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Argument componentClass cannot be null");
        }

        if (rolesToAdd == null) {
            throw new IllegalArgumentException("Argument rolesToadd cannot be null");
        }

        EnumSet<UserRole> roles = rolesForComponentClass.get(componentClass);
        if (roles == null) {
            roles = EnumSet.noneOf(UserRole.class);
            rolesForComponentClass.put(componentClass, roles);
        }
        roles.addAll(Arrays.asList(rolesToAdd));
    }

    /**
     * Gives the given role permission to instantiate the given class.
     *
     * @param <T>
     * @param componentClass The component class
     * @param rolesToAdd     The roles to add
     */
    public final <T extends Component> void authorize(final Class<T> componentClass, final EnumSet<UserRole> rolesToAdd) {
        authorize(componentClass, rolesToAdd.toArray(new UserRole[rolesToAdd.size()]));
    }

    /**
     * Gives all roles permission to instantiate the given class. Note that this
     * is only relevant if a role was previously authorized for that class. If
     * no roles where previously authorized the effect of the unauthorize call
     * is that no roles at all will be authorized for that class.
     *
     * @param <T>
     * @param componentClass The component class
     */
    public final <T extends Component> void authorizeAll(final Class<T> componentClass) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Argument componentClass cannot be null");
        }

        rolesForComponentClass.remove(componentClass);
    }

    /**
     * Gets the roles that have a binding with the given component class.
     *
     * @param <T>
     * @param componentClass the component class
     * @return the roles that have a binding with the given component class, or
     * null if no entries are found
     */
    public <T extends IRequestableComponent> EnumSet<UserRole> authorizedRoles(final Class<T> componentClass) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Argument componentClass cannot be null");
        }

        return rolesForComponentClass.get(componentClass);
    }

    /**
     * Removes permission for the given role to instantiate the given class.
     *
     * @param <T>
     * @param componentClass The class
     * @param rolesToRemove  The role to deny
     */
    public final <T extends Component> void unauthorize(final Class<T> componentClass, final UserRole... rolesToRemove) {
        if (componentClass == null) {
            throw new IllegalArgumentException("Argument componentClass cannot be null");
        }

        if (rolesToRemove == null) {
            throw new IllegalArgumentException("Argument rolesToRemove cannot be null");
        }

        EnumSet<UserRole> roles = rolesForComponentClass.get(componentClass);
        if (roles != null) {
            roles.removeAll(Arrays.asList(rolesToRemove));
        } else {
            roles = EnumSet.noneOf(UserRole.class);
            rolesForComponentClass.put(componentClass, roles);
        }

        // If we removed the last authorized role, we authorize the empty role
        // so that removing authorization can't suddenly open something up to
        // everyone.
        if (roles.size() == 0) {
            roles.add(UserRole.NO_ROLE);
        }
    }

    /**
     * Removes permission for the given role to instantiate the given class.
     *
     * @param <T>
     * @param componentClass The class
     * @param rolesToRemove  The role to deny
     */
    public final <T extends Component> void unauthorize(final Class<T> componentClass, final EnumSet<UserRole> rolesToRemove) {
        unauthorize(componentClass, rolesToRemove.toArray(new UserRole[rolesToRemove.size()]));
    }

    /**
     * @return gets map with roles objects for a component classes
     */
    protected final Map<Class<? extends Component>, EnumSet<UserRole>> getRolesForComponentClass() {
        return rolesForComponentClass;
    }

}

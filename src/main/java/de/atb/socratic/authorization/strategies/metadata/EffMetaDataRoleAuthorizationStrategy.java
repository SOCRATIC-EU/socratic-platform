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

import java.util.EnumSet;

import de.atb.socratic.authorization.strategies.EffAbstractRoleAuthorizationStrategy;
import de.atb.socratic.authorization.strategies.IEffRoleCheckingStrategy;
import de.atb.socratic.model.UserRole;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.request.component.IRequestableComponent;

/**
 * MetaDataRoleAuthorizationStrategy
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class EffMetaDataRoleAuthorizationStrategy extends EffAbstractRoleAuthorizationStrategy {

    public static final MetaDataKey<ActionPermissions> ACTION_PERMISSIONS = new MetaDataKey<ActionPermissions>() {
        private static final long serialVersionUID = -6034806047968773277L;

    };

    /**
     * Application meta data key for actions/roles information. Typically, you
     * do not need to use this meta data key directly, but instead use one of
     * the bind methods of this class.
     */
    public static final MetaDataKey<InstantiationPermissions> INSTANTIATION_PERMISSIONS = new MetaDataKey<InstantiationPermissions>() {
        private static final long serialVersionUID = 5526377385090628897L;
    };

    /**
     * Construct.
     *
     * @param roleCheckingStrategy the authorizer object
     */
    public EffMetaDataRoleAuthorizationStrategy(final IEffRoleCheckingStrategy roleCheckingStrategy) {
        super(roleCheckingStrategy);
    }

    /**
     * Authorizes the given role to create component instances of type
     * componentClass. This authorization is added to any previously authorized
     * roles.
     *
     * @param <T>
     * @param componentClass The component type that is subject for the authorization
     * @param roles          The comma separated roles that are authorized to create
     *                       component instances of type componentClass
     */
    public static final <T extends Component> void authorize(final Class<T> componentClass, final UserRole... roles) {
        final Application application = Application.get();
        InstantiationPermissions permissions = application.getMetaData(INSTANTIATION_PERMISSIONS);
        if (permissions == null) {
            permissions = new InstantiationPermissions();
            application.setMetaData(INSTANTIATION_PERMISSIONS, permissions);
        }
        permissions.authorize(componentClass, roles);
    }

    public static final <T extends Component> void authorizeIf(final IAuthorizationCondition condition, final Class<T> componentClass,
                                                               final UserRole... roles) {
        if (condition.isAuthorized()) {
            authorize(componentClass, roles);
        }
    }

    /**
     * Authorizes the given role to create component instances of type
     * componentClass. This authorization is added to any previously authorized
     * roles.
     *
     * @param <T>
     * @param componentClass The component type that is subject for the authorization
     * @param roles          The comma separated roles that are authorized to create
     *                       component instances of type componentClass
     */
    public static final <T extends Component> void authorize(final Class<T> componentClass, final EnumSet<UserRole> roles) {
        authorize(componentClass, roles.toArray(new UserRole[roles.size()]));
    }

    public static final <T extends Component> void authorizeIf(final IAuthorizationCondition condition, final Class<T> componentClass,
                                                               final EnumSet<UserRole> roles) {
        if (condition.isAuthorized()) {
            authorize(componentClass, roles);
        }
    }

    /**
     * Authorizes the given role to perform the given action on the given
     * component.
     *
     * @param component The component that is subject to the authorization
     * @param action    The action to authorize
     * @param roles     The comma separated roles to authorize
     */
    public static final void authorize(final Component component, final Action action, final UserRole... roles) {
        ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
        if (permissions == null) {
            permissions = new ActionPermissions();
            component.setMetaData(ACTION_PERMISSIONS, permissions);
        }
        permissions.authorize(action, roles);
    }

    public static final void authorizeIf(final IAuthorizationCondition condition, final Component component, final Action action,
                                         final UserRole... roles) {
        if (condition.isAuthorized()) {
            authorize(component, action, roles);
        }
    }

    /**
     * Authorizes the given role to perform the given action on the given
     * component.
     *
     * @param component The component that is subject to the authorization
     * @param action    The action to authorize
     * @param roles     The comma separated roles to authorize
     */
    public static final void authorize(final Component component, final Action action, final EnumSet<UserRole> roles) {
        authorize(component, action, roles.toArray(new UserRole[roles.size()]));
    }

    public static final void authorizeIf(final IAuthorizationCondition condition, final Component component, final Action action,
                                         final EnumSet<UserRole> roles) {
        if (condition.isAuthorized()) {
            authorize(component, action, roles);
        }
    }

    /**
     * Grants permission to all roles to create instances of the given component
     * class.
     *
     * @param <T>
     * @param componentClass The component class
     */
    public static final <T extends Component> void authorizeAll(final Class<T> componentClass) {
        Application application = Application.get();
        InstantiationPermissions authorizedRoles = application.getMetaData(INSTANTIATION_PERMISSIONS);
        if (authorizedRoles != null) {
            authorizedRoles.authorizeAll(componentClass);
        }
    }

    public static final <T extends Component> void authorizeAllIf(final IAuthorizationCondition condition, final Class<T> componentClass) {
        if (condition.isAuthorized()) {
            authorizeAll(componentClass);
        }
    }

    /**
     * Grants permission to all roles to perform the given action on the given
     * component.
     *
     * @param component The component that is subject to the authorization
     * @param action    The action to authorize
     */
    public static final void authorizeAll(final Component component, final Action action) {
        ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
        if (permissions != null) {
            permissions.authorizeAll(action);
        }
    }

    public static final void authorizeAllIf(final IAuthorizationCondition condition, final Component component, final Action action) {
        if (condition.isAuthorized()) {
            authorizeAll(component, action);
        }
    }

    /**
     * Removes permission for the given roles to create instances of the given
     * component class. There is no danger in removing authorization by calling
     * this method. If the last authorization grant is removed for a given
     * componentClass, the internal role NO_ROLE will automatically be added,
     * effectively denying access to all roles (if this was not done, all roles
     * would suddenly have access since no authorization is equivalent to full
     * access).
     *
     * @param <T>
     * @param componentClass The component type
     * @param roles          The comma separated list of roles that are no longer to be
     *                       authorized to create instances of type componentClass
     */
    public static final <T extends Component> void unauthorize(final Class<T> componentClass, final UserRole... roles) {
        final InstantiationPermissions permissions = Application.get().getMetaData(INSTANTIATION_PERMISSIONS);
        if (permissions != null) {
            permissions.unauthorize(componentClass, roles);
        }
    }

    public static final <T extends Component> void unauthorizeIf(final IAuthorizationCondition condition, final Class<T> componentClass,
                                                                 final UserRole... roles) {
        if (condition.isAuthorized()) {
            unauthorize(componentClass, roles);
        }
    }

    /**
     * Removes permission for the given roles to create instances of the given
     * component class. There is no danger in removing authorization by calling
     * this method. If the last authorization grant is removed for a given
     * componentClass, the internal role NO_ROLE will automatically be added,
     * effectively denying access to all roles (if this was not done, all roles
     * would suddenly have access since no authorization is equivalent to full
     * access).
     *
     * @param <T>
     * @param componentClass The component type
     * @param roles          The comma separated list of roles that are no longer to be
     *                       authorized to create instances of type componentClass
     */
    public static final <T extends Component> void unauthorize(final Class<T> componentClass, final EnumSet<UserRole> roles) {
        unauthorize(componentClass, roles.toArray(new UserRole[roles.size()]));
    }

    public static final <T extends Component> void unauthorizeIf(final IAuthorizationCondition condition, final Class<T> componentClass,
                                                                 final EnumSet<UserRole> roles) {
        if (condition.isAuthorized()) {
            unauthorize(componentClass, roles);
        }
    }

    /**
     * Removes permission for the given role to perform the given action on the
     * given component. There is no danger in removing authorization by calling
     * this method. If the last authorization grant is removed for a given
     * action, the internal role NO_ROLE will automatically be added,
     * effectively denying access to all roles (if this was not done, all roles
     * would suddenly have access since no authorization is equivalent to full
     * access).
     *
     * @param component The component
     * @param action    The action
     * @param roles     The comma separated list of roles that are no longer allowed
     *                  to perform the given action
     */
    public static final void unauthorize(final Component component, final Action action, final UserRole... roles) {
        final ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
        if (permissions != null) {
            permissions.unauthorize(action, roles);
        }
    }

    public static final void unauthorizeIf(final IAuthorizationCondition condition, final Component component, final Action action,
                                           final UserRole... roles) {
        if (condition.isAuthorized()) {
            unauthorize(component, action, roles);
        }
    }

    /**
     * Removes permission for the given role to perform the given action on the
     * given component. There is no danger in removing authorization by calling
     * this method. If the last authorization grant is removed for a given
     * action, the internal role NO_ROLE will automatically be added,
     * effectively denying access to all roles (if this was not done, all roles
     * would suddenly have access since no authorization is equivalent to full
     * access).
     *
     * @param component The component
     * @param action    The action
     * @param roles     The comma separated list of roles that are no longer allowed
     *                  to perform the given action
     */
    public static final void unauthorize(final Component component, final Action action, final EnumSet<UserRole> roles) {
        unauthorize(component, action, roles.toArray(new UserRole[roles.size()]));
    }

    public static final void unauthorizeIf(final IAuthorizationCondition condition, final Component component, final Action action,
                                           final EnumSet<UserRole> roles) {
        if (condition.isAuthorized()) {
            unauthorize(component, action, roles);
        }
    }

    /**
     * Grants authorization to instantiate the given class to just the role
     * NO_ROLE, effectively denying all other roles.
     *
     * @param <T>
     * @param componentClass The component class
     */
    public static final <T extends Component> void unauthorizeAll(Class<T> componentClass) {
        authorizeAll(componentClass);
        authorize(componentClass, UserRole.NO_ROLE);
    }

    public static final <T extends Component> void unauthorizeAllIf(final IAuthorizationCondition condition, Class<T> componentClass) {
        if (condition.isAuthorized()) {
            unauthorizeAll(componentClass);
        }
    }

    /**
     * Grants authorization to perform the given action to just the role
     * NO_ROLE, effectively denying all other roles.
     *
     * @param component the component that is subject to the authorization
     * @param action    the action to authorize
     */
    public static final void unauthorizeAll(final Component component, final Action action) {
        authorizeAll(component, action);
        authorize(component, action, UserRole.NO_ROLE);
    }

    public static final void unauthorizeAllIf(final IAuthorizationCondition condition, final Component component, final Action action) {
        if (condition.isAuthorized()) {
            unauthorizeAll(component, action);
        }
    }

    /**
     * Uses component level meta data to match roles for component action
     * execution.
     *
     * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
     * org.apache.wicket.authorization.Action)
     */
    @Override
    public boolean isActionAuthorized(final Component component, final Action action) {
        if (component == null) {
            throw new IllegalArgumentException("argument component has to be not null");
        }
        if (action == null) {
            throw new IllegalArgumentException("argument action has to be not null");
        }

        final EnumSet<UserRole> roles = rolesAuthorizedToPerformAction(component, action);
        if (roles != null) {
            return hasAny(roles);
        }
        return true;
    }

    /**
     * Uses application level meta data to match roles for component
     * instantiation.
     *
     * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
     */
    @Override
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> componentClass) {
        if (componentClass == null) {
            throw new IllegalArgumentException("argument componentClass cannot be null");
        }

        // as long as the interface does not use generics, we should check this
        if (!Component.class.isAssignableFrom(componentClass)) {
            throw new IllegalArgumentException("argument componentClass must be of type " + Component.class.getName());
        }

        final EnumSet<UserRole> roles = rolesAuthorizedToInstantiate(componentClass);
        if (roles != null) {
            return hasAny(roles);
        }
        return true;
    }

    /**
     * Gets the roles for creation of the given component class, or null if none
     * were registered.
     *
     * @param <T>
     * @param componentClass the component class
     * @return the roles that are authorized for creation of the componentClass,
     * or null if no specific authorization was configured
     */
    private static <T extends IRequestableComponent> EnumSet<UserRole> rolesAuthorizedToInstantiate(final Class<T> componentClass) {
        final InstantiationPermissions permissions = Application.get().getMetaData(INSTANTIATION_PERMISSIONS);
        if (permissions != null) {
            return permissions.authorizedRoles(componentClass);
        }
        return null;
    }

    /**
     * Gets the roles for the given action/component combination.
     *
     * @param component the component
     * @param action    the action
     * @return the roles for the action as defined with the given component
     */
    private static EnumSet<UserRole> rolesAuthorizedToPerformAction(final Component component, final Action action) {
        final ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
        if (permissions != null) {
            EnumSet<UserRole> rolesFor = permissions.rolesFor(action);
            return rolesFor;
        }
        return null;
    }

}

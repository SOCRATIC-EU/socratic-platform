package de.atb.socratic.authorization.strategies.annotations;

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

import de.atb.socratic.authorization.strategies.EffAbstractRoleAuthorizationStrategy;
import de.atb.socratic.authorization.strategies.IEffRoleCheckingStrategy;
import de.atb.socratic.model.UserRole;
import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.request.component.IRequestableComponent;

/**
 * AnnotationsRoleAuthorizationStrategy
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class EffAnnotationsRoleAuthorizationStrategy extends EffAbstractRoleAuthorizationStrategy {
    /**
     * Construct.
     *
     * @param roleCheckingStrategy the authorizer delegate
     */
    public EffAnnotationsRoleAuthorizationStrategy(final IEffRoleCheckingStrategy roleCheckingStrategy) {
        super(roleCheckingStrategy);
    }

    /**
     * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
     */
    @Override
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> componentClass) {
        // We are authorized unless we are found not to be
        boolean authorized = true;

        // Check class annotation first because it is more specific than package
        // annotation
        final AuthorizeInstantiation classAnnotation = componentClass.getAnnotation(AuthorizeInstantiation.class);
        if (classAnnotation != null) {
            authorized = hasAny(classAnnotation.value());
        } else {
            // Check package annotation if there is no one on the the class
            final Package componentPackage = componentClass.getPackage();
            if (componentPackage != null) {
                final AuthorizeInstantiation packageAnnotation = componentPackage.getAnnotation(AuthorizeInstantiation.class);
                if (packageAnnotation != null) {
                    authorized = hasAny(packageAnnotation.value());
                }
            }
        }

        return authorized;
    }

    @Override
    public boolean isActionAuthorized(final Component component, final Action action) {
        // Get component's class
        final Class<?> componentClass = component.getClass();

        return isActionAuthorized(componentClass, action);
    }

    protected boolean isActionAuthorized(final Class<?> componentClass, final Action action) {
        // Check for a single action
        if (!check(action, componentClass.getAnnotation(AuthorizeAction.class))) {
            return false;
        }

        // Check for multiple actions
        final AuthorizeActions authorizeActionsAnnotation = componentClass.getAnnotation(AuthorizeActions.class);
        if (authorizeActionsAnnotation != null) {
            for (final AuthorizeAction authorizeActionAnnotation : authorizeActionsAnnotation.actions()) {
                if (!check(action, authorizeActionAnnotation)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @param action                    The action to check
     * @param authorizeActionAnnotation The annotations information
     * @return False if the action is not authorized
     */
    private boolean check(final Action action, final AuthorizeAction authorizeActionAnnotation) {
        if (authorizeActionAnnotation != null) {
            if (action.getName().equals(authorizeActionAnnotation.action())) {
                UserRole[] deniedRoles = authorizeActionAnnotation.deny();
                if ((isEmpty(deniedRoles) == false) && hasAny(deniedRoles)) {
                    return false;
                }

                UserRole[] acceptedRoles = authorizeActionAnnotation.roles();
                if (!(isEmpty(acceptedRoles) || hasAny(acceptedRoles))) {
                    return false;
                }
            }
        }
        return true;
    }
}

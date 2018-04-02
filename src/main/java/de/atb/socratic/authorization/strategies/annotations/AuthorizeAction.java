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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.atb.socratic.model.UserRole;

/**
 * AuthorizeAction
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Documented
@Inherited
public @interface AuthorizeAction {

    /**
     * The action that is allowed. The default actions that are supported by
     * Wicket are <code>RENDER</code> and
     * <code>ENABLE<code> as defined as constants
     * of {@link org.apache.wicket.Component}.
     *
     * @return the action that is allowed
     * @see org.apache.wicket.Component#RENDER
     * @see org.apache.wicket.Component#ENABLE
     */
    String action();

    /**
     * The roles for this action.
     *
     * @return the roles for this action. The default is an empty array
     * (annotations do not allow null default values)
     */
    UserRole[] roles() default {};

    /**
     * The roles to deny for this action.
     *
     * @return the roles to deny for this action. The default is an empty array
     * (annotations do not allow null default values)
     */
    UserRole[] deny() default {};

}

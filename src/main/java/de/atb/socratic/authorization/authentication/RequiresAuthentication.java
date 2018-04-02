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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import de.atb.socratic.authorization.authentication.http.HttpRequestAuthenticator;
import de.atb.socratic.model.UserRole;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * RequiresBasicAuthentication
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@InterceptorBinding
@Target({TYPE, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresAuthentication {

    /**
     * Specifies the list of allowed Authentication types. Only authentication
     * types given here will be accepted, if none specified authentication is
     * ignored completely!
     *
     * @return the allowed authentication types for accessing the annotated
     * method or methods in the annotated class.
     */
    @Nonbinding
    AuthenticationType[] allowedTypes() default {AuthenticationType.SESSION, AuthenticationType.BASIC, AuthenticationType.OAUTH1};

    /**
     * Specifies the list of roles that are required to access the annotated
     * method or class.
     * <p>
     * The user roles specified here are checked against the user role that is
     * identified by the implementing {@link HttpRequestAuthenticator}. This authenticator
     * should take into account containing roles (i.e. compound roles).
     * <p>
     * Example: a method is annotated with a required role of <code>USER</code>.
     * In the system there exists a <code>MANAGER</code> role, which is a
     * compound of <code>USER</code> and <code>MANAGER</code> role. Any user
     * with the role <code>MANAGER</code> is then allowed to access the
     * annotated ressource, as the role <code>USER</code> is contained in the
     * <code>MANAGER</code> role.
     *
     * @return the required user roles for accessing the annotated method or
     * methods in the annotated class.
     */
    @Nonbinding
    UserRole[] requiredRoles() default UserRole.USER;

    /**
     * Specifies the list of user logins (usually e-Mail address) that are
     * allowed to access the annotated method or class.
     * <p>
     * <p>
     * By default this list is empty which means that every user can access the
     * method, no matter the login. Of course the role of the user must match at
     * least one of those specified though {@link #requiredRoles()}.
     *
     * @return
     */
    @Nonbinding
    String[] allowedUserLogins() default {};
}

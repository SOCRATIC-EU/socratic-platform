package de.atb.socratic.interceptor;

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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.EnumSet;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import de.atb.socratic.authorization.authentication.AuthenticationType;
import de.atb.socratic.authorization.authentication.RequiresAuthentication;
import de.atb.socratic.authorization.authentication.http.HttpRequestAuthenticator;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.exception.UserAuthenticationResponseException;
import de.atb.socratic.model.User;
import de.atb.socratic.web.qualifier.LoggedInUser;

/**
 * SecurityInterceptor
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Interceptor
@RequiresAuthentication
public class SecurityInterceptor {

    @Inject
    @LoggedInUser
    User loggedInUser;

    @AroundInvoke
    public Object manageTransaction(InvocationContext invocation) throws Exception {

        Method method = invocation.getMethod();
        RequiresAuthentication requiresAuthenticationAnnotation = getMostSpecificAnnotation(method, RequiresAuthentication.class);
        if (requiresAuthenticationAnnotation == null) {
            return invocation.proceed();
        } else {
            EnumSet<AuthenticationType> authTypes = AuthenticationType.getFromAnnotation(requiresAuthenticationAnnotation);
            handleRequest(method, authTypes, requiresAuthenticationAnnotation);
            return invocation.proceed();
        }
    }

    private void handleRequest(Method method, EnumSet<AuthenticationType> authTypes,
                               RequiresAuthentication requiresAuthenticationAnnotation) throws UserAuthenticationException {
        if ((loggedInUser != null) || (loggedInUser.getCurrentEmployment() != null)) {
            if (loggedInUser == null) {
                throw new UserAuthenticationResponseException("Your user could not be authenticated!");
            } else if (loggedInUser.getCurrentEmployment() == null) {
                throw new UserAuthenticationResponseException("You did not specify your current employment correctly!");
            }
        }
        HttpRequestAuthenticator.authenticate(loggedInUser, loggedInUser.getCurrentEmployment(), requiresAuthenticationAnnotation);
    }

    private <T extends Annotation> T getMostSpecificAnnotation(Method method, Class<T> clazz) {
        return this.getMethodBasedAnnotation(method, clazz) != null ? this.getMethodBasedAnnotation(method, clazz) : this
                .getClassBasedAnnotation(method, clazz);
    }

    private <T extends Annotation> T getMethodBasedAnnotation(Method method, Class<T> clazz) {
        if (method.isAnnotationPresent(clazz)) {
            return method.getAnnotation(clazz);
        }
        return null;
    }

    private <T extends Annotation> T getClassBasedAnnotation(Method method, Class<T> clazz) {
        if (method.getDeclaringClass().isAnnotationPresent(clazz)) {
            return method.getDeclaringClass().getAnnotation(clazz);
        }
        return null;
    }

}

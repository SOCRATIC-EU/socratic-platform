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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import de.atb.socratic.qualifier.Conversational;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.bound.Bound;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequest;
import org.jboss.weld.context.bound.MutableBoundRequest;
import org.jboss.weld.context.http.Http;

@Conversational
@Interceptor
public class ConversationalInterceptor {

    @Inject
    @Http
    ConversationContext context;

    @Inject
    @Bound
    BoundConversationContext boundContext;

    @AroundInvoke
    @AroundTimeout
    public Object wrapInConversation(InvocationContext invocation)
            throws Exception {

        BoundRequest storage = null;

        if (!context.isActive() && !boundContext.isActive()) {
            Map<String, Object> session = new HashMap<String, Object>();
            Map<String, Object> request = new HashMap<String, Object>();
            storage = new MutableBoundRequest(request, session);
            boundContext.associate(storage);
            boundContext.activate();
        }

        try {
            return invocation.proceed();
        } finally {
            if (storage != null) {
                boundContext.deactivate();
                boundContext.dissociate(storage);
            }
        }
    }
}

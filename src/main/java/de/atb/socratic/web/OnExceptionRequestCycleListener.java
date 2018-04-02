/**
 *
 */
package de.atb.socratic.web;

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

import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;

/**
 * @author ATB
 */
public class OnExceptionRequestCycleListener extends AbstractRequestCycleListener {

    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception ex) {
        Session session = Session.get();
        if (session != null && session instanceof EFFSession) {
            if (((EFFSession) session).isAuthenticated()) {
                return createPageRequestHandler(new PageProvider(new ErrorPage(ex)));
            } else {
                return createPageRequestHandler(new PageProvider(new NotLoggedInErrorPage()));
            }
        } else {
            return createPageRequestHandler(new PageProvider(new NotLoggedInErrorPage()));
        }
    }

    private RenderPageRequestHandler createPageRequestHandler(PageProvider pageProvider) {
        RequestCycle requestCycle = RequestCycle.get();

        if (requestCycle == null) {
            throw new IllegalStateException("there is no current request cycle attached to this thread");
        }

        /*
         * Use NEVER_REDIRECT policy to preserve the original page's URL for
         * non-Ajax requests and always redirect for ajax requests
         */
        RenderPageRequestHandler.RedirectPolicy redirect = RenderPageRequestHandler.RedirectPolicy.NEVER_REDIRECT;

        if (isProcessingAjaxRequest()) {
            redirect = RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT;
        }

        return new RenderPageRequestHandler(pageProvider, redirect);
    }

    private boolean isProcessingAjaxRequest() {
        RequestCycle rc = RequestCycle.get();
        Request request = rc.getRequest();
        return request instanceof WebRequest && ((WebRequest) request).isAjax();
    }

}

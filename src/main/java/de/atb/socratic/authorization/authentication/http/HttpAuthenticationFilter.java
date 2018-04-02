package de.atb.socratic.authorization.authentication.http;

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

import java.io.IOException;
import java.security.Principal;
import java.util.EnumSet;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import de.atb.socratic.authorization.authentication.AuthenticationType;
import de.atb.socratic.authorization.authentication.UserEmploymentPrincipal;
import de.atb.socratic.exception.RegistrationNotConfirmedException;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.User;
import org.jboss.solder.logging.Logger;

/**
 * AuthenticationFilter
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@WebFilter(filterName = "HttpAuthenticationFilter", urlPatterns = "/rest/*", dispatcherTypes = {DispatcherType.REQUEST})
public class HttpAuthenticationFilter implements Filter {

    @Inject
    Instance<HttpRequestAuthenticator> availableAuthenticators;

    @Inject
    Logger logger;

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        _doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void _doFilter(HttpServletRequest httpRequest, HttpServletResponse response, FilterChain filterChain) throws IOException,
            ServletException {
        for (HttpRequestAuthenticator authenticator : availableAuthenticators) {
            try {
                boolean canHandleRequest = authenticator.canHandleAuthentication(EnumSet.of(AuthenticationType.BASIC,
                        AuthenticationType.OAUTH1));
                boolean verifyRequest = authenticator.verifyRequest(httpRequest);
                if (canHandleRequest && verifyRequest) {
                    logger.info("Trying to authenticate http session with " + authenticator.getAuthenticationType() + " method");
                    User user = authenticator.getAuthenticatedUserFromRequest(httpRequest);
                    if (user == null) {
                        logger.info("Could not get user from request!");
                    }
                    try {
                        Employment currentEmployment = authenticator.getCurrentEmploymentFromRequest(user, httpRequest);
                        if ((user != null) && (currentEmployment != null)) {
                            final UserEmploymentPrincipal principal = new UserEmploymentPrincipal(user, currentEmployment,
                                    authenticator.getAuthenticationType());
                            httpRequest = new HttpServletRequestWrapper(httpRequest) {
                                @Override
                                public Principal getUserPrincipal() {
                                    return principal;
                                }
                            };
                            httpRequest.getSession().setAttribute(Principal.class.getName(), principal);
                        } else {
                            logger.info("Could not get employment from request!");
                        }
                        break;
                    } catch (RuntimeException e) {
                        throw new ServletException(e);
                    }
                }
            } catch (SecurityException e) {
                throw new ServletException(e);
            } catch (IllegalArgumentException e) {
                throw new ServletException(e);
            } catch (UserAuthenticationException e) {
                throw new ServletException(e);
            } catch (RegistrationNotConfirmedException e) {
                throw new ServletException(e);
            }
        }
        filterChain.doFilter(httpRequest, response);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}

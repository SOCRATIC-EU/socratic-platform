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
import java.util.EnumSet;
import java.util.StringTokenizer;

import javax.ejb.Singleton;
import javax.servlet.http.HttpServletRequest;

import de.atb.socratic.authorization.authentication.AuthenticationType;
import de.atb.socratic.exception.AutoCreatedLDAPUserException;
import de.atb.socratic.exception.AutoCreatedLDAPUserWithoutCompanyException;
import de.atb.socratic.exception.RegistrationNotConfirmedException;
import de.atb.socratic.exception.UserAuthenticationException;
import de.atb.socratic.model.User;
import de.atb.socratic.qualifier.Conversational;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.resteasy.util.Base64;

/**
 * BasicAuthenticator
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Singleton
@Conversational
public class BasicHttpAuthenticator extends HttpRequestAuthenticator {

    /**
     *
     */
    private static final long serialVersionUID = -1975431145661829393L;

    private static final String HTTP_HEADER_KEY_AUTH = "authorization";
    private static final String HTTP_HEADER_KEY_PARAM = "basic";

    @Override
    public boolean canHandleAuthentication(EnumSet<AuthenticationType> types) {
        return types.contains(AuthenticationType.BASIC);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.eff.rest.authentication.IAuthenticator#requestContainsValidAuthType
     * (org.jboss.resteasy.spi.HttpRequest)
     */
    @Override
    public boolean verifyRequest(HttpServletRequest request) {
        String property = getAuthHeaderProperty(request);
        return (property != null) && property.toLowerCase().startsWith(HTTP_HEADER_KEY_PARAM);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.eff.rest.authentication.Authenticator#getUserFromRequest(org.jboss
     * .resteasy.spi.HttpRequest)
     */
    @Override
    public User getAuthenticatedUserFromRequest(HttpServletRequest request) throws UserAuthenticationException,
            RegistrationNotConfirmedException {
        String headerValue = getAuthHeaderProperty(request);
        try {
            String base64EncodedCredentials = headerValue.substring(HTTP_HEADER_KEY_PARAM.length() + 1).trim();
            String[] credentials = getUsernameAndPassword(base64EncodedCredentials);
            if ((credentials == null) || (credentials.length < 2)) {
                throw new UnauthorizedException("No authentication credentials provided!");
            }
            String username = credentials[0];
            String password = credentials[1];
            return authService.authenticate(username, password);
        } catch (IOException e) {
            return null;
        } catch (AutoCreatedLDAPUserException e) {
            return e.getAutoCreatedUser();
        } catch (AutoCreatedLDAPUserWithoutCompanyException e) {
            return e.getAutoCreatedUser();
        }
    }

    private String getAuthHeaderProperty(HttpServletRequest request) {
        String authProperty = request.getHeader(HTTP_HEADER_KEY_AUTH);
        if ((authProperty != null) && (!authProperty.isEmpty())) {
            return authProperty;
        }
        return null;
    }

    private String[] getUsernameAndPassword(String base64EncodedCredentials) throws IOException {
        byte[] raw = Base64.decode(base64EncodedCredentials);
        StringTokenizer tokenizer = new StringTokenizer(new String(raw), ":");
        String[] tokens = new String[tokenizer.countTokens()];
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            tokens[i++] = tokenizer.nextToken();
        }
        return tokens;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.eff.rest.security.authentication.HttpRequestAuthenticator#
     * getAuthenticationType()
     */
    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.BASIC;
    }

}

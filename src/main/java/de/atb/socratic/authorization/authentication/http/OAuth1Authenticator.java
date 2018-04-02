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

import java.util.EnumSet;

import javax.ejb.Singleton;
import javax.servlet.http.HttpServletRequest;

import de.atb.socratic.authorization.authentication.AuthenticationType;
import de.atb.socratic.model.User;
import org.jboss.resteasy.spi.UnauthorizedException;

/**
 * OAuth1Authenticator
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Singleton
public class OAuth1Authenticator extends HttpRequestAuthenticator {

    /**
     *
     */
    private static final long serialVersionUID = -968001329482593560L;

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.eff.rest.authentication.Authenticator#getUserFromRequest(org.jboss
     * .resteasy.spi.HttpRequest)
     */
    @Override
    public User getAuthenticatedUserFromRequest(HttpServletRequest request) throws UnauthorizedException {
        // implement
        throw new UnauthorizedException("OAuth1 is not implemented yet!");
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.eff.rest.authentication.IAuthenticator#canHandleAuthentication
     * (java.util.EnumSet)
     */
    @Override
    public boolean canHandleAuthentication(EnumSet<AuthenticationType> types) {
        return types.contains(AuthenticationType.OAUTH1);
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
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.eff.rest.security.authentication.HttpRequestAuthenticator#
     * getAuthenticationType()
     */
    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.OAUTH1;
    }

}

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

import java.net.HttpURLConnection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.resteasy.auth.oauth.OAuthConsumer;
import org.jboss.resteasy.auth.oauth.OAuthException;
import org.jboss.resteasy.auth.oauth.OAuthRequestToken;
import org.jboss.resteasy.auth.oauth.OAuthToken;

/**
 * OAuthProvider
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class OAuthProvider implements org.jboss.resteasy.auth.oauth.OAuthProvider {

    private static ConcurrentHashMap<String, OAuthConsumer> consumers;
    private ConcurrentHashMap<String, OAuthRequestToken> requestTokens = new ConcurrentHashMap<String, OAuthRequestToken>();
    private ConcurrentHashMap<String, OAuthToken> accessTokens = new ConcurrentHashMap<String, OAuthToken>();

    private static final String realm = "SOCRATIC";

    static {
        consumers = new ConcurrentHashMap<String, OAuthConsumer>();
        consumers.put("key", new OAuthConsumer("key", "secret", null, null));
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthConsumerRegistration#registerConsumer
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public OAuthConsumer registerConsumer(String consumerKey, String displayName, String connectURI) throws OAuthException {
        OAuthConsumer consumer = consumers.get(consumerKey);
        if (consumer == null) {
            return consumer;
        }
        consumer = new OAuthConsumer(consumerKey, "secret", displayName, connectURI);
        consumers.putIfAbsent(consumerKey, consumer);
        return consumer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.resteasy.auth.oauth.OAuthConsumerRegistration#
     * registerConsumerScopes(java.lang.String, java.lang.String[])
     */
    @Override
    public void registerConsumerScopes(String consumerKey, String[] scopes) throws OAuthException {
        OAuthConsumer consumer = _getConsumer(consumerKey);
        consumer.setScopes(scopes);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.resteasy.auth.oauth.OAuthConsumerRegistration#
     * registerConsumerPermissions(java.lang.String, java.lang.String[])
     */
    @Override
    public void registerConsumerPermissions(String consumerKey, String[] permissions) throws OAuthException {
        // ignore
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.resteasy.auth.oauth.OAuthProvider#getRealm()
     */
    @Override
    public String getRealm() {
        return realm;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#getConsumer(java.lang.String)
     */
    @Override
    public OAuthConsumer getConsumer(String consumerKey) throws OAuthException {
        return _getConsumer(consumerKey);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#getRequestToken(java.lang
     * .String, java.lang.String)
     */
    @Override
    public OAuthRequestToken getRequestToken(String consumerKey, String requestToken) throws OAuthException {
        OAuthRequestToken token = getRequestToken(requestToken);
        if ((consumerKey != null) && !token.getConsumer().getKey().equals(consumerKey)) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "No such consumer key " + consumerKey);
        }
        return token;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#getAccessToken(java.lang.
     * String, java.lang.String)
     */
    @Override
    public OAuthToken getAccessToken(String consumerKey, String accessToken) throws OAuthException {
        // get is atomic
        OAuthToken ret = accessTokens.get(accessToken);
        if (ret == null) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "No such access key " + accessToken);
        }
        if (!ret.getConsumer().getKey().equals(consumerKey)) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "Consumer is invalid");
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#makeRequestToken(java.lang
     * .String, java.lang.String, java.lang.String[], java.lang.String[])
     */
    @Override
    public OAuthToken makeRequestToken(String consumerKey, String callback, String[] scopes, String[] permissions) throws OAuthException {
        OAuthRequestToken token = doMakeRequestToken(consumerKey, callback, scopes, permissions);
        return token;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#makeAccessToken(java.lang
     * .String, java.lang.String, java.lang.String)
     */
    @Override
    public OAuthToken makeAccessToken(String consumerKey, String requestToken, String verifier) throws OAuthException {
        OAuthRequestToken token = verifyAndRemoveRequestToken(consumerKey, requestToken, verifier);
        return doMakeAccessTokens(token);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#authoriseRequestToken(java
     * .lang.String, java.lang.String)
     */
    @Override
    public String authoriseRequestToken(String consumerKey, String requestToken) throws OAuthException {
        String verifier = makeRandomString();
        doGetRequestToken(consumerKey, requestToken).setVerifier(verifier);
        return verifier;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#checkTimestamp(org.jboss.
     * resteasy.auth.oauth.OAuthToken, long)
     */
    @Override
    public void checkTimestamp(OAuthToken token, long timestamp) throws OAuthException {
        if (token.getTimestamp() > timestamp) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "Invalid timestamp " + timestamp);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.jboss.resteasy.auth.oauth.OAuthProvider#convertPermissionsToRoles
     * (java.lang.String[])
     */
    @Override
    public Set<String> convertPermissionsToRoles(String[] permissions) {
        return null;
    }

    private OAuthRequestToken doGetRequestToken(String customerKey, String requestKey) throws OAuthException {
        // get is atomic
        OAuthRequestToken ret = requestTokens.get(requestKey);
        checkCustomerKey(ret, customerKey);
        if (ret == null) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "No such request key " + requestKey);
        }
        return ret;
    }

    public OAuthRequestToken verifyAndRemoveRequestToken(String customerKey, String requestToken, String verifier) throws OAuthException {
        OAuthRequestToken request = getRequestToken(requestToken);
        checkCustomerKey(request, customerKey);
        // check the verifier, which is only set when the request token was
        // accepted
        if ((verifier == null) || !verifier.equals(request.getVerifier())) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "Invalid verifier code for token " + requestToken);
        }
        // then let's go through and exchange this for an access token
        return requestTokens.remove(requestToken);
    }

    public OAuthRequestToken getRequestToken(String requestToken) throws OAuthException {
        OAuthRequestToken token = requestTokens.get(requestToken);
        if (token == null) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "No such request token " + requestToken);
        }
        return token;
    }

    private void checkCustomerKey(OAuthToken token, String customerKey) throws OAuthException {
        if ((customerKey != null) && !customerKey.equals(token.getConsumer().getKey())) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "Invalid customer key");
        }
    }

    private OAuthRequestToken doMakeRequestToken(String consumerKey, String callback, String[] scopes, String[] permissions)
            throws OAuthException {
        OAuthConsumer consumer = _getConsumer(consumerKey);
        String newToken;
        do {
            newToken = makeRandomString();
        } while (requestTokens.containsKey(newToken));
        OAuthRequestToken token = new OAuthRequestToken(newToken, makeRandomString(), callback, scopes, permissions, 3600, consumer);
        requestTokens.put(token.getToken(), token);
        return token;
    }

    protected OAuthConsumer _getConsumer(String consumerKey) throws OAuthException {
        OAuthConsumer ret = consumers.get(consumerKey);
        if (ret == null) {
            throw new OAuthException(HttpURLConnection.HTTP_UNAUTHORIZED, "No such consumer key " + consumerKey);
        }
        return ret;
    }

    private OAuthToken doMakeAccessTokens(OAuthRequestToken requestToken) throws OAuthException {
        String newToken;
        do {
            newToken = makeRandomString();
        } while (accessTokens.containsKey(newToken));
        OAuthToken token = new OAuthToken(newToken, makeRandomString(), requestToken.getScopes(), requestToken.getPermissions(), -1,
                requestToken.getConsumer());
        accessTokens.put(token.getToken(), token);
        return token;
    }

    private static String makeRandomString() {
        return UUID.randomUUID().toString();
    }

}

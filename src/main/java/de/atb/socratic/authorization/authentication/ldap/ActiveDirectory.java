package de.atb.socratic.authorization.authentication.ldap;

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

import java.util.Hashtable;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.jboss.solder.logging.Logger;

import static javax.naming.directory.SearchControls.SUBTREE_SCOPE;

@ApplicationScoped
@Singleton
public class ActiveDirectory {

    private static String[] userAttributes = {"distinguishedName", "cn", "name", "mail", "uid", "sn", "givenname", "samaccountname",
            "userPrincipalName"};

    private static final boolean enabled = false;

    @Inject
    Logger logger;

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Used to authenticate a user given a username/password. Domain name is
     * derived from the username if it is in the form of user@domain.tld or
     * Domain\\user or fully qualified domain name of the host machine.
     */
    public LdapContext getConnection(String username, String password) throws NamingException {
        return getConnection(username, password, null, null);
    }

    /**
     * Used to authenticate a user given a username/password and domain name.
     */
    public LdapContext getConnection(String username, String password, String domainName) throws NamingException {
        return getConnection(username, password, domainName, null);
    }

    public String getUsernameFromPrincipal(String principal) {
        String userName = null;
        if (principal.contains("@")) {
            userName = principal.substring(0, principal.indexOf("@"));
        } else if (principal.contains("\\")) {
            userName = principal.substring(principal.indexOf("\\") + 1);
        }
        return userName;
    }

    public String getDomainNameFromPrincipal(String prinicpal) {
        String domainName = null;
        if (prinicpal.contains("@")) {
            domainName = prinicpal.substring(prinicpal.indexOf("@") + 1);
        } else if (prinicpal.contains("\\")) {
            domainName = prinicpal.substring(0, prinicpal.indexOf("\\"));
        } else {
            try {
                String fqdn = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                if (fqdn.split("\\.").length > 1) {
                    domainName = fqdn.substring(fqdn.indexOf(".") + 1);
                }
            } catch (java.net.UnknownHostException e) {
            }
        }
        return domainName;
    }

    /**
     * Used to authenticate a user given a username/password and domain name.
     * Provides an option to identify a specific a Active Directory server.
     */
    public LdapContext getConnection(String username, String password, String domainName, String serverName) throws CommunicationException, NamingException {

        if (domainName == null) {
            if (username.contains("@")) {
                domainName = username.substring(username.indexOf("@") + 1);
                username = username.substring(0, username.indexOf("@"));
            } else if (username.contains("\\")) {
                domainName = username.substring(0, username.indexOf("\\"));
                username = username.substring(username.indexOf("\\") + 1);
            } else {
                try {
                    String fqdn = java.net.InetAddress.getLocalHost().getCanonicalHostName();
                    if (fqdn.split("\\.").length > 1) {
                        domainName = fqdn.substring(fqdn.indexOf(".") + 1);
                    }
                } catch (java.net.UnknownHostException e) {
                }
            }
        }

        if (password != null) {
            password = password.trim();
            if (password.length() == 0) {
                password = null;
            }
        }

        // bind by using the specified username/password
        Hashtable<String, String> props = new Hashtable<String, String>();
        String principalName = username + "@" + domainName;
        props.put(Context.SECURITY_PRINCIPAL, principalName);
        if (password != null) {
            props.put(Context.SECURITY_CREDENTIALS, password);
        }

        String ldapURL = "ldap://" + ((serverName == null) ? domainName : serverName + "." + domainName) + '/';
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, ldapURL);
        props.put(Context.REFERRAL, "follow");
        try {
            return new InitialLdapContext(props, null);
        } catch (CommunicationException e) {
            logger.warn("Failed to connect to " + domainName + ((serverName == null) ? "" : " through " + serverName), e);
            throw e;
        } catch (NamingException e) {
            logger.warn(("Failed to authenticate " + username + "@" + domainName
                    + ((serverName == null) ? "" : " through " + serverName)), e);
            throw e;
        }
    }

    /**
     * Used to check whether a username is valid.
     *
     * @param username A username to validate (e.g. "peter", "peter@acme.com", or
     *                 "ACME\peter").
     */
    public ActiveDirectoryUser getUser(String username, LdapContext context) {
        try {
            String domainName = null;
            if (username.contains("@")) {
                domainName = username.substring(username.indexOf("@") + 1);
                username = username.substring(0, username.indexOf("@"));
            } else if (username.contains("\\")) {
                domainName = username.substring(0, username.indexOf("\\"));
                username = username.substring(username.indexOf("\\") + 1);
            } else {
                String authenticatedUser = (String) context.getEnvironment().get(Context.SECURITY_PRINCIPAL);
                if (authenticatedUser.contains("@")) {
                    domainName = authenticatedUser.substring(authenticatedUser.indexOf("@") + 1);
                }
            }

            if (domainName != null) {
                String principalName = username + "@" + domainName;
                SearchControls controls = new SearchControls();
                controls.setSearchScope(SUBTREE_SCOPE);
                controls.setReturningAttributes(userAttributes);
                NamingEnumeration<SearchResult> answer = context
                        .search(toDC(domainName),
                                "(&(|(userPrincipalName="
                                        + principalName
                                        + ")(sAMAccountName="
                                        + username
                                        + "))(objectClass=user)(!(userAccountControl:1.2.840.113556.1.4.804:=18)))",
                                controls);
                if (answer.hasMore()) {
                    Attributes attr = answer.next().getAttributes();
                    Attribute user = attr.get("userPrincipalName");
                    if (user == null) {
                        user = attr.get("sAMAccountName");
                    }
                    if (user != null) {
                        return new ActiveDirectoryUser(attr, domainName);
                    }
                }
            }
        } catch (NamingException e) {
            logger.warn(e.getExplanation(), e);
        }
        return null;
    }

    public Attributes getGroup(String groupDN, LdapContext context) {
        try {
            SearchControls controls = new SearchControls();
            controls.setSearchScope(SUBTREE_SCOPE);
//			controls.setReturningAttributes(userAttributes);
            // change accordingly to real DC!!!
            NamingEnumeration<SearchResult> answer = context.search(toDC("atb-bremen.de"), "(&(distinguishedName=" + groupDN
                    + ")(objectClass=group))", controls);
            if (answer.hasMore()) {
                Attributes attr = answer.next().getAttributes();
                return attr;
            }
        } catch (NamingException e) {
            // e.printStackTrace();
        }
        return null;
    }

    /**
     * isGroupMember -- determine if user belongs to an LDAP group
     * <p>
     * #param group name of group to examine
     * #param user name user to search for emembership+-
     *
     * @throws NamingException
     */
    public boolean isGroupMember(String group, String user, LdapContext context) throws NamingException {
        return isGroupMember(group, getUser(user, context), context);
    }

    /**
     * isGroupMember -- determine if user belongs to an LDAP group
     * <p>
     * #param group name of group to examine
     * #param user name user to search for emembership+-
     *
     * @throws NamingException
     */
    public boolean isGroupMember(String group, ActiveDirectoryUser user, LdapContext context) throws NamingException {
        Attributes attr = getGroup(group, context);
        Attribute attribute = attr.get("member");
        @SuppressWarnings("unchecked")
        NamingEnumeration<String> all = (NamingEnumeration<String>) attribute.getAll();
        while (all.hasMore()) {
            String userDN = all.next();
            if (user.getDistinguishedName().equals(userDN)) {
                return true;
            }
        }
        return false;
    }

    private String toDC(String domainName) {
        StringBuilder buf = new StringBuilder();
        for (String token : domainName.split("\\.")) {
            if (token.length() == 0) {
                continue; // defensive check
            }
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append("DC=").append(token);
        }
        return buf.toString();
    }

    /**
     * Used to represent a User in Active Directory
     */
    public class ActiveDirectoryUser {

        private Attributes attr;

        private String domainName = "example.com";

        public ActiveDirectoryUser() {
            this.attr = new BasicAttributes();
        }

        public ActiveDirectoryUser(Attributes attr, String domainName) {
            this.attr = attr;
            this.domainName = domainName;
        }

        public String getSurname() {
            try {
                return attr.get("sn") != null ? (String) attr.get("sn").get() : null;
            } catch (NamingException e) {
            }
            return null;
        }

        public String getFirstname() {
            try {
                return attr.get("givenName") != null ? (String) attr.get("givenName").get() : null;
            } catch (NamingException e) {
            }
            return null;
        }

        public String getEMail() {
            try {
                return attr.get("mail") != null ? (String) attr.get("mail").get() : null;
            } catch (NamingException e) {
            }
            return null;
        }

        public String getLogin() {
            try {
                return attr.get("userPrincipalName") != null ?
                        (String) attr.get("userPrincipalName").get() :
                        (String) attr.get("sAMAccountName").get() + "@" + domainName;
            } catch (NamingException e) {
            }
            return null;
        }

        public String getCommonName() {
            try {
                return attr.get("cn") != null ? (String) attr.get("cn").get() : null;
            } catch (NamingException e) {
            }
            return null;
        }

        public String getDistinguishedName() {
            try {
                return attr.get("distinguishedName") != null ? (String) attr.get("distinguishedName").get() : null;
            } catch (NamingException e) {
            }
            return null;
        }

        @Override
        public String toString() {
            return getDistinguishedName();
        }

    }
}

package de.atb.socratic.service.notification;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.util.ConfigFileHandler;
import de.atb.socratic.web.provider.UrlProvider;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.jboss.solder.logging.Logger;

import static javax.mail.Message.RecipientType.TO;

/**
 * @author ATB
 */
public abstract class MailNotificationService<T> extends AbstractService<T> {

    private static final long serialVersionUID = -1904730256327585616L;

    private static final boolean SMTP_TLS_ENABLED = false;

    private static final String CHARSET = "UTF-8";

    private static final String CONTENT_TYPE = "text/html; charset=UTF-8";

    private static final String HOMEPAGE_LINK = "HOMEPAGE_LINK";

    // inject a logger
    @Inject
    protected Logger logger;

    @SuppressWarnings("WeakerAccess")
    @Inject
    UrlProvider urlProvider;

    @Inject
    ConfigFileHandler configFileHandler;

    private Session session;

    private VelocityContext ctx;

    protected String template;

    protected String subject;

    protected String from;

    @SuppressWarnings({"unchecked", "unused"})
    protected MailNotificationService() {
        this((Class<T>) Void.class);
    }

    /**
     *
     */
    protected MailNotificationService(Class<T> clazz) {
        super(clazz);
        ctx = new VelocityContext();
    }

    protected void setHomepageLink() {
        if (Application.exists()) {
            setValue(HOMEPAGE_LINK, getHomepageLink(Application.get().getHomePage()));
        } else {
            setValue(HOMEPAGE_LINK, "https://www.atb-bremen.de/socratic-platform");
        }
    }

    protected void setValue(String name, Object value) {
        ctx.put(name, value);
    }

    void sendMessage(String... addresses) throws NotificationException {
        try {
            send(addresses);
            logger.info(String.format("Successfully sent message to [%s]", StringUtils.join(addresses, ",")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new NotificationException(String.format("Exception while sending message to [%s]",
                    StringUtils.join(addresses, ",")), e);
        }
    }

    private String getHomepageLink(Class<? extends Page> homepage) {
        return urlProvider.urlFor(homepage);
    }

    private Session getSession() {
        if (session == null) {
            final Properties mailProperties = new Properties();
            mailProperties.put("mail.smtp.host", configFileHandler.getSMTPHost());
            mailProperties.put("mail.smtp.port", configFileHandler.getSMTPPort());
            mailProperties.put("mail.smtp.starttls.enable", SMTP_TLS_ENABLED);
            // always authenticate at SMTP server
            mailProperties.put("mail.smtp.auth", true);

            session = Session.getInstance(mailProperties, new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            configFileHandler.getMailNotificationUserName(),
                            configFileHandler.getMailNotificationPw());
                }
            });
        }
        return session;
    }

    private void send(String... addresses) throws Exception {
        final Message message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(configFileHandler.getMailNotificationUserName(), from, CHARSET));
        for (String address : addresses) {
            message.addRecipient(TO, new InternetAddress(address));
        }
        message.setContent(getContent(), CONTENT_TYPE);
        message.setSubject(subject);

        Transport.send(message);
    }

    private String getContent() throws Exception {
        BufferedReader reader = null;
        try {
            reader = loadTemplate(template);
            final StringWriter message = new StringWriter();
            Velocity.evaluate(ctx, message, "message", reader);
            return message.toString();
        } catch (Exception e) {
            logger.error(String.format("Exception while evaluating message template [%s]", template), e);
            throw e;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private BufferedReader loadTemplate(String template) throws IOException {
        final InputStream is = this.getClass().getResourceAsStream(template);
        // resource not found --> throw exception
        if (is == null) {
            throw new IOException(String.format("Could not find template %s", template));
        }
        // noinspection ResultOfMethodCallIgnored
        is.available();
        return new BufferedReader(new InputStreamReader(is));
    }

}

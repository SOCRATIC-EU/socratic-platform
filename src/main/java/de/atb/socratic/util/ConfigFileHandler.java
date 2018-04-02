package de.atb.socratic.util;

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

import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Singleton
@ApplicationScoped
public class ConfigFileHandler implements Serializable {

    private static final String CONFIG_PROPERTIES = "config.properties";

    private static final long serialVersionUID = 7617782923344156164L;

    // initial model data
    private static String ADMIN_EMAIL = "socratic-admin-email";
    private static String ADMIN_PASSWORD = "socratic-admin-pw";

    // mail notification
    private static String SMTP_HOST = "smtp-host";
    private static String SMTP_PORT = "smtp-port";
    private static String MAIL_NOTIFICATION_USERNAME = "mailNotification-userName";
    private static String MAIL_NOTIFICATION_PASSWORD = "mailNotification-password";

    // inject a logger
    @Inject
    Logger logger;

    private final Properties configProperties = new Properties();

    @PostConstruct
    public void loadPropertiesFromConfigfiles() {
        logger.info("Trying to load config file..");
        try (final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
            this.configProperties.load(inputStream);
        } catch (Exception e) {
            logger.error("Exception reading config file : " + CONFIG_PROPERTIES);
            throw new RuntimeException(e);
        }
    }

    public String getAdminEmail() {
        return configProperties.getProperty(ADMIN_EMAIL);
    }

    public String getAdminPW() {
        return configProperties.getProperty(ADMIN_PASSWORD);
    }

    public String getSMTPHost() {
        return configProperties.getProperty(SMTP_HOST);
    }

    public String getSMTPPort() {
        return configProperties.getProperty(SMTP_PORT);
    }

    public String getMailNotificationUserName() {
        return configProperties.getProperty(MAIL_NOTIFICATION_USERNAME);
    }

    public String getMailNotificationPw() {
        return configProperties.getProperty(MAIL_NOTIFICATION_PASSWORD);
    }

}

/**
 *
 */
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

import javax.enterprise.context.ApplicationScoped;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.User;

/**
 * @author ATB
 */
@ApplicationScoped
public class ResetPasswordMailService extends MailNotificationService<Void> {

    /**
     *
     */
    private static final long serialVersionUID = 1232965101471750486L;
    private static final String TEMPLATE_RESET_PASSWORD_LINK = "reset_password_template.html";
    private static final String TEMPLATE_RESET_PASSWORD_SUCCESS = "reset_password_success_template.html";
    private static final String FULLNAME = "FULLNAME";
    private static final String HOMEPAGE_LINK = "HOMEPAGE_LINK";
    private static final String RESET_PASSWORD_LINK = "RESET_PASSWORD_LINK";
    private static final String FROM = "SOCRATIC Support";
    private static final String SUBJECT = "SOCRATIC Password Reset";

    /**
     *
     */
    public ResetPasswordMailService() {
        super(Void.class);
        this.from = FROM;
        this.subject = SUBJECT;
    }

    /**
     * @param user
     * @param resetPasswordLink
     * @param homePageLink
     * @throws NotificationException
     */
    public void sendResetPWLink(User user, String resetPasswordLink, String homePageLink) throws NotificationException {
        this.template = TEMPLATE_RESET_PASSWORD_LINK;

        setValue(RESET_PASSWORD_LINK, resetPasswordLink);
        setValue(FULLNAME, user.getNickName());
        setValue(HOMEPAGE_LINK, homePageLink);
        sendMessage(user.getEmail());
    }

    /**
     * @param user
     * @throws NotificationException
     */
    public void sendResetPWSuccessMessage(User user) throws NotificationException {
        this.template = TEMPLATE_RESET_PASSWORD_SUCCESS;

        setValue(FULLNAME, user.getNickName());
        sendMessage(user.getEmail());
    }

}

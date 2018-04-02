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
public class RegistrationMailService extends MailNotificationService<Void> {

    /**
     *
     */
    private static final long serialVersionUID = -7158718344563550354L;
    private static final String TEMPLATE_REGISTRATION = "registration_template.html";
    private static final String TEMPLATE_INVITATION = "invitation_template.html";
    // template to send a message that registration has been confirmed or
    // cancelled
    private static final String TEMPLATE_REGISTRATION_COMPLETE = "registration_complete_template.html";
    private static final String CONFIRMATION_LINK = "CONFIRMATION_LINK";
    private static final String CANCEL_LINK = "CANCEL_LINK";
    private static final String EMAIL = "EMAIL";
    private static final String FULLNAME = "FULLNAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String COMPANY = "COMPANY";
    private static final String HOST = "HOST";
    private static final String STATUS = "STATUS";
    private static final String CONFIRMED = "confirmed";
    private static final String CANCELLED = "cancelled";
    private static final String SUBJECT_REGISTRATION = "Welcome to SOCRATIC!";
    private static final String SUBJECT_CANCELLED = "Goodbye from SOCRATIC.";

    /**
     *
     */
    public RegistrationMailService() {
        super(Void.class);
    }

    /**
     * @param user
     * @param confirmationLink
     * @param cancelLink
     * @param homePageLink
     * @throws NotificationException
     */
    public void sendRegistrationConfirmationMessage(User user,
                                                    String confirmationLink, String cancelLink, String homePageLink)
            throws NotificationException {
        this.template = TEMPLATE_REGISTRATION;
        this.subject = SUBJECT_REGISTRATION;
        this.from = this.subject;

        setValue(CONFIRMATION_LINK, confirmationLink);
        setValue(CANCEL_LINK, cancelLink);
        setValue(EMAIL, user.getEmail());
        setValue(FULLNAME, user.getNickName());
        setHomepageLink();
        sendMessage(user.getEmail());
    }

    /**
     * @param user
     * @param homePageLink
     * @throws NotificationException
     */
    public void sendRegistrationConfirmedMessage(User user, String homePageLink)
            throws NotificationException {
        sendRegistrationCompleteMessage(user, homePageLink,
                SUBJECT_REGISTRATION, CONFIRMED);
    }

    /**
     * @param user
     * @param homePageLink
     * @throws NotificationException
     */
    public void sendRegistrationCancelledMessage(User user, String homePageLink)
            throws NotificationException {
        sendRegistrationCompleteMessage(user, homePageLink, SUBJECT_CANCELLED,
                CANCELLED);
    }

    /**
     * @param user
     * @param homePageLink
     * @param subject
     * @param status
     * @throws NotificationException
     */
    private void sendRegistrationCompleteMessage(User user,
                                                 String homePageLink, String subject, String status)
            throws NotificationException {
        this.template = TEMPLATE_REGISTRATION_COMPLETE;
        this.subject = subject;
        this.from = this.subject;

        setValue(EMAIL, user.getEmail());
        setValue(FULLNAME, user.getNickName());
        setHomepageLink();
        setValue(STATUS, status);
        sendMessage(user.getEmail());
    }
}

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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.User;
import de.atb.socratic.service.user.UserService;

/**
 * @author ATB
 */
@ApplicationScoped
public class InvitationMailService extends MailNotificationService<Void> {

    private static final long serialVersionUID = 5396500281435453010L;

    @EJB
    UserService userService;

    private static final String TEMPLATE_PLATFORM_INVITATION = "platform_invitation_template.html";
    private static final String TEMPLATE_CHALLENGE_INVITATION = "challenge_invitation_template.html";
    private static final String TEMPLATE_IDEA_INVITATION = "idea_invitation_template.html";
    private static final String TEMPLATE_ACTION_INVITATION = "action_invitation_template.html";
    private static final String TEMPLATE_USER_DELETE = "user_deletion_template.html";
    private static final String TEMPLATE_ACTION_MATCH_SUGGESTION = "action_match_suggestion_template.html";


    private static final String REGISTRATION_LINK = "REGISTRATION_LINK";
    private static final String HOMEPAGE_LINK = "HOMEPAGE_LINK";
    private static final String CHALLENGE_LINK = "CHALLENGE_LINK";
    private static final String IDEA_LINK = "IDEA_LINK";
    private static final String ACTION_LINK = "ACTION_LINK";

    private static final String EMAIL = "EMAIL";
    private static final String USER = "USER";
    private static final String MESSAGE = "MESSAGE";
    private static final String CHALLENGE_NAME = "CHALLENGE_NAME";
    private static final String IDEA_NAME = "IDEA_NAME";
    private static final String ACTION_NAME = "ACTION_NAME";


    private static final String SUBJECT_PLATFORM_REGISTRATION = "Invitation to join the SOCRATIC Platform!";
    private static final String SUBJECT_CHALLENGE_INVITATION = "Invitation to take part in SOCRATIC Challenge!";
    private static final String SUBJECT_IDEA_INVITATION = "Invitation to take part in SOCRATIC Idea!";
    private static final String SUBJECT_ACTION_INVITATION = "Invitation to take part in SOCRATIC Action!";
    private static final String SUBJECT_USER_DELETE = "Sorry to see you go!";
    private static final String SUBJECT_ACTION_SUGGESTIONS = "Found new Actions to take part!";

    public InvitationMailService() {
        super(Void.class);
    }

    public void sendInvitationMessage(
            User user,
            String registrationLink,
            String homepageLink,
            String message,
            String... invitationEmails) throws NotificationException {
        // do not send invitations again to users who are already registered
        Set<String> notYetRegistered = filterOutAlreadyRegisteredEmails(new HashSet<>(Lists.newArrayList(invitationEmails)));

        this.template = TEMPLATE_PLATFORM_INVITATION;
        this.subject = SUBJECT_PLATFORM_REGISTRATION;
        this.from = this.subject;

        setValue(REGISTRATION_LINK, registrationLink);
        setValue(USER, user.getNickName());
        setValue(HOMEPAGE_LINK, homepageLink);
        setValue(MESSAGE, message);

        for (String email : notYetRegistered) {
            setValue(EMAIL, email);
            sendMessage(email);
        }
    }

    public void sendInvitationMessageFromChallenge(
            User user,
            String registrationLink,
            String challengeLink,
            String message,
            String challengeName,
            String... invitationEmails) throws NotificationException {
        // do not send invitations again to users who are already registered
        Set<String> notYetRegistered = filterOutAlreadyRegisteredEmails(new HashSet<>(Lists.newArrayList(invitationEmails)));

        this.template = TEMPLATE_CHALLENGE_INVITATION;
        this.subject = SUBJECT_CHALLENGE_INVITATION;
        this.from = this.subject;

        setValue(REGISTRATION_LINK, registrationLink);
        setValue(USER, user.getNickName());
        setValue(CHALLENGE_LINK, challengeLink);
        setValue(MESSAGE, message);
        setValue(CHALLENGE_NAME, challengeName);

        for (String email : notYetRegistered) {
            setValue(EMAIL, email);
            sendMessage(email);
        }
    }

    public void sendInvitationMessageFromIdea(
            User user,
            String registrationLink,
            String ideaLink,
            String message,
            String ideaName,
            String... invitationEmails) throws NotificationException {
        // do not send invitations again to users who are already registered
        Set<String> notYetRegistered = filterOutAlreadyRegisteredEmails(new HashSet<>(Lists.newArrayList(invitationEmails)));

        this.template = TEMPLATE_IDEA_INVITATION;
        this.subject = SUBJECT_IDEA_INVITATION;
        this.from = this.subject;

        setValue(REGISTRATION_LINK, registrationLink);
        setValue(USER, user.getNickName());
        setValue(IDEA_LINK, ideaLink);
        setValue(MESSAGE, message);
        setValue(IDEA_NAME, ideaName);

        for (String email : notYetRegistered) {
            setValue(EMAIL, email);
            sendMessage(email);
        }
    }

    public void sendInvitationMessageFromAction(
            User user,
            String registrationLink,
            String actionLink,
            String message,
            String actionName,
            String... invitationEmails) throws NotificationException {
        // do not send invitations again to users who are already registered
        Set<String> notYetRegistered = filterOutAlreadyRegisteredEmails(new HashSet<>(Lists.newArrayList(invitationEmails)));

        this.template = TEMPLATE_ACTION_INVITATION;
        this.subject = SUBJECT_ACTION_INVITATION;
        this.from = this.subject;

        setValue(REGISTRATION_LINK, registrationLink);
        setValue(USER, user.getNickName());
        setValue(ACTION_LINK, actionLink);
        setValue(MESSAGE, message);
        setValue(ACTION_NAME, actionName);

        for (String email : notYetRegistered) {
            setValue(EMAIL, email);
            sendMessage(email);
        }
    }

    public void sendGoodByeMessageToUser(String userNickName, String userEmail) throws NotificationException {

        this.template = TEMPLATE_USER_DELETE;
        this.subject = SUBJECT_USER_DELETE;
        this.from = this.subject;

        setValue(USER, userNickName);
        setValue(EMAIL, userEmail);
        sendMessage(userEmail);
    }

    /**
     * this method will send mail to user with email about actions matches his skills or interests...
     *
     * @param email
     * @param actionsMap, is list of map which will store mainly two types of entries Map.put("name", actionName) and
     *                    Map.put("url", actionUrl);
     * @throws NotificationException
     */
    public void sendActionMatchSuggestionMessage(String email, List<Map<String, String>> actionsMap) throws NotificationException {
        this.template = TEMPLATE_ACTION_MATCH_SUGGESTION;
        this.subject = SUBJECT_ACTION_SUGGESTIONS;
        this.from = this.subject;

        // map....
        setValue("actionsList", actionsMap);
        setValue(EMAIL, email);
        sendMessage(email);
    }

    private Set<String> filterOutAlreadyRegisteredEmails(final Set<String> emails) {
        Set<String> registeredEmails = new HashSet<>(userService.getEmailsOfRegisteredUserByEmails(emails));
        return Sets.difference(emails, registeredEmails);
    }

}

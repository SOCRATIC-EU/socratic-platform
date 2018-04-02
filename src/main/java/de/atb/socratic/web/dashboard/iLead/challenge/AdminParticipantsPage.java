/**
 *
 */
package de.atb.socratic.web.dashboard.iLead.challenge;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.validation.EmailMultiInputValidator;
import de.atb.socratic.service.notification.InvitationMailService;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.security.register.RegisterPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class AdminParticipantsPage extends AdminChallenge {
    private static final long serialVersionUID = -5559578453943490669L;

    private final AdminParticipantsListPanel adminParticipantsListPanel;

    private final TextField<String> emailTextField;
    private final TextArea<String> messageToInvitedContactTextArea;
    private String invitationEmails;

    @Inject
    InvitationMailService invitationMailService;

    @Inject
    UrlProvider urlProvider;

    private final Form<Void> form;

    public AdminParticipantsPage(PageParameters parameters) {
        super(parameters);

        // add form
        form = new Form<>("form");
        add(form);

        adminParticipantsListPanel = new AdminParticipantsListPanel("participantsList", Model.of(theChallenge), feedbackPanel) {

            private static final long serialVersionUID = 8087201252939474804L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        form.add(adminParticipantsListPanel.setOutputMarkupId(true));

        emailTextField = newEmailTextField("emailTextField", new PropertyModel<String>(this, "invitationEmails"));
        form.add(newEmailTextFieldValidationBorder("emailTextFieldValidationBorder", emailTextField));
        form.add(new EmailMultiInputValidator(emailTextField));
        emailTextField.setOutputMarkupId(true);

        // messageToInvitedContact
        messageToInvitedContactTextArea = new TextArea<>("messageToInvitedContact", new Model<String>(null));
        form.add(messageToInvitedContactTextArea);
        messageToInvitedContactTextArea.setOutputMarkupId(true);

        form.add(newSendInvitationsLink());
    }

    private AjaxSubmitLink newSendInvitationsLink() {
        return new AjaxSubmitLink("sendInvitations", form) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                try {
                    sendInvitations();
                } catch (NotificationException e) {
                    e.printStackTrace();
                }
                emailTextField.setModelObject("");
                messageToInvitedContactTextArea.setModelObject("");
                target.add(emailTextField);
                target.add(messageToInvitedContactTextArea);
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(emailTextField);
                target.add(messageToInvitedContactTextArea);
                target.add(form);
            }
        };
    }

    /**
     * @param email
     * @param model
     * @return
     */
    private TextField<String> newEmailTextField(String email, PropertyModel<String> model) {
        return new TextField<>(email, model);
    }

    private InputBorder<String> newEmailTextFieldValidationBorder(String id, final TextField<String> textField) {
        return new InputBorder<>(id, textField, new Model<String>());
    }

    private void sendInvitations() throws NotificationException {
        // Do not allow message to be sent if user is already registered on platform
        if (StringUtils.isNotBlank(invitationEmails)) {
            // avoid duplicate emails..
            Set<String> noRepeatedEmails = new HashSet<>(Arrays.asList(invitationEmails.split("\\s+")));
            String message = messageToInvitedContactTextArea.getModelObject();
            invitationMailService.sendInvitationMessageFromChallenge(loggedInUser,
                    urlProvider.urlFor(RegisterPage.class, new PageParameters()),
                    urlProvider.urlFor(ChallengeDefinitionPage.class, new PageParameters().set("id", theChallenge.getId())),
                    message, theChallenge.getName(), noRepeatedEmails.toArray(new String[noRepeatedEmails.size()]));
        }
    }

    /**
     * @return
     */
    private DropDownChoice<ParticipantsSortingCriteria> newSortingCriteriaDropDownChoice() {
        List<ParticipantsSortingCriteria> listOfSorrtingCriteria = Arrays.asList(ParticipantsSortingCriteria.values());
        final DropDownChoice<ParticipantsSortingCriteria> sortingCriteriaChoice = new DropDownChoice<ParticipantsSortingCriteria>(
                "sortingCriteriaChoice", new Model<ParticipantsSortingCriteria>(), listOfSorrtingCriteria);

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = -2730480336562437755L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                target.add(adminParticipantsListPanel.setSortingCriteria(sortingCriteriaChoice.getModelObject()));
            }
        });
        return sortingCriteriaChoice;
    }

    public enum ParticipantsSortingCriteria {
        OverallRating,
        Relevancy,
        Feasibility,
        Date,
        Comments,
        Likes;
    }
}

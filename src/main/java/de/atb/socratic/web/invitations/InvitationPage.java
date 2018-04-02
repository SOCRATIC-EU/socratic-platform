package de.atb.socratic.web.invitations;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.RegistrationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.model.validation.EmailInputValidator;
import de.atb.socratic.service.notification.InvitationMailService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.service.votes.ToursService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.ModalActionButton;
import de.atb.socratic.web.components.NotificationModal;
import de.atb.socratic.web.provider.UrlProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.security.register.RegisterPage;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class InvitationPage extends BasePage {

    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    UserService userService;

    @Inject
    InvitationMailService invitationMailService;

    @Inject
    UrlProvider urlProvider;

    @EJB
    ToursService toursService;

    @Inject
    Logger logger;

    private final Form<Void> form;
    private final TextField<String> emailTextField;
    private final TextArea<String> messageToInvitedContactTextArea;
    private final List<String> invitations = new ArrayList<>();
    private ListView<String> emailsListView;
    private DataView<String> invitedContactsView;
    private String invitationEmail;
    private String messageToInvitedContact;
    private final AjaxSubmitLink addInvitation;
    private final WebMarkupContainer emailContainer;
    private final AjaxLink submitInvitations, cancelInvitations;
    private NotificationModal sendEmailModal;
    private WebMarkupContainer invitedContactsContainer;
    private Map<String, String> invitationsMap = new HashMap<>();

    public InvitationPage(final PageParameters parameters) {
        super(parameters);

        // add form
        form = new Form<>("form");
        add(form);

        // add email field
        emailTextField = newEmailTextField("emailAddress", new PropertyModel<String>(this, "invitationEmail"));
        form.add(new InputBorder<>("emailValidationBorder", emailTextField));
        form.add(new EmailInputValidator(emailTextField));

        // messageToInvitedContact
        messageToInvitedContactTextArea = new TextArea<>(
                "messageToInvitedContact",
                new PropertyModel<String>(this, "messageToInvitedContact"));
        form.add(new InputBorder<>("messageValidationBorder", messageToInvitedContactTextArea.setRequired(true)));

        // add "add email" button
        addInvitation = newAjaxSubmitLinkToAddInvitation("addInvitation", form);
        form.add(addInvitation);

        emailContainer = new WebMarkupContainer("emailContainer");
        add(emailContainer.setOutputMarkupPlaceholderTag(true));
        // Add email list view
        emailsListView = newListViewForInvitations("allEmails", Model.ofList(invitations));
        emailContainer.add(emailsListView.setOutputMarkupId(true));

        //add Submit Button
        emailContainer.add(submitInvitations = newAjaxSubmitLinkToSubmitInvitations("submitInvitations"));

        //add Cancel Button
        emailContainer.add(cancelInvitations = newAjaxSubmitLinkToCancelInvitations("cancelInvitations"));

        // Add WebmarkupContainer
        invitedContactsContainer = newWMCForInvitedContacts("invitedContactsContainer");
        add(invitedContactsContainer);

        // Add invitedContactsView view
        invitedContactsView = newDataViewForInvitationsMade("invitedContacts", loggedInUser.getInvitedEmailContacts());
        invitedContactsView.setOutputMarkupId(true);
        invitedContactsContainer.add(invitedContactsView);

        // Add Paging to Dataview
        invitedContactsContainer.add(newPagingNavigator("paging", invitedContactsView));

        // add confirmation modal
        add(sendEmailModal = newSendEmailModal());
    }

    private TextField<String> newEmailTextField(String id, PropertyModel<String> model) {
        final TextField<String> textField = new TextField<>(id, model);
        textField.setRequired(true);
        return textField;
    }

    private AjaxSubmitLink newAjaxSubmitLinkToAddInvitation(String addInvitation, Form<Void> form) {
        return new AjaxSubmitLink(addInvitation, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                // has the email already been added to the list of invitations (view model) ||  been invited before (data model)?
                Boolean alreadyAdded = invitations.contains(invitationEmail)
                        || loggedInUser.getInvitedEmailContacts().contains(invitationEmail);
                Boolean alreadyAddedOwn =
                        StringUtils.isNotBlank(invitationEmail) && invitationEmail.equals(loggedInUser.getEmail());

                // add email to list of emails
                if (alreadyAdded) {
                    getPage().error(new StringResourceModel("email.input.validation.added", this, null).getString());
                    target.add(feedbackPanel);
                } else if (alreadyAddedOwn) {
                    getPage().error(new StringResourceModel("email.input.validation.own", this, null).getString());
                    target.add(feedbackPanel);
                } else {
                    invitations.add(invitationEmail);
                    invitationsMap.put(invitationEmail, messageToInvitedContact);
                    getPage().success(new StringResourceModel("email.input.validation.success", this, null).getString());
                    target.add(feedbackPanel);
                    emailTextField.setModelObject("");
                    messageToInvitedContactTextArea.setModelObject("");
                    form.clearInput();
                    target.add(emailContainer);
                }
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };
    }

    private ListView<String> newListViewForInvitations(String allEmails, IModel<List<? extends String>> listIModel) {
        return new ListView<String>(allEmails, listIModel) {
            @Override
            protected void populateItem(final ListItem<String> item) {

                final String key = item.getModelObject();
                final String value = invitationsMap.get(key);

                item.add(new Label("emailViewLabel", key));
                item.add(new Label("messageViewLabel", value));
                item.add(new AjaxLink("actionRemove") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        invitations.remove(key);
                        invitationsMap.remove(key);
                        target.add(emailContainer);
                    }
                });
            }
        };
    }

    private WebMarkupContainer newWMCForInvitedContacts(String markupId) {
        WebMarkupContainer wmc = new WebMarkupContainer(markupId) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(loggedInUser.getInvitedEmailContacts().size() > 0);
            }
        };
        wmc.setOutputMarkupPlaceholderTag(true);
        return wmc;
    }

    private DataView<String> newDataViewForInvitationsMade(String id, List<String> invitedEmailContacts) {
        return new DataView<String>(id, new ListDataProvider<>(invitedEmailContacts), 5) {
            @Override
            protected void populateItem(final Item<String> item) {
                item.add(new Label("invitedContactsLabel", item.getModelObject()));
                item.add(new Label("invitedContactsStatus", getStatus(item.getModelObject())));
            }
        };
    }

    private String getStatus(String invitedContact) {
        User user = userService.getByMailNullIfNotFound(invitedContact);
        if (user == null || user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            return new StringResourceModel("contacts.invited.status.pending", this, null).getString();
        } else if (user.getRegistrationStatus() == RegistrationStatus.CONFIRMED) {
            return new StringResourceModel("contacts.invited.status.registered", this, null).getString();
        } else if (user.getRegistrationStatus() == RegistrationStatus.CANCELLED) {   // it is when usr has deleted or unregistered itself
            return new StringResourceModel("contacts.invited.status.cancelled", this, null).getString();
        }
        return "";
    }

    private PagingNavigator newPagingNavigator(final String id, final DataView<String> invitedContactsView) {
        return new PagingNavigator(id, invitedContactsView);
    }

    private AjaxLink newAjaxSubmitLinkToSubmitInvitations(String submitInvitations) {
        return new AjaxLink(submitInvitations) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(!invitations.isEmpty());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                sendEmailModal.appendShowDialogJavaScript(target);
            }
        };
    }

    private AjaxLink newAjaxSubmitLinkToCancelInvitations(String cancelInvitations) {
        return new AjaxLink(cancelInvitations) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(!invitations.isEmpty());
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                clearInvitations();
                getPage().info(new StringResourceModel("cancel.invitations.feedback", this, null).getString());
                target.add(feedbackPanel);
                target.add(form);
            }
        };
    }

    private void submitInvitations(List<String> invitations, User loggedInUser) {
        for (String invite : invitations) {
            loggedInUser.addInvitedEmailContacts(invite);
        }
        userService.update(loggedInUser);
    }

    private NotificationModal newSendEmailModal() {
        final NotificationModal notificationModal = new NotificationModal(
                "sendEmailModal",
                new StringResourceModel("sendEmail.notification.modal.header", this, null),
                new StringResourceModel("sendEmail.notification.modal.message", this, null),
                false);
        notificationModal.addButton(new ModalActionButton(notificationModal,
                ButtonType.Info,
                new StringResourceModel("sendEmail.notification.modal.show.text", this, null),
                true) {
            private static final long serialVersionUID = 8931306355855637710L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                try {
                    submitInvitations(invitations, loggedInUser);
                    sendEmails();
                    getPage().success(new StringResourceModel("send.email.message", this, null).getString());
                } catch (NotificationException e) {
                    logger.error("Failed to send invitations", e);
                    getPage().error(new StringResourceModel("send.email.message.fail", this, null).getString());
                }
                // feedback
                target.add(invitedContactsContainer, feedbackPanel, emailContainer, form);
                closeNotificationModal(notificationModal, target);
                logger.info("The user " + loggedInUser.getNickName() + " invited some contacts to join the SOCRATIC platform via email.");
                //reload page
                setResponsePage(InvitationPage.class, getPageParameters());
            }
        });
        notificationModal.addButton(new ModalActionButton(
                notificationModal,
                ButtonType.Default,
                new StringResourceModel("sendEmail.notification.modal.ignore.text", this, null),
                true) {
            private static final long serialVersionUID = 6658207871656326413L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                // Cancel clicked --> do nothing but close modal
                target.add(invitedContactsContainer, feedbackPanel, emailContainer, form);
                closeNotificationModal(notificationModal, target);
            }
        });
        return notificationModal;
    }

    private void closeNotificationModal(final Modal modal, AjaxRequestTarget target) {
        modal.appendCloseDialogJavaScript(target);
    }

    private void clearInvitations() {
        form.clearInput();
        form.modelChanged();
        invitationEmail = "";
        invitations.clear();
        invitationsMap.clear();
    }

    private void sendEmails() throws NotificationException {
        if (invitationsMap.size() != 0) {
            for (Map.Entry<String, String> entry : invitationsMap.entrySet()) {
                invitationMailService.sendInvitationMessage(
                        loggedInUser,
                        urlProvider.urlFor(RegisterPage.class, new PageParameters()),
                        urlProvider.urlFor(getApplication().getHomePage()),
                        entry.getValue(),
                        entry.getKey());
            }
        }
        //clear
        clearInvitations();
    }

}

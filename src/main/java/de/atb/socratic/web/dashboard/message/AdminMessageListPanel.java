package de.atb.socratic.web.dashboard.message;

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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;

import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Message;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.MessageService;
import de.atb.socratic.web.components.ModalActionButton;
import de.atb.socratic.web.components.NotificationModal;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class AdminMessageListPanel extends GenericPanel<User> {

    private static final long serialVersionUID = 5545249725081327677L;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;
    // how many participants do we show initially
    private static final int itemsPerPage = 6;

    // container holding the list of participants
    private final WebMarkupContainer messagesContainer;

    private final CheckGroup<Message> messageGroup;

    private final AjaxSubmitLink deleteSelectedButton;

    private ModalWindow createMessageModalWindow;
    private ModalWindow displayMessageModalWindow;

    private CreateMessagePanel messageModelPanel;

    private DisplayMessagePanel displayMessageModelPanel;
    // Modal to inform when you are about to delete message
    private final Modal deleteConfirmationModal;

    // Repeating view showing the list of existing messages
    private final DataView<Message> messagesRepeater;

    private final EntityProvider<Message> messagesProvider;

    @EJB
    MessageService messageService;

    private MessageState messageState;

    private User loggedInUser;

    public AdminMessageListPanel(final String id, final IModel<User> model, final StyledFeedbackPanel feedbackPanel,
                                 MessageState messageState) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;
        this.messageState = messageState;
        this.loggedInUser = model.getObject();

        final Form<Message> deleteForm = new Form<Message>("deleteForm");
        add(deleteForm);

        // add message group and container with list of existing messages
        deleteForm.add(messageGroup = newMessageCheckGroup());
        messagesContainer = new WebMarkupContainer("messagesContainer");
        messageGroup.add(messagesContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("messageGroupSelector", messageGroup);
        messagesContainer.add(checkGroupSelector);

        deleteSelectedButton = newDeleteSelectedButton(messageGroup);
        messageGroup.add(deleteSelectedButton);

        deleteForm.add(createMessageButton());
        deleteForm.add(createMessageModelWindow());
        deleteForm.add(displayMessageModelWindow());

        // add confirmation modal for deleting projects
        add(deleteConfirmationModal = newDeleteConfirmationModal());

        // add messages table header: From and To label
        if (messageState.equals(MessageState.receving)) {
            messagesContainer.add(new Label("tableHeader", "From"));
        } else if (messageState.equals(MessageState.sending)) {
            messagesContainer.add(new Label("tableHeader", "To"));
        }

        // add repeating view with list of existing participants
        messagesProvider = new MessageProvider(getModelObject(), messageState);

        messagesRepeater = new DataView<Message>("messagesRepeater", messagesProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Message> item) {
                item.setOutputMarkupId(true);
                AdminMessageListPanel.this.populateItem(item, item.getModelObject());
            }
        };
        messagesContainer.add(messagesRepeater.setOutputMarkupId(true));

        add(new BootstrapAjaxPagingNavigator("pagination", messagesRepeater) {
            private static final long serialVersionUID = 981342675527042691L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(messagesRepeater.getPageCount() > 1);
            }
        });
    }

    protected void populateItem(final WebMarkupContainer item, final Message message) {
        item.setOutputMarkupId(true);
        item.add(newDeleteCheck(message));
        if (messageState.equals(MessageState.receving)) {
            AjaxLink<Void> link = teamMemberImageLink(message.getSender());
            link.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.THUMBNAIL,
                    message.getSender())));
            item.add(link);
            item.add(new Label("nickName", new PropertyModel<String>(message.getSender(), "nickName")));
        } else if (messageState.equals(MessageState.sending)) {

            // if there are multiple recipients, show some common group name
            if (message.getReceivers().size() > 1) {
                AjaxLink<Void> link = teamMemberImageLink(null);
                link.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.THUMBNAIL, null)));
                item.add(link);
                // item.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.PROFILE, null)));
                String nickNameOfFirstReceiver = message.getReceivers().get(0).getNickName() + " and others..";
                item.add(new Label("nickName", nickNameOfFirstReceiver));
            } else {
                AjaxLink<Void> link = teamMemberImageLink(message.getReceivers().get(0));
                link.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.PROFILE, message
                        .getReceivers().get(0))));
                item.add(link);
                item.add(new Label("nickName", new PropertyModel<String>(message.getReceivers().get(0), "nickName")));
            }
        }

        item.add(new Label("date", new PropertyModel<String>(message, "postedAt")));

        // in order to change text color when user clicks on message subject and reads it
        Label subjectTextLabel = new Label("subject", new PropertyModel<String>(message, "subject"));
        subjectTextLabel.setOutputMarkupId(true);
        // change color of row if message is not read
        if (message.getIsRedByUser().contains(loggedInUser)) {
            subjectTextLabel.add(new AttributeModifier("class", "text-primary"));
        } else {
            subjectTextLabel.add(new AttributeModifier("class", "text-success"));
        }
        AjaxLink<Void> subjectLink = messageSubjectLink(message, subjectTextLabel);
        subjectLink.add(subjectTextLabel);
        item.add(subjectLink);
    }

    /**
     * @param message
     * @param subjectTextLabel
     * @return
     */
    private AjaxLink<Void> messageSubjectLink(final Message message, final Label subjectTextLabel) {
        AjaxLink<Void> link = new AjaxLink<Void>("subjectLink") {
            private static final long serialVersionUID = -6633994061963892062L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                message.getIsRedByUser().remove(loggedInUser);
                messageService.update(message);
                if (message.getIsRedByUser().contains(loggedInUser)) {
                    subjectTextLabel.add(new AttributeModifier("class", "text-primary"));
                } else {
                    subjectTextLabel.add(new AttributeModifier("class", "text-success"));
                }

                target.add(subjectTextLabel);

                // update unread messages counter
                onUnreadMessagesNoChanged(target);

                // on click display message
                displayMessageModelPanel = new DisplayMessagePanel(displayMessageModalWindow.getContentId(), displayMessageModalWindow,
                        message) {
                    private static final long serialVersionUID = 1L;
                };

                displayMessageModalWindow.setContent(displayMessageModelPanel.setOutputMarkupId(true));
                displayMessageModalWindow.show(target);
            }
        };

        link.add(AttributeModifier.append("title", "click to read message"));
        link.setOutputMarkupId(true);
        return link;
    }

    /**
     * @param user
     * @return
     */
    private AjaxLink<Void> teamMemberImageLink(final User user) {
        AjaxLink<Void> link = new AjaxLink<Void>("link") {
            private static final long serialVersionUID = -6633994061963892062L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (user != null) {
                    setResponsePage(UserProfileDetailsPage.class, new PageParameters().set("id", user.getId()));
                }
            }
        };

        if (user != null) {
            link.add(AttributeModifier.append("title", user.getNickName()));
        }
        return link;
    }

    /**
     * @param message
     * @return
     */
    private Check<Message> newDeleteCheck(final Message message) {
        Check<Message> check = new Check<Message>("messageCheck", new Model<>(message), messageGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
    }

    /**
     * @return
     */
    private NotificationModal newDeleteConfirmationModal() {
        final NotificationModal notificationModal = new NotificationModal("deleteConfirmationModal", new StringResourceModel(
                "delete.confirmation.modal.header", this, null), new StringResourceModel("delete.confirmation.modal.message", this,
                null), false);
        notificationModal.addButton(new ModalActionButton(notificationModal, ButtonType.Primary, new StringResourceModel(
                "delete.confirmation.modal.submit.text", this, null), true) {
            private static final long serialVersionUID = -8579196626175159237L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                // confirmed --> delete
                deleteSelectedMessages(messageGroup.getModelObject(), target);

                // update unread messages counter
                onUnreadMessagesNoChanged(target);

                // close modal
                closeDeleteConfirmationModal(notificationModal, target);
            }
        });
        notificationModal.addButton(new ModalActionButton(notificationModal, ButtonType.Default, new StringResourceModel(
                "delete.confirmation.modal.cancel.text", this, null), true) {
            private static final long serialVersionUID = 8931306355855637710L;

            @Override
            protected void onAfterClick(AjaxRequestTarget target) {
                // Cancel clicked --> do nothing, close modal
                closeDeleteConfirmationModal(notificationModal, target);
            }
        });
        return notificationModal;
    }

    /**
     * @param modal
     * @param target
     */
    private void closeDeleteConfirmationModal(final Modal modal, AjaxRequestTarget target) {
        // close
        modal.appendCloseDialogJavaScript(target);
    }

    /**
     * @param messages
     * @param target
     */
    private void deleteSelectedMessages(Collection<Message> messages, AjaxRequestTarget target) {
        String sucessStr = new StringResourceModel("message.selected.deleted", this, null).getString();
        for (Message msg : messages) {
            if (messageState.equals(MessageState.receving)) {
                messageService.deleteMessageForReceiver(msg, loggedInUser);
            } else if (messageState.equals(MessageState.sending)) {
                messageService.deleteMessageForSender(msg, loggedInUser);
            }

        }
        getPage().success(sucessStr);
        updateMessageList(target);
        // update the delete button
        messageGroup.updateModel();
        updateDeleteSelectedButton(target);
    }

    /**
     * @param target
     */
    private void updateMessageList(AjaxRequestTarget target) {
        target.add(messagesContainer);
    }

    /**
     * @return
     */
    private CheckGroup<Message> newMessageCheckGroup() {
        CheckGroup<Message> checkGroup = new CheckGroup<Message>("messageGroup", new ArrayList<Message>());
        checkGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = -8193184672687169923L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateDeleteSelectedButton(target);
            }
        });

        return checkGroup;
    }

    /**
     * @param target
     */
    private void updateDeleteSelectedButton(AjaxRequestTarget target) {
        target.add(deleteSelectedButton);
    }

    /**
     * @param
     * @return
     */
    private AjaxSubmitLink createMessageButton() {
        AjaxSubmitLink ajaxSubmitLink = new AjaxSubmitLink("createMessage") {

            private static final long serialVersionUID = 7675565566981782898L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(createMessageModalWindow);
                List<User> recipients = new LinkedList<>();

                if (!messageGroup.getModelObject().isEmpty()) {
                    if (messageState.equals(MessageState.receving)) {
                        for (Message msg : messageGroup.getModelObject()) {
                            recipients.add(msg.getSender());
                        }
                    } else if (messageState.equals(MessageState.sending)) {
                        for (Message msg : messageGroup.getModelObject()) {
                            for (User user : msg.getReceivers()) {
                                recipients.add(user);
                            }
                        }
                    }
                }

                // pass non repeated users to be displayed in recipients field.
                Set<User> noRepeatedUsres = new HashSet<>(recipients);
                messageModelPanel = new CreateMessagePanel(createMessageModalWindow.getContentId(), createMessageModalWindow,
                        noRepeatedUsres) {
                    private static final long serialVersionUID = 1L;
                };

                createMessageModalWindow.setContent(messageModelPanel.setOutputMarkupId(true));
                createMessageModalWindow.show(target);

            }
        };
        return ajaxSubmitLink;
    }

    /**
     * @param
     * @return
     */
    private ModalWindow createMessageModelWindow() {
        createMessageModalWindow = new ModalWindow("createMessageModalWindow") {

            private static final long serialVersionUID = -6118683848343086655L;

            @Override
            public void show(AjaxRequestTarget target) {
                super.show(target);
                target.appendJavaScript(""//
                        + "var thisWindow = Wicket.Window.get();\n"
                        + "if (thisWindow) {\n"
                        + "thisWindow.window.style.width = \"1500px\";\n"
                        + "thisWindow.content.style.height = \"1000px\";\n"
                        + "thisWindow.center();\n" + "}");
                setOutputMarkupId(true);
            }
        };

        // createMessageModalWindow.setTitle("Compose Message");
        createMessageModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {

            private static final long serialVersionUID = -9143847141081283640L;

            @Override
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return true;
            }
        }).setOutputMarkupId(true);

        createMessageModalWindow.setResizable(true);
        createMessageModalWindow.setAutoSize(true);
        createMessageModalWindow.setInitialWidth(1400);
        createMessageModalWindow.setInitialHeight(1400);

        return createMessageModalWindow;
    }

    /**
     * @param
     * @return
     */
    private ModalWindow displayMessageModelWindow() {
        displayMessageModalWindow = new ModalWindow("displayMessageModalWindow") {

            private static final long serialVersionUID = -6118683848343086655L;

            @Override
            public void show(AjaxRequestTarget target) {
                super.show(target);
                target.appendJavaScript(""//
                        + "var thisWindow = Wicket.Window.get();\n"
                        + "if (thisWindow) {\n"
                        + "thisWindow.window.style.width = \"800px\";\n"
                        + "thisWindow.content.style.height = \"800px\";\n"
                        + "thisWindow.center();\n" + "}");
                setOutputMarkupId(true);
            }
        };
        displayMessageModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {

            private static final long serialVersionUID = -9143847141081283640L;

            @Override
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return true;
            }
        }).setOutputMarkupId(true);

        displayMessageModalWindow.setResizable(true);
        displayMessageModalWindow.setAutoSize(true);
        displayMessageModalWindow.setInitialWidth(800);
        displayMessageModalWindow.setInitialHeight(800);

        return displayMessageModalWindow;
    }

    /**
     * @param messageGroup
     * @return
     */
    private AjaxSubmitLink newDeleteSelectedButton(final CheckGroup<Message> messageGroup) {
        AjaxSubmitLink ajaxSubmitLink = new AjaxSubmitLink("deleteSelected") {
            private static final long serialVersionUID = 1162060284069587067L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                // only enabled if at least one project is selected
                if (messageGroup.getModelObject().isEmpty()) {
                    add(new CssClassNameAppender(Model.of("disabled")) {
                        private static final long serialVersionUID = 5588027455196328830L;

                        // remove css class when component is rendered again
                        @Override
                        public boolean isTemporary(Component component) {
                            return true;
                        }
                    });
                    setEnabled(false);
                } else {
                    setEnabled(true);
                }
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                deleteConfirmationModal.appendShowDialogJavaScript(target);
            }
        };
        return ajaxSubmitLink;
    }

    /**
     * @author ATB
     */
    private final class MessageProvider extends EntityProvider<Message> {

        /**
         *
         */
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;

        private MessageState messageState;

        public MessageProvider(User user, MessageState messageState) {
            super();
            this.user = user;
            this.messageState = messageState;
        }

        @Override
        public Iterator<? extends Message> iterator(long first, long count) {
            List<Message> messages = null;

            if (messageState.equals(MessageState.receving)) {
                messages = messageService.getAllReceivedMessages(user, Long.valueOf(first).intValue(), Long.valueOf(count)
                        .intValue());
            } else if (messageState.equals(MessageState.sending)) {
                messages = messageService.getAllSentMessages(user, Long.valueOf(first).intValue(), Long.valueOf(count)
                        .intValue());
            }
            return messages.iterator();
        }

        @Override
        public long size() {
            if (messageState.equals(MessageState.receving)) {
                return messageService.countAllReceivedMessages(user);
            } else if (messageState.equals(MessageState.sending)) {
                return messageService.countAllSentMessages(user);
            }
            return 0;
        }

    }

    protected abstract void onUnreadMessagesNoChanged(AjaxRequestTarget target);

    public enum MessageState {
        sending,
        receving
    }
}

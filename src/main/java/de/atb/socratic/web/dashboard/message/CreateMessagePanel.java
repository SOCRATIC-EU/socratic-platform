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

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.atb.socratic.model.Message;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.MessageService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.HtmlEvent;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.components.OnEventInputBeanValidationBorder;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class CreateMessagePanel extends GenericPanel<Message> {

    private static final long serialVersionUID = 3761581601061855778L;

    @Inject
    MessageService messageService;

    @Inject
    UserService userService;

    @Inject
    @LoggedInUser
    User loggedInUser;

    @Inject
    Logger logger;

    private Message theMessage;
    private final Form<Message> messageForm;
    protected final InputBorder<String> subjectValidationBorder;
    protected final InputBorder<String> messageTextValidationBorder;

    protected final TextArea<String> subjectTextArea;
    protected final TextArea<String> messageTextArea;
    private final TextField<String> nickNameTextField;

    public static final String MESSAGE_PARAM = "message";

    public static final String LEVEL_PARAM = "level";

    private String recepientsNickNames = "";
    private String nickName = "";

    public CreateMessagePanel(String id, final ModalWindow window, Set<User> noRepeatedUsres) {
        super(id);

        theMessage = new Message();

        // load recipients list text field if receivers are provided.
        if (noRepeatedUsres.size() != 0) {
            for (User user : noRepeatedUsres) {
                recepientsNickNames += user.getNickName() + " ";
            }
        }

        // add form to create new Message
        add(messageForm = newMessageForm());

        // add NickName field
        nickNameTextField = newNickNameTextField("nickName", new PropertyModel<String>(this, "recepientsNickNames"));
        nickNameTextField.setOutputMarkupId(true);
        messageForm.add(newNickNameTextFieldValidationBorder("nickNameValidationBorder", nickNameTextField));

        messageForm.add(subjectValidationBorder = newTextField("subjectValidationBorder",
                subjectTextArea = newTextArea("subject")));

        messageForm.add(messageTextValidationBorder = newTextField("messageTextValidationBorder",
                messageTextArea = newTextArea("messageText")));

        messageForm.add(newSendButton(window));
    }

    private InputBorder<String> newNickNameTextFieldValidationBorder(String id, final TextField<String> textField) {
        return new InputBorder<>(id, textField, new Model<String>());
    }

    /**
     * @return
     */
    private Form<Message> newMessageForm() {
        Form<Message> form = new InputValidationForm<>("form");
        form.setOutputMarkupId(true);
        return form;
    }

    private AjaxSubmitLink newSendButton(final ModalWindow window) {
        return new AjaxSubmitLink("sendButton") {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (save(target)) {
                    window.close(target);
                } else {
                    showErrors(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                showErrors(target);
            }
        };
    }

    /**
     * @param target
     */
    protected void showErrors(AjaxRequestTarget target) {
        // in case of errors (e.g. validation errors) show error
        // messages in form
        target.add(messageForm);
    }

    protected boolean save(AjaxRequestTarget target) {
        if (!createMessage(target)) {
            showErrors(target);
            return false;
        }
        return true;
    }

    private boolean createMessage(AjaxRequestTarget target) {

        theMessage.setPostedAt(new Date());
        theMessage.setSender(loggedInUser);

        // find receiver based on recepientsNickName.
        List<User> receiversWithNickName = new LinkedList<>();

        if (StringUtils.isNotBlank(recepientsNickNames)) {
            // avoid duplicate nickNames..
            Set<String> noRepeatedNickNames = new HashSet<>(Arrays.asList(recepientsNickNames.split("\\s+")));
            for (String nickName : noRepeatedNickNames) {
                User receiver = userService.getByNickNameNullIfNotFound(nickName);
                if (receiver == null || receiver.getId() == null) {
                    this.nickName = nickName;
                    nickNameTextField.error(String.format(getString("nickName.input.validation.userNotFoundException"), nickName));
                    target.add(nickNameTextField);
                    target.add(messageForm);
                    return false;
                } else if (receiver.getDeleted()) {
                    this.nickName = nickName;
                    nickNameTextField.error(String.format(getString("nickName.input.validation.userDeletedException"), nickName));
                    target.add(nickNameTextField);
                    target.add(messageForm);
                    return false;
                }

                // if everything goes well, then add it to list
                receiversWithNickName.add(receiver);

            }
        }

        theMessage.setReceivers(receiversWithNickName);

        // add all receivers to this list in order to make sure they did not read message
        theMessage.setIsRedByUser(receiversWithNickName);

        // add all receivers to this list in order to make sure they did not delete message
        theMessage.setIsDeletedByReceiver(receiversWithNickName);

        messageService.create(theMessage);
        logger.info("The message " + theMessage.getSubject() + " is created");
        return true;
    }

    private TextField<String> newNickNameTextField(String id, PropertyModel<String> model) {
        final TextField<String> textField = new TextField<>(id, model);
        textField.setRequired(true);
        return textField;
    }

    /**
     * @return
     */
    private InputBorder<String> newTextField(String id, TextArea<String> textArea) {
        return new OnEventInputBeanValidationBorder<String>(id, textArea, new Model<String>(), HtmlEvent.ONCHANGE);
    }

    private TextArea<String> newTextArea(String id) {
        TextArea<String> textArea = new TextArea<String>(id, new PropertyModel<String>(theMessage, id));
        textArea.setRequired(true);
        return textArea;
    }

}

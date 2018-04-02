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

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import de.atb.socratic.model.Message;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.MessageService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.PropertyModel;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class DisplayMessagePanel extends GenericPanel<Message> {

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

    private CreateMessagePanel messageModelPanel;
    private ModalWindow createMessageModalWindow;

    public DisplayMessagePanel(String id, final ModalWindow window, Message message) {
        super(id);

        this.theMessage = message;

        add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.THUMBNAIL, message.getSender())));
        add(new Label("sender.nickName", new PropertyModel<String>(message, "sender.nickName")));

        // date
        add(new Label("date", new PropertyModel<String>(theMessage, "postedAt")));
        // subject
        add(new Label("subject", new PropertyModel<String>(theMessage, "subject")));

        // messageText
        add(new Label("messageText", new PropertyModel<String>(theMessage, "messageText")));

        // send message button
        add(sendReplyButton(theMessage.getSender(), window));
        add(createMessageModelWindow());
    }

    /**
     * @param
     * @return
     */
    private AjaxLink<Void> sendReplyButton(final User user, final ModalWindow window) {
        AjaxLink<Void> ajaxSubmitLink = new AjaxLink<Void>("createMessage") {

            private static final long serialVersionUID = 7675565566981782898L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                // Don't show reply button if message sender is the same as loggedIn user
                setVisible(loggedInUser.equals(user) ? false : true);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                // show new create message window
                target.add(createMessageModalWindow);
                Set<User> recipients = new HashSet<>();

                recipients.add(user);
                messageModelPanel = new CreateMessagePanel(createMessageModalWindow.getContentId(), createMessageModalWindow,
                        recipients) {
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

}

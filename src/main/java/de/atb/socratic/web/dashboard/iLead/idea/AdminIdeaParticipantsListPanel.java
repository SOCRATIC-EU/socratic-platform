package de.atb.socratic.web.dashboard.iLead.idea;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.MessageParticipantsSortingCriteria;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class AdminIdeaParticipantsListPanel extends GenericPanel<Idea> {
    private static final long serialVersionUID = -257930933985282429L;
    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private final DropDownChoice<MessageParticipantsSortingCriteria> messageParticipantsSortingCriteria;

    private ModalWindow createMessageModalWindow;
    private CreateMessagePanel messageModelPanel;
    private final CheckGroup<User> userGroup;

    // how many participants do we show initially
    private static final int itemsPerPage = 6;

    // container holding the list of participants
    private final WebMarkupContainer participantsContainer;

    // Repeating view showing the list of existing participants
    private final DataView<User> participantsRepeater;

    private final EntityProvider<User> participantProvider;

    @EJB
    IdeaService ideaService;

    @EJB
    UserService userService;

    @EJB
    ActivityService activityService;

    @Inject
    ParticipateNotificationService participateNotifier;

    final Idea idea;

    public AdminIdeaParticipantsListPanel(final String id, final IModel<Idea> model, final StyledFeedbackPanel feedbackPanel) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        // get the ideas participants
        idea = getModelObject();

        final Form<User> form = new Form<User>("form");
        add(form);
        form.add(userGroup = newUserCheckGroup());

        // add container with list of existing participants
        participantsContainer = new WebMarkupContainer("participantsContainer");
        userGroup.add(participantsContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("userGroupSelector", userGroup);
        participantsContainer.add(checkGroupSelector);

        form.add(createMessageButton());
        form.add(createMessageModelWindow());
        form.add(messageParticipantsSortingCriteria = newSortingCriteriaDropDownChoice());

        // add repeating view with list of existing participants
        participantProvider = new UserProvider(idea);
        participantsRepeater = new DataView<User>("participantsRepeater", participantProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<User> item) {
                item.setOutputMarkupId(true);
                AdminIdeaParticipantsListPanel.this.populateItem(item, item.getModelObject());
            }
        };
        participantsContainer.add(participantsRepeater);

        userGroup.add(new BootstrapAjaxPagingNavigator("pagination", participantsRepeater) {

            private static final long serialVersionUID = -7891882039916463527L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(participantsRepeater.getPageCount() > 1);
            }
        });
    }

    protected void populateItem(final WebMarkupContainer item, final User user) {
        item.setOutputMarkupId(true);
        item.add(newSelectionCheck(user));
        AjaxLink<Void> link = teamMemberImageLink(user);
        link.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.THUMBNAIL, user)));
        item.add(link);
        item.add(new Label("nickName", new PropertyModel<String>(user, "nickName")));
        item.add(new Label("noOfCommentsToIdea", ideaService.countCommentsForIdeaByUser(idea, user)));
    }

    /**
     * @return
     */
    private DropDownChoice<MessageParticipantsSortingCriteria> newSortingCriteriaDropDownChoice() {
        List<MessageParticipantsSortingCriteria> listOfSorrtingCriteria = Arrays.asList(MessageParticipantsSortingCriteria
                .values());
        final DropDownChoice<MessageParticipantsSortingCriteria> sortingCriteriaChoice = new DropDownChoice<MessageParticipantsSortingCriteria>(
                "messageParicipantsSortingChoice", new Model<MessageParticipantsSortingCriteria>(), listOfSorrtingCriteria, new IChoiceRenderer<MessageParticipantsSortingCriteria>() {
            private static final long serialVersionUID = -4910707158069588234L;

            @Override
            public Object getDisplayValue(MessageParticipantsSortingCriteria object) {
                return new StringResourceModel(object.getNameKey(), AdminIdeaParticipantsListPanel.this, null).getString();
            }

            @Override
            public String getIdValue(MessageParticipantsSortingCriteria object, int index) {
                return String.valueOf(index);
            }
        });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(MessageParticipantsSortingCriteria.Selected_Users);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = -2840672822107134374L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                target.add(messageParticipantsSortingCriteria);
            }
        });
        return sortingCriteriaChoice;
    }

    /**
     * @return
     */
    private CheckGroup<User> newUserCheckGroup() {
        CheckGroup<User> checkGroup = new CheckGroup<User>("userGroup", new ArrayList<User>());
        checkGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = -8193184672687169923L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // updateDeleteSelectedButton(target);
            }
        });

        return checkGroup;
    }

    /**
     * @param user
     * @return
     */
    private Check<User> newSelectionCheck(final User user) {
        Check<User> check = new Check<User>("userCheck", new Model<>(user), userGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
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
     * @param
     * @return
     */
    private AjaxSubmitLink createMessageButton() {
        AjaxSubmitLink ajaxSubmitLink = new AjaxSubmitLink("createMessage") {

            private static final long serialVersionUID = 7675565566981782898L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(createMessageModalWindow);
                Collection<User> recipients = new LinkedList<>();

                if (messageParticipantsSortingCriteria.getModelObject().equals(MessageParticipantsSortingCriteria.All_Users)) {
                    recipients = activityService.getAllIdeaActivityUsersByAscNickNameAndByIdea(idea);
                } else if (!userGroup.getModelObject().isEmpty()) {
                    recipients = userGroup.getModelObject();
                }

                // pass non repeated users to be displayed in recipients field.
                Set<User> noRepeatedUsers = new HashSet<>(recipients);
                messageModelPanel = new CreateMessagePanel(createMessageModalWindow.getContentId(), createMessageModalWindow,
                        noRepeatedUsers) {
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
     * @author ATB
     */
    private final class UserProvider extends EntityProvider<User> {

        /**
         *
         */
        private static final long serialVersionUID = -1727094205049792307L;

        private final Idea idea;

        public UserProvider(Idea idea) {
            super();
            this.idea = idea;
        }

        @Override
        public Iterator<? extends User> iterator(long first, long count) {
            List<User> users = null;
            users = activityService.getAllIdeaActivityUsersByAscNickNameAndByIdea(idea, Long.valueOf(first).intValue(),
                    Long.valueOf(count).intValue());
            return users.iterator();
        }

        @Override
        public long size() {
            return activityService.countAllIdeaActivityUsersByIdea(idea);
        }

    }
}

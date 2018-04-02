package de.atb.socratic.web.profile;

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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CommentService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.Dashboard.StateForDashboard;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import de.atb.socratic.web.dashboard.panels.ActionListPanel;
import de.atb.socratic.web.dashboard.panels.ChallengeListPanel;
import de.atb.socratic.web.dashboard.panels.IdeasListPanel;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class UserProfileDetailsPage extends BasePage {

    private static final long serialVersionUID = 2839424925522841147L;

    @Inject
    @LoggedInUser
    private User loggedInUser;

    @EJB
    private UserService userService;

    @EJB
    private IdeaService ideaService;

    @EJB
    private CommentService commentService;

    private User user;
    private Long userId;

    // how many challenges do we show initially
    private static final int challengesPerPage = 1;

    // how many ideas do we show initially
    private static final int ideasPerPage = 1;

    // how many actions do we show initially
    private static final int actionsPerPage = 1;

    private final ChallengeListPanel userLeadChallengesListPanel;
    private final IdeasListPanel userLeadIdeasListPanel;
    private final ActionListPanel userLeadActionsListPanel;

    private final ChallengeListPanel userParticipateChallengesListPanel;
    private final IdeasListPanel userParticipateIdeasListPanel;
    private final ActionListPanel userParticipateActionsListPanel;
    private CreateMessagePanel messageModelPanel;
    private ModalWindow createMessageModalWindow;

    public UserProfileDetailsPage(PageParameters parameters) {
        super(parameters);

        // load user
        loadUser(parameters);

        add(new Label("nickName", new PropertyModel<>(user, "nickName")));
        add(new Label("city", new PropertyModel<>(user, "city")));
        add(new Label("country", new PropertyModel<>(user, "country")));

        add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.PROFILE, user)));

        // send message button
        add(createMessageButton(user));
        add(createMessageModelWindow());

        // User leads, ideas, contribution etc panel
        int totalNoOfLeadsByUser = user.getNoOfCampaignsLeads() + user.getNoOfIdeasLeads() + user.getNoOfActionsLeads();
        add(new Label("leadLabel", totalNoOfLeadsByUser));
        add(new Label("ideasLabel", user.getNoOfIdeasLeads()));
        add(new Label("contributionsLabel", user.getNoOfCommentsPosts()));
        add(new Label("likesLabel", user.getNoOfLikesReceived()));

        Date birthDate = user.getBirthDate();
        String birthDateStr = "";
        if (birthDate != null) {
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
            birthDateStr = df.format(birthDate);
        }
        add(new Label("birthDate", birthDateStr));

        // add website to User class
        add(new ExternalLink("website", user.getWebsite(), user.getWebsite()));

        // Social networks
        add(new ExternalLink("facebookUrl", user.getFacebookUrl()));
        add(new ExternalLink("linkedInUrl", user.getLinkedInUrl()));
        add(new ExternalLink("twitterUrl", user.getTwitterUrl()));

        // Interests
        add(newUserInterestPanel(user));
        // skills
        add(newUserSkillsPanel(user));

        // add panel with list of existing challenges
        userLeadChallengesListPanel = new ChallengeListPanel("userLeadChallengesListPanel", Model.of(user), feedbackPanel,
                StateForDashboard.Lead, challengesPerPage) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(userLeadChallengesListPanel.setOutputMarkupId(true));

        // add panel with list of existing ideas
        userLeadIdeasListPanel = new IdeasListPanel("userLeadIdeasListPanel", Model.of(user), feedbackPanel,
                StateForDashboard.Lead, ideasPerPage) {
            private static final long serialVersionUID = -4602672523322280660L;
        };
        add(userLeadIdeasListPanel.setOutputMarkupId(true));

        // add panel with list of existing action
        userLeadActionsListPanel = new ActionListPanel("userLeadActionsListPanel", Model.of(user), feedbackPanel,
                StateForDashboard.Lead, actionsPerPage) {
            private static final long serialVersionUID = 6393614856591095877L;
        };
        add(userLeadActionsListPanel.setOutputMarkupId(true));

        // add panel with list of existing challenges
        userParticipateChallengesListPanel = new ChallengeListPanel("userParticipateChallengesListPanel", Model.of(user),
                feedbackPanel, StateForDashboard.TakePart, challengesPerPage) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(userParticipateChallengesListPanel.setOutputMarkupId(true));

        // add panel with list of existing ideas
        userParticipateIdeasListPanel = new IdeasListPanel("userParticipateIdeasListPanel", Model.of(user), feedbackPanel,
                StateForDashboard.TakePart, ideasPerPage) {
            private static final long serialVersionUID = -4602672523322280660L;
        };
        add(userParticipateIdeasListPanel.setOutputMarkupId(true));

        // add panel with list of existing action
        userParticipateActionsListPanel = new ActionListPanel("userParticipateActionsListPanel", Model.of(user), feedbackPanel,
                StateForDashboard.TakePart, actionsPerPage) {
            private static final long serialVersionUID = 6393614856591095877L;
        };
        add(userParticipateActionsListPanel.setOutputMarkupId(true));
    }

    private void loadUser(PageParameters parameters) {
        userId = parameters.get("id").toOptionalLong();
        if (userId != null) {
            user = userService.getById(userId);
            // if user is deleted then redirect page to UserDoesNotExists Page
            if (user.getDeleted()) {
                setResponsePage(UserDeletedProfilePage.class);
            }
        } else {
            error("Could not find user!");
            // show proper error page
        }
    }

    private WebMarkupContainer newUserInterestPanel(final User user) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("interestsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!user.getInterests().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("interests.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("interests", new ListModel<>(user.getInterests())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("interest", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
    }

    private WebMarkupContainer newUserSkillsPanel(final User user) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("skillsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!user.getSkills().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("skills.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("skills", new ListModel<>(user.getSkills())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("skill", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
    }

    /**
     * @param
     * @return
     */
    private AjaxLink<Void> createMessageButton(final User user) {
        AjaxLink<Void> ajaxSubmitLink = new AjaxLink<Void>("createMessage") {

            private static final long serialVersionUID = 7675565566981782898L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(createMessageModalWindow);
                Set<User> recipients = new HashSet<>();

                recipients.add(user);
                messageModelPanel = new CreateMessagePanel(createMessageModalWindow.getContentId(), createMessageModalWindow, recipients) {
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

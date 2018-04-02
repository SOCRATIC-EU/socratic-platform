package de.atb.socratic.web.action.detail;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionTeamTool;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.facebook.share.FacebookSharePanel;
import de.atb.socratic.web.components.linkedin.share.LinkedInSharePanel;
import de.atb.socratic.web.components.resource.ActionPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.iLead.action.AdminActionBusinessModelAddEditPage;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @param <T>
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class CommonActionResourceHeaderPanel<T> extends GenericPanel<T> {

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // inject the EJB for managing ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @EJB
    IdeaService ideaService;

    @EJB
    UserService userService;

    private Action action;
    private NonCachingImage actionProfilePicture;

    private ListView<User> teamMembersView;

    private CreateMessagePanel messageModelPanel;
    private ModalWindow createMessageModalWindow;
    protected final AjaxLink<Void> addFollowersLink;
    protected final AjaxLink<Void> addManageLink;
    private int pageNumber;
    protected final StyledFeedbackPanel feedbackPanel;

    public CommonActionResourceHeaderPanel(final String id, final IModel<T> model, final StyledFeedbackPanel feedbackPanel) {
        super(id, model);
        this.setOutputMarkupId(true);
        this.feedbackPanel = feedbackPanel;

        this.action = (Action) model.getObject();

        // challenge picture
        add(newChallengeProfilePicturePreview(action));

        // action title
        add(new Label("shortText", new PropertyModel<String>(action, "shortText")));

        // action elevatorPitch
        add(new Label("elevatorPitch", new PropertyModel<String>(action, "elevatorPitch")));

        // action owner
        AjaxLink<Void> link = userImageLink("actionLeaderlink", action.getPostedBy());
        link.add(new NonCachingImage("userPicture", ProfilePictureResource.get(PictureType.THUMBNAIL, action.getPostedBy())));
        add(link);
        add(new Label("nickName", new PropertyModel<String>(action, "postedBy.nickName")));

        // followers button
        addFollowersLink = newAddFollowersLink();
        add(addFollowersLink);

        // manage button
        addManageLink = newManageLink();
        add(addManageLink);

        // facebook share button panel
        FacebookSharePanel<Action> facebookSharePanel = new FacebookSharePanel<Action>("facebookShare", Model.of(action)) {
            private static final long serialVersionUID = 5783650552807954153L;

            @Override
            protected String providePageLink() {
                String url = RequestCycle
                        .get()
                        .getUrlRenderer()
                        .renderFullUrl(
                                Url.parse(RequestCycle.get()
                                        .urlFor(ActionSolutionPage.class, new PageParameters().set("id", action.getId())).toString()))
                        + "?" + pageNumber;

                return url;
            }
        };
        add(facebookSharePanel);

        // linkedIn share button panel
        LinkedInSharePanel<Action> linkedInSharePanel = new LinkedInSharePanel<Action>("linkedInShare", Model.of(action), feedbackPanel) {
            private static final long serialVersionUID = 5783650552807954153L;

            @Override
            protected String providePageLink() {
                String url = RequestCycle
                        .get()
                        .getUrlRenderer()
                        .renderFullUrl(
                                Url.parse(RequestCycle.get()
                                        .urlFor(ActionSolutionPage.class, new PageParameters().set("id", action.getId())).toString()))
                        + "?" + pageNumber;

                return url;
            }
        };

        add(linkedInSharePanel);

        // send message button
        add(createMessageButton(action.getPostedBy()));
        add(createMessageModelWindow());

        // Team
        List<User> teamMembers = new LinkedList<>();
        if (action.getTeamMembers() != null && action.getTeamMembers().size() > 4) {
            teamMembers = action.getTeamMembers().subList(0, 4);
        } else {
            teamMembers = action.getTeamMembers();
        }

        teamMembersView = new ListView<User>("teamMembers", teamMembers) {
            private static final long serialVersionUID = -1540809427150384449L;

            @Override
            protected void populateItem(ListItem<User> item) {
                AjaxLink<Void> link = userImageLink("teamMemberLink", item.getModelObject());
                link.add(new NonCachingImage("con_img",
                        ProfilePictureResource.get(PictureType.THUMBNAIL, item.getModelObject())));
                item.add(link);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        add(teamMembersView);

        AjaxLink<Void> seeAllLink = new AjaxLink<Void>("seeAll") {
            private static final long serialVersionUID = -2024160838194169142L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(ActionTeamMembersListPage.class, new PageParameters().set("id", action.getId()));
            }
        };
        seeAllLink.setOutputMarkupId(true);
        add(seeAllLink);

        add(new AjaxLink<Void>("solution") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(ActionSolutionPage.class, new PageParameters().set("id", action.getId()));
            }
        });

        add(new AjaxLink<Void>("businessModel") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(ActionBusinessModelDetailPage.class,
                        new PageParameters().set("id", action.getId()).set("businessModelId", action.getBusinessModel().getId()));
            }
        });

        add(new AjaxLink<Void>("iterations") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(ActionIterationsListPage.class, new PageParameters().set("id", action.getId()));
            }
        });
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        pageNumber = getPage().getPageId();
    }

    protected void activateCurrentActionTab(IHeaderResponse response, final String currentTabId) {
        // make current tab "active", all others "inactive"
        response.render(OnDomReadyHeaderItem.forScript("$('#actionTabs > li').removeClass('active');$('#" + currentTabId
                + "').addClass('active');"));
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

    protected NonCachingImage newChallengeProfilePicturePreview(Action action) {
        actionProfilePicture = new NonCachingImage("profilePicturePreview", ActionPictureResource.get(PictureType.PROFILE,
                action));
        actionProfilePicture.setOutputMarkupId(true);
        return actionProfilePicture;
    }

    /**
     * @param user
     * @return
     */
    private AjaxLink<Void> userImageLink(String wicketId, final User user) {
        AjaxLink<Void> link = new AjaxLink<Void>(wicketId) {
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

    public AjaxLink<Void> newAddFollowersLink() {

        return new AjaxLink<Void>("addFollowers") {
            private static final long serialVersionUID = 8719961604750452803L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(loggedInUser != null && !loggedInUser.equals(action.getPostedBy()));
                setEnabled(loggedInUser != null && !loggedInUser.equals(action.getPostedBy()));
                if (!userService.isUserFollowsGivenAction(action, loggedInUser.getId())) {
                    // if loggedInUser is not a follower
                    add(new AttributeModifier("value", new StringResourceModel("follow.button", this, null).getString()));
                } else {
                    add(new AttributeModifier("value", new StringResourceModel("unfollow.button", this, null).getString()));
                }

                setOutputMarkupId(true);
            }

            ;

            @Override
            public void onClick(AjaxRequestTarget target) {
                // if loggedInUsr is not a follower
                if (!userService.isUserFollowsGivenAction(action, loggedInUser.getId())) {
                    // follow Action
                    loggedInUser = userService.addActionToFollowedActionsList(action, loggedInUser.getId());
                } else {
                    // Unfollow Action
                    loggedInUser = userService.removeActionFromFollowedActionsList(action, loggedInUser.getId());
                }
                target.add(this);

                onFollowersUpdate(target);
            }
        };

    }

    public AjaxLink<Void> newManageLink() {

        return new AjaxLink<Void>("manageLink") {
            private static final long serialVersionUID = 8719961604750452803L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                add(new AttributeModifier("value", new StringResourceModel("manage.button", this, null).getString()));
                setOutputMarkupId(true);
                // if current user is not action leader, do not show this button
                if (!action.isEditableBy(loggedInUser)) {
                    setVisible(false);
                }
            }

            ;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionBusinessModelAddEditPage.class,
                        new PageParameters().set("id", action.getId()).set("businessModelId", action.getBusinessModel().getId()));
            }
        };

    }

    public ListView<ActionTeamTool> newListViewForTeamTools(final String allTools, final String exteranlLinkId,
                                                            final IModel<List<? extends ActionTeamTool>> listIModel) {
        return new ListView<ActionTeamTool>(allTools, listIModel) {
            private static final long serialVersionUID = -6208493438326553258L;

            @Override
            protected void populateItem(final ListItem<ActionTeamTool> item) {
                final ActionTeamTool key = item.getModelObject();
                item.add(new ExternalLink(exteranlLinkId, key.getUrl(), key.getToolName()));
            }
        };
    }

    protected abstract void onFollowersUpdate(AjaxRequestTarget target);
}

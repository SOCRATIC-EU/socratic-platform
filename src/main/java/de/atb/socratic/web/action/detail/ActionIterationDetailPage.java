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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.ActionTeamTool;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.common.comment.CommonCommentListPanel;
import de.atb.socratic.web.components.resource.ActionIterationPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import de.atb.socratic.web.inception.idea.VotingPanel;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionIterationDetailPage extends BasePage {

    private static final long serialVersionUID = 7378255719158818040L;

    @Inject
    Logger logger;

    @EJB
    ActionIterationService actionIterationService;

    @EJB
    ActivityService activityService;

    @EJB
    ActionService actionService;

    @EJB
    UserService userService;

    private ActionIteration theIteration;
    private Action theAction;

    private VotingPanel votingPanel;

    private CreateMessagePanel messageModelPanel;
    private ModalWindow createMessageModalWindow;

    private NonCachingImage iterationProfilePicture;

    private DataView<User> contributorsView;
    // container holding the list of contributors
    private final WebMarkupContainer contributorsContainer;

    private static final int itemsPerPage = 3;
    private ContribuotrsProvider contribuotrsProvider;
    private final WebMarkupContainer commentsContainer;
    private final ListView<ActionTeamTool> teamToolsListView;

    @Inject
    @LoggedInUser
    User loggedInUser;

    final CommonActionResourceHeaderPanel<Action> headerPanel;

    public ActionIterationDetailPage(final PageParameters parameters) {
        super(parameters);

        loadActionAndIteration(parameters);

        headerPanel = new CommonActionResourceHeaderPanel<Action>("commonHeaderPanel", Model.of(theAction), feedbackPanel) {
            private static final long serialVersionUID = 4494582353460389258L;

            @Override
            protected void onFollowersUpdate(AjaxRequestTarget target) {
            }
        };
        add(headerPanel);
        add(new Label("iterationNumberLabel", actionService.getIterationNumber(theAction.getId(), theIteration)));
        add(new Label("title", new PropertyModel<>(theIteration, "title")));

        add(new Label("aimOfExperiment", new PropertyModel<>(theIteration, "aimOfExperiment")));
        add(new Label("methodology", new PropertyModel<>(theIteration, "methodology")));
        add(new Label("plan", new PropertyModel<>(theIteration, "plan")));
        add(newItertaionProfilePicturePreview(theIteration));

        add(new Label("callToAction", new PropertyModel<>(theAction, "callToAction")));

        // contributors: add repeating view
        contributorsContainer = new WebMarkupContainer("contributorsContainer");
        add(contributorsContainer.setOutputMarkupId(true));

        contribuotrsProvider = new ContribuotrsProvider();

        contributorsView = new DataView<User>("contributors", contribuotrsProvider, itemsPerPage) {
            private static final long serialVersionUID = -1540809427150384449L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }

            @Override
            protected void populateItem(Item<User> item) {
                AjaxLink<Void> link = userImageLink("userProfileDetailLink", item.getModelObject());
                link.add(new NonCachingImage("con_img",
                        ProfilePictureResource.get(PictureType.THUMBNAIL, item.getModelObject())));
                item.add(link);
                item.add(new Label("con_name", item.getModelObject().getNickName()));
            }
        };
        contributorsContainer.add(contributorsView);

        add(new BootstrapAjaxPagingNavigator("pagination", contributorsView) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(contributorsView.getPageCount() > 1);
            }
        });

        // commentsPanel
        commentsContainer = new WebMarkupContainer("commentsContainer");
        add(commentsContainer.setOutputMarkupId(true));

        CommonCommentListPanel<ActionIteration> commentsPanel = new CommonCommentListPanel<ActionIteration>("commentsPanel",
                Model.of(theIteration), true) {
            private static final long serialVersionUID = -2329905767924123369L;

            @Override
            protected int getTotalCommentSize() {
                return this.getModelObject().getComments().size();
            }

            @Override
            protected List<Comment> getListOfComments() {
                return this.getModelObject().getComments();
            }

            @Override
            protected void deleteComment(final Comment comment) {
                actionIterationService.removeComment(this.getModelObject().getId(), comment);
            }

            @Override
            protected void onCommentListChanged(AjaxRequestTarget target) {
                target.add(contributorsContainer);
                target.add(commentsContainer);

                // Update AjaxLink
                headerPanel.addFollowersLink.configure();
                target.add(headerPanel.addFollowersLink);
                target.add(headerPanel);
            }
        };

        commentsContainer.add(commentsPanel);
        commentsContainer.add(commentsPanel.getCommentsNumberTopOnTheButton());
        add(commentsPanel.getCommentsNumberLeftSideOfTheButton());

        // send message button
        add(createMessageButton(theAction.getPostedBy()));
        add(createMessageModelWindow());

        // Action voting
        votingPanel = new VotingPanel<ActionIteration>("thumbsUpVoting", Model.of(theIteration)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting up");
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting down");
            }
        };
        add(votingPanel);

        // add a back link
        add(new AjaxLink<User>("back") {
            private static final long serialVersionUID = -4776506958975416730L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(ActionIterationsListPage.class, new PageParameters().set("id", theAction.getId()));
            }

            @Override
            protected void onConfigure() {
                setVisible(true);
                super.onConfigure();
            }
        });

        // Add team tool list view
        teamToolsListView = headerPanel.newListViewForTeamTools("allTeamTools", "toolDescriptionViewLabel", Model.ofList(theAction.getActionTeamTools()));
        add(teamToolsListView.setOutputMarkupId(true));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "actionsTab");
        headerPanel.activateCurrentActionTab(response, "iterationsTab");
    }

    /**
     * @param
     * @return
     */
    protected AjaxLink<Void> createMessageButton(final User user) {
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
    protected ModalWindow createMessageModelWindow() {
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

    private void loadActionAndIteration(PageParameters parameters) {
        final Long iterationId = parameters.get("iterationId").toOptionalLong();
        if (iterationId != null) {
            theIteration = actionIterationService.getById(iterationId);
        } else {
            theIteration = null;
        }

        final Long actionId = parameters.get("id").toOptionalLong();
        if (actionId != null) {
            theAction = actionService.getById(actionId);
        } else {
            theAction = null;
        }
    }

    private NonCachingImage newItertaionProfilePicturePreview(ActionIteration iteration) {
        iterationProfilePicture = new NonCachingImage("profilePicturePreview", ActionIterationPictureResource.get(
                PictureType.PROFILE, iteration));
        iterationProfilePicture.setOutputMarkupId(true);
        return iterationProfilePicture;
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

    /**
     * @author ATB
     */
    private final class ContribuotrsProvider extends EntityProvider<User> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends User> iterator(long first, long count) {

            List<User> contributors = activityService.getAllActionContributorsBasedOnActionActivity(theAction, (int) first,
                    (int) count);
            return contributors.iterator();
        }

        @Override
        public long size() {
            return activityService.countAllActionContributorsBasedOnActionActivity(theAction);
        }
    }

}

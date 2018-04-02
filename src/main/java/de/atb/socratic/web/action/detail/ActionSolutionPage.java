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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.ActionTeamTool;
import de.atb.socratic.model.Comment;
import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.IdeaType;
import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.ScopeService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.common.comment.CommonCommentListPanel;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.idea.VotingPanel;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionSolutionPage extends BasePage {

    private static final long serialVersionUID = 7378255719158818040L;

    @Inject
    Logger logger;

    @EJB
    ActionService actionService;

    @EJB
    ActivityService activityService;

    @EJB
    UserService userService;

    @EJB
    ScopeService scopeService;

    private Action action;

    private VotingPanel votingPanel;

    private final ListView<ActionTeamTool> teamToolsListView;

    @Inject
    @LoggedInUser
    User loggedInUser;

    final CommonActionResourceHeaderPanel<Action> headerPanel;

    private Label followersTotalNoLabel;

    public ActionSolutionPage(final PageParameters parameters) {
        super(parameters);

        final Long actionId = parameters.get("id").toOptionalLong();
        if (actionId != null) {
            action = actionService.getById(parameters.get("id").toOptionalLong());
        } else {
            action = null;
        }

        // challengeDefinition Header
        headerPanel = new CommonActionResourceHeaderPanel<Action>("commonHeaderPanel", Model.of(action), feedbackPanel) {
            private static final long serialVersionUID = 4494582353460389258L;

            @Override
            protected void onFollowersUpdate(AjaxRequestTarget target) {
                // Update followersTotalNoLabel
                followersTotalNoLabel.setDefaultModelObject(userService.countAllUsersByGivenFollowedAction(action));
                target.add(followersTotalNoLabel);
            }
        };
        add(headerPanel);

        add(new Label("titleOfAction", new PropertyModel<>(action, "shortText")));

        add(new Label("descriptionOfAction", new PropertyModel<>(action, "description")));
        add(new Label("beneficiaries", new PropertyModel<>(action, "beneficiaries")));

        // optional fields, hide them if not provided by idea leader        
        add(newLabelForOptionalFields("valueForBeneficiariesHeader", "action.definition.label.value",
                action.getValueForBeneficiaries() != null && !action.getValueForBeneficiaries().isEmpty()));
        add(new Label("valueForBeneficiaries", new PropertyModel<>(action, "valueForBeneficiaries")));

        add(newLabelForOptionalFields("impactStakeholdersHeader", "action.definition.label.impact",
                action.getImpactStakeholders() != null && !action.getImpactStakeholders().isEmpty()));
        add(new Label("impactStakeholders", new PropertyModel<>(action, "impactStakeholders")));

        add(newLabelForOptionalFields("resourcesForActionImplementationHeader", "action.definition.label.resources",
                action.getResourcesForActionImplementation() != null && !action.getResourcesForActionImplementation().isEmpty()));
        add(new Label("resourcesForActionImplementation", new PropertyModel<>(action, "resourcesForActionImplementation")));

        add(newLabelForOptionalFields("implementationPlanHeader", "action.definition.label.implementation",
                action.getImplementationPlan() != null && !action.getImplementationPlan().isEmpty()));
        add(new Label("implementationPlan", new PropertyModel<>(action, "implementationPlan")));

        add(newLabelForOptionalFields("locationHeader", "action.definition.label.location",
                action.getLocation() != null && !action.getLocation().isEmpty()));
        add(new Label("location", new PropertyModel<>(action, "location")));

        add(newLabelForOptionalFields("reasonForBringingActionForwardHeader", "action.definition.label.rightperson",
                action.getReasonForBringingActionForward() != null && !action.getReasonForBringingActionForward().isEmpty()));
        add(new Label("reasonForBringingActionForward", new PropertyModel<>(action, "reasonForBringingActionForward")));

        add(newLabelForOptionalFields("relatedInnovationsHeader", "action.definition.label.innovation",
                action.getRelatedInnovations() != null && !action.getRelatedInnovations().isEmpty()));
        add(new Label("relatedInnovations", new PropertyModel<>(action, "relatedInnovations")));

        add(new Label("callToAction", new PropertyModel<>(action, "callToAction")));
        add(showIterationLink("showIterationLink"));

        add(new Label("previousChallengeText", new PropertyModel<>(action.getIdea().getCampaign(), "name")));
        add(showChallengeLink("showChallengeLink"));

        // add follower label locally
        this.followersTotalNoLabel = new Label("followersTotalNoLabel", userService.countAllUsersByGivenFollowedAction(action));
        this.followersTotalNoLabel.setOutputMarkupId(true);
        add(this.followersTotalNoLabel);

        // commentsPanel
        final WebMarkupContainer commentsContainer = new WebMarkupContainer("commentsContainer");
        add(commentsContainer.setOutputMarkupId(true));

        CommonCommentListPanel<Action> commentsPanel = new CommonCommentListPanel<Action>("commentsPanel", Model.of(action), true) {
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
                actionService.removeComment(this.getModelObject().getId(), comment);
            }

            @Override
            protected void onCommentListChanged(AjaxRequestTarget target) {
                // Update AjaxLink
                headerPanel.addFollowersLink.configure();
                target.add(headerPanel.addFollowersLink);

                // Update followersTotalNoLabel
                followersTotalNoLabel.setDefaultModelObject(userService.countAllUsersByGivenFollowedAction(action));
                target.add(followersTotalNoLabel);
                target.add(headerPanel);
            }
        };

        commentsContainer.add(commentsPanel);
        commentsContainer.add(commentsPanel.getCommentsNumberTopOnTheButton());
        add(commentsPanel.getCommentsNumberLeftSideOfTheButton());


        // Action voting
        votingPanel = new VotingPanel<Action>("thumbsUpVoting", Model.of(action)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void voteUpOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting up");
                // notify about Action comments and likes
                actionService.notifyActionFollowersAboutActionCommentsAndLikes(action);
            }

            @Override
            public void voteDownOnClick(AjaxRequestTarget target, Component component) {
                System.out.println("voting down");
            }
        };
        add(votingPanel);


        // actiontypes
        final List<IdeaType> actionTypes = action.getActionType();

        // non repeating set of actionTypes
        Set<String> actionTypesStrings = new HashSet<>();
        for (IdeaType type : actionTypes) {
            if (type.equals(IdeaType.Others)) {
                // if action type text is not null then use it...
                if (action.getActionTypeText() != null) {
                    actionTypesStrings.add(action.getActionTypeText());
                } else {
                    actionTypesStrings.add(type.getType());
                }
            } else {
                actionTypesStrings.add(type.getType());
            }
        }
        add(new MultiLineLabel("actionTypes", actionTypesStrings.toString().replace("[", "").replace("]", "").replace(", ", "\n")));

        // keywords
        add(newIdeaKeywordsPanel(action));

        // skills
        add(newIdeaSkillsPanel(action));

        // Add team tool list view
        teamToolsListView = headerPanel.newListViewForTeamTools("allTeamTools", "toolDescriptionViewLabel", Model.ofList(action.getActionTeamTools()));
        add(teamToolsListView.setOutputMarkupId(true));

        // add lessons learnt from latest iteration
        String lessonsLearnt = "";
        List<ActionIteration> actionIterations = actionService.getAllActionIterationsByDescendingCreationDate(actionId,
                Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (ActionIteration actionIteration : actionIterations) {
            if (actionIteration.getLessonsLearnt() != null && !actionIteration.getLessonsLearnt().isEmpty()) {
                lessonsLearnt = actionIteration.getLessonsLearnt();
                break;
            }
        }

        // hide the title if lessons learnt is empty
        final boolean flagForLessonsLearntLabel = lessonsLearnt.equals("") ? false : true;
        Label lessonsLearntTitleLabel = new Label("lessonsLearntTitleLabel", new StringResourceModel(
                "action.iteration.label.lessonsLearnt", this, null)) {
            private static final long serialVersionUID = 2613281975533260598L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(flagForLessonsLearntLabel);
            }
        };
        add(lessonsLearntTitleLabel);

        Label lessonsLearntTextLabel = new Label("lessonsLearntTextLabel", lessonsLearnt);
        add(lessonsLearntTextLabel);

        // attachments
        add(newAttachtmentsList(action));

    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "actionsTab");
        headerPanel.activateCurrentActionTab(response, "solutionTab");
    }

    private ListView<FileInfo> newAttachtmentsList(final Action action) {
        return new ListView<FileInfo>("attachments", new ArrayList<>(action.getAttachments())) {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            protected void populateItem(ListItem<FileInfo> item) {
                FileInfo file = item.getModelObject();
                item.add(new DownloadLink("fileLink", new Model<>(file.getFile())) {
                    private static final long serialVersionUID = 5621866025141144279L;

                    @Override
                    public void onClick() {
                        super.onClick();
                        File file = getModelObject();
                        IResourceStream resourceStream = new FileResourceStream(new org.apache.wicket.util.file.File(file));
                        getRequestCycle().scheduleRequestHandlerAfterCurrent(
                                new ResourceStreamRequestHandler(resourceStream, file.getName()));
                    }
                }.add(new Label("fileLabel", file.getDisplayName())));
            }
        };
    }

    private Label newLabelForOptionalFields(String wicketId, String resourceModelId, final boolean isVisible) {
        return new Label(wicketId, new StringResourceModel(resourceModelId, this, null)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setOutputMarkupId(true);
                setVisible(isVisible);
            }
        };
    }

    private AjaxLink<Void> showIterationLink(String markUpId) {
        AjaxLink<Void> showIterationLink = new AjaxLink<Void>(markUpId) {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                ActionIteration latestIteration = actionService.getLatestIterationOfAction(action.getId());
                setResponsePage(ActionIterationDetailPage.class,
                        new PageParameters().set("id", action.getId()).set("iterationId", latestIteration.getId()));
            }
        };

        return showIterationLink;
    }

    private AjaxLink<ChallengeDefinitionPage> showChallengeLink(String markUpId) {
        AjaxLink<ChallengeDefinitionPage> showIterationLink = new AjaxLink<ChallengeDefinitionPage>(markUpId) {
            private static final long serialVersionUID = 202363713959040288L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(ChallengeDefinitionPage.class,
                        new PageParameters().set("id", action.getIdea().getCampaign().getId()));
            }
        };

        return showIterationLink;
    }

    private WebMarkupContainer newIdeaSkillsPanel(final Action action) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("skillsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!action.getKeywords().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("skills.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("skills", new ListModel<>(action.getSkills())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("skill", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
    }

    private WebMarkupContainer newIdeaKeywordsPanel(final Action action) {
        final WebMarkupContainer tagsContainer = new WebMarkupContainer("keywordsContainer") {
            private static final long serialVersionUID = 7869210537650985756L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!action.getKeywords().isEmpty());
            }
        };
        tagsContainer.setOutputMarkupPlaceholderTag(true);
        tagsContainer.add(new AttributeModifier("title", new StringResourceModel("keywords.title.text", this, null)));

        tagsContainer.add(new ListView<Tag>("keywords", new ListModel<>(action.getKeywords())) {
            private static final long serialVersionUID = -707257134087075019L;

            @Override
            protected void populateItem(ListItem<Tag> item) {
                item.add(new Label("keyword", item.getModelObject().getTag()));
            }
        });
        return tagsContainer;
    }
}

package de.atb.socratic.web.dashboard.coordinator.overview;

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

import javax.ejb.EJB;

import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionBusinessModelService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.CommentService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.dashboard.coordinator.DashboardCommonHeaderPage;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class CoordinatorDashboardOverviewPage extends DashboardCommonHeaderPage {
    private static final long serialVersionUID = -7085939814517735639L;

    @EJB
    CampaignService campaignService;

    @EJB
    IdeaService ideaService;

    @EJB
    ActionService actionService;

    @EJB
    ActionIterationService actionIterationService;

    @EJB
    ActionBusinessModelService businessModelService;

    @EJB
    CommentService commentService;

    @EJB
    UserService userService;

    @EJB
    ActivityService activityService;

    public CoordinatorDashboardOverviewPage(PageParameters parameters) {
        super(parameters);

        // totalNoChallenges
        Label totalNoChallengesLabel = new Label("totalNoChallengesLabel", campaignService.countAll());
        totalNoChallengesLabel.setOutputMarkupId(true);
        add(totalNoChallengesLabel);

        // totalNoIdeas
        Label totalNoIdeasLabel = new Label("totalNoIdeasLabel", ideaService.countAll());
        totalNoIdeasLabel.setOutputMarkupId(true);
        add(totalNoIdeasLabel);

        // totalNoActions
        Label totalNoActionsLabel = new Label("totalNoActionsLabel", actionService.countAll());
        totalNoActionsLabel.setOutputMarkupId(true);
        add(totalNoActionsLabel);

        // totalNoIterations
        Label totalNoIterationsLabel = new Label("totalNoIterationsLabel", actionIterationService.countAll());
        totalNoIterationsLabel.setOutputMarkupId(true);
        add(totalNoIterationsLabel);

        // totalNoBusinessModels
        Label totalNoBusinessModelsLabel = new Label("totalNoBusinessModelsLabel", businessModelService.countAll());
        totalNoBusinessModelsLabel.setOutputMarkupId(true);
        add(totalNoBusinessModelsLabel);

        // totalNoComments
        Label totalNoCommentsLabel = new Label("totalNoCommentsLabel", commentService.countAll());
        totalNoCommentsLabel.setOutputMarkupId(true);
        add(totalNoCommentsLabel);

        // totalNoLikes
        Label totalNoLikesLabel = new Label("totalNoLikesLabel", activityService.getTotalNoOfLikesForAllProcesses());
        totalNoLikesLabel.setOutputMarkupId(true);
        add(totalNoLikesLabel);

        // references

        // totalNoVotesForIdea
        Label totalNoVotesForIdeaLabel = new Label("totalNoVotesForIdeaLabel", ideaService.countTotalVotesForAllIdeas());
        totalNoVotesForIdeaLabel.setOutputMarkupId(true);
        add(totalNoVotesForIdeaLabel);

        // shares

        // totalNoUsers
        Label totalNoUsersLabel = new Label("totalNoUsersLabel", userService.countAll());
        totalNoUsersLabel.setOutputMarkupId(true);
        add(totalNoUsersLabel);

        // totalNoFollowers
        Label totalNoFollowersLabel = new Label("totalNoFollowersLabel", userService.countAllUsersFollowingAllProcesses(null, null));
        totalNoFollowersLabel.setOutputMarkupId(true);
        add(totalNoFollowersLabel);

        // totalNoContributors
        Label totalNoContributorsLabel = new Label("totalNoContributorsLabel", activityService.countAllContributorsFromAllProcesses(null, null));
        totalNoContributorsLabel.setOutputMarkupId(true);
        add(totalNoContributorsLabel);

        // totalNoLeaders
        Label totalNoLeadersLabel = new Label("totalNoLeadersLabel", userService.countAllUsersLeadingAllProcesses(null, null));
        totalNoLeadersLabel.setOutputMarkupId(true);
        add(totalNoLeadersLabel);

    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "overviewTab");
    }
}

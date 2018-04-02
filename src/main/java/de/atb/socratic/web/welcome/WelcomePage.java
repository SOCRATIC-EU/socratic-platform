package de.atb.socratic.web.welcome;

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

import java.util.Iterator;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.AboutPage;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.action.detail.ActionsListPage;
import de.atb.socratic.web.components.InnovationStatusIndicatorPanel;
import de.atb.socratic.web.components.resource.ActionPictureResource;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.IdeaPictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.dashboard.Dashboard.StateForDashboard;
import de.atb.socratic.web.dashboard.panels.ActionPanel;
import de.atb.socratic.web.dashboard.panels.ChallengePanel;
import de.atb.socratic.web.dashboard.panels.IdeasPanel;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import de.atb.socratic.web.inception.CampaignsPage;
import de.atb.socratic.web.provider.EntityProvider;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@ApplicationScoped
public class WelcomePage extends BasePage {

    @EJB
    CampaignService campaignService;

    @EJB
    IdeaService ideaService;

    @EJB
    ActionService actionService;

    @EJB
    ActivityService activityService;

    @Inject
    Logger logger;

    private DataView<Campaign> latestChallengesView;
    private DataView<Idea> latestIdeasView;
    private DataView<Action> latestActionsView;

    private List<Campaign> latestChallenges;
    private BookmarkablePageLink<CampaignsPage> linkToChallenges;
    private BookmarkablePageLink<CampaignsPage> moreChallenges;

    private BookmarkablePageLink<ActionsListPage> moreActions;
    private BookmarkablePageLink createChallenges;

    private CampaignProvider campaignProvider;
    private IdeaProvider ideaProvider;
    private ActionProvider actionProvider;

    private User loggedInUser;
    private NonCachingImage challengeProfilePicture;
    private NonCachingImage ideaProfilePicture;
    private NonCachingImage actionProfilePicture;

    public static final String CHALLENGE_ANCHOR_PREFIX = "_challenge_";
    public static final String IDEA_ANCHOR_PREFIX = "_idea_";
    public static final String ACTION_ANCHOR_PREFIX = "_action_";
    final int itemsPerPage = 3;

    /**
     * @param parameters
     */
    public WelcomePage(PageParameters parameters) {
        super(parameters);

        loggedInUser = loggedInUserProvider.getLoggedInUser();

        // Active Challenges
        campaignProvider = new CampaignProvider();

        latestChallengesView = new DataView<Campaign>("challengesRepeater", campaignProvider, itemsPerPage) {
            private static final long serialVersionUID = -1057487410826162720L;

            @Override
            protected void populateItem(Item<Campaign> item) {
                Campaign challenge = item.getModelObject();

                //final PageParameters params = ;
                item.setOutputMarkupId(true);
                item.add(newChallengeProfilePicturePreview(challenge));
                item.add(newChallengeAnchor(challenge.getId()));
                item.add(newChallengePanel("challengePanel", item, challenge, StateForDashboard.TakePart));
                item.add(newStageIndicator(challenge));
            }
        };
        add(latestChallengesView);
        moreChallenges = newGenericLink("welcome.challenges.link", CampaignsPage.class);
        add(moreChallenges);

        // Active Ideas
        ideaProvider = new IdeaProvider();

        latestIdeasView = new DataView<Idea>("ideasRepeater", ideaProvider, itemsPerPage) {
            private static final long serialVersionUID = -1057487410826162720L;

            @Override
            protected void populateItem(Item<Idea> item) {
                Idea idea = item.getModelObject();
                item.setOutputMarkupId(true);
                item.add(newIdeaProfilePicturePreview(idea));
                item.add(newIdeaAnchor(idea.getId()));
                item.add(newIdeaPanel("ideaPanel", item, idea, StateForDashboard.TakePart));
            }
        };
        add(latestIdeasView);

        // Active Actions
        actionProvider = new ActionProvider();

        latestActionsView = new DataView<Action>("actionsRepeater", actionProvider, itemsPerPage) {
            private static final long serialVersionUID = -1057487410826162720L;

            @Override
            protected void populateItem(Item<Action> item) {
                Action action = item.getModelObject();
                item.setOutputMarkupId(true);
                item.add(newActionProfilePicturePreview(action));
                item.add(newActionAnchor(action.getId()));
                item.add(newActionPanel("actionPanel", item, action, StateForDashboard.TakePart));
            }
        };
        add(latestActionsView);
        moreActions = newGenericLink("welcome.actions.link", ActionsListPage.class);
        add(moreActions);

        // rest of links
        linkToChallenges = newGenericLink("welcome.takePart.link", CampaignsPage.class);
        add(linkToChallenges);
        createChallenges = newLinkToCreateChallenges("welcome.challenges.create");
        add(createChallenges);
        add(new BookmarkablePageLink<AboutPage>("aboutLink", AboutPage.class, new PageParameters()));

    }

    /**
     * @param challenge
     * @return
     */
    public ChallengePanel newChallengePanel(final String id, final MarkupContainer item, final Campaign challenge,
                                            final StateForDashboard dashboardState) {
        return new ChallengePanel(id, Model.of(challenge), dashboardState) {
            private static final long serialVersionUID = -1078593562271992866L;
        };
    }

    private InnovationStatusIndicatorPanel newStageIndicator(Campaign campaign) {
        InnovationStatusIndicatorPanel innovationStatusIndicatorPanel = new InnovationStatusIndicatorPanel("status",
                Model.of(campaign));
        return innovationStatusIndicatorPanel;
    }

    public Component newChallengeAnchor(final Long id) {
        return new WebMarkupContainer("challengeAnchorMan").setMarkupId(CHALLENGE_ANCHOR_PREFIX + id)
                .setOutputMarkupId(true);
    }

    protected NonCachingImage newChallengeProfilePicturePreview(Campaign challenge) {
        challengeProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(
                PictureType.PROFILE, challenge));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
    }

    /**
     * @param idea
     * @return
     */
    public IdeasPanel newIdeaPanel(final String id, final MarkupContainer item, final Idea idea, final StateForDashboard dashboardState) {
        return new IdeasPanel(id, Model.of(idea), dashboardState) {
            private static final long serialVersionUID = -1078593562271992866L;
        };
    }

    protected NonCachingImage newIdeaProfilePicturePreview(Idea idea) {
        ideaProfilePicture = new NonCachingImage("ideasProfilePicturePreview", IdeaPictureResource.get(
                PictureType.PROFILE, idea));
        ideaProfilePicture.setOutputMarkupId(true);
        return ideaProfilePicture;
    }

    public Component newIdeaAnchor(final Long id) {
        return new WebMarkupContainer("ideaAnchorMan").setMarkupId(IDEA_ANCHOR_PREFIX + id)
                .setOutputMarkupId(true);
    }

    /**
     * @param action
     * @return
     */
    public ActionPanel newActionPanel(final String id, final MarkupContainer item, final Action action,
                                      final StateForDashboard dashboardState) {
        return new ActionPanel(id, Model.of(action), dashboardState) {
            private static final long serialVersionUID = -1078593562271992866L;
        };
    }

    protected NonCachingImage newActionProfilePicturePreview(Action action) {
        actionProfilePicture = new NonCachingImage("actionsProfilePicturePreview", ActionPictureResource.get(PictureType.PROFILE,
                action));
        actionProfilePicture.setOutputMarkupId(true);
        return actionProfilePicture;
    }

    public Component newActionAnchor(final Long id) {
        return new WebMarkupContainer("actionAnchorMan").setMarkupId(ACTION_ANCHOR_PREFIX + id).setOutputMarkupId(true);
    }

    private <T> BookmarkablePageLink<T> newLinkToCreateChallenges(String markupid) {
        logger.info("loggedInUser: " + loggedInUser);
        if (loggedInUser != null && loggedInUser.hasAnyRoles(UserRole.ADMIN, UserRole.MANAGER, UserRole.SUPER_ADMIN)) {
            return new BookmarkablePageLink<>(markupid, CampaignAddEditPage.class, new PageParameters());
        } else {
            return new BookmarkablePageLink<>(markupid, CampaignsPage.class, new PageParameters());
        }
    }

    private <T> BookmarkablePageLink<T> newGenericLink(String markupid, Class clazz) {
        return new BookmarkablePageLink<>(markupid, clazz, new PageParameters());
    }

    /**
     * @author ATB
     */
    private final class CampaignProvider extends EntityProvider<Campaign> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends Campaign> iterator(long first, long count) {

            List<Campaign> allTopicCampaigns = activityService.getActiveChallengesByLatestChallengeActivity(Long.valueOf(first)
                    .intValue(), Long.valueOf(count).intValue());
            return allTopicCampaigns.iterator();
        }

        @Override
        public long size() {
            return activityService.countActiveChallengesByLatestChallengeActivity();
        }
    }

    /**
     * @author ATB
     */
    private final class IdeaProvider extends EntityProvider<Idea> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {

            List<Idea> allTopicIdeas = activityService.getActiveIdeasByLatestIdeaActivity(Long.valueOf(first)
                    .intValue(), Long.valueOf(count).intValue());
            return allTopicIdeas.iterator();
        }

        @Override
        public long size() {
            return activityService.countActiveIdeasByLatestIdeaActivity();
        }
    }

    /**
     * @author ATB
     */
    private final class ActionProvider extends EntityProvider<Action> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends Action> iterator(long first, long count) {

            List<Action> allTopicIdeas = activityService.getActiveActionsByLatestActionActivity(Long.valueOf(first)
                    .intValue(), Long.valueOf(count).intValue());
            return allTopicIdeas.iterator();
        }

        @Override
        public long size() {
            return activityService.countActiveActionsByLatestActionActivity();
        }
    }

}

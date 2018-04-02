package de.atb.socratic.web.dashboard.panels;

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
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.components.InnovationStatusIndicatorPanel;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.dashboard.Dashboard.StateForDashboard;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ChallengeListPanel extends GenericPanel<User> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing campaigns
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    CampaignService campaignService;

    @EJB
    ActivityService activityService;

    // container holding the list of challenges
    private final WebMarkupContainer challengesContainer;

    // Repeating view showing the list of existing challenges
    private final DataView<Campaign> challengesRepeater;

    private final EntityProvider<Campaign> challengeProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private NonCachingImage challengeProfilePicture;

    public static final String CHALLENGE_ANCHOR_PREFIX = "_challenge_";

    @Inject
    @LoggedInUser
    protected User loggedInUser;

    final User user;

    private EntitiySortingCriteria sortingCriteria = EntitiySortingCriteria.created; // by default 

    public ChallengeListPanel(final String id, final IModel<User> model, final StyledFeedbackPanel feedbackPanel,
                              final StateForDashboard dashboardState, final int itemsPerPage) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        // get the user
        user = getModelObject();

        // add container with list of existing challenges
        challengesContainer = new WebMarkupContainer("challengesContainer");
        add(challengesContainer.setOutputMarkupId(true));

        // add repeating view with list of existing challenges
        challengeProvider = new ChallengeProvider(user, dashboardState);
        challengesRepeater = new DataView<Campaign>("challengesRepeater", challengeProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Campaign> item) {
                item.setOutputMarkupId(true);
                ChallengeListPanel.this.populateItem(item, item.getModelObject(), dashboardState);
            }
        };
        challengesContainer.add(challengesRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", challengesRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(challengesRepeater.getPageCount() > 1);
            }
        });
    }

    /**
     * @param sortingCriteria
     * @return
     */
    public ChallengeListPanel setSortingCriteria(EntitiySortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    /**
     * @return
     */
    public long getChallengeListSize() {
        return challengeProvider.size();
    }

    public Component newChallengeAnchor(final Long id) {
        return new WebMarkupContainer("challengeAnchorMan").setMarkupId(CHALLENGE_ANCHOR_PREFIX + id)
                .setOutputMarkupId(true);
    }


    protected void populateItem(final WebMarkupContainer item, final Campaign challenge, final StateForDashboard dashboardState) {
        //final PageParameters params = ;
        item.setOutputMarkupId(true);
        item.add(newChallengeProfilePicturePreview(challenge));
        item.add(newChallengeAnchor(challenge.getId()));
        // change wiket id in html
        item.add(newChallengePanel("challengePanel", item, challenge, dashboardState));
        item.add(newStageIndicator(challenge));
    }

    private InnovationStatusIndicatorPanel newStageIndicator(Campaign campaign) {
        InnovationStatusIndicatorPanel innovationStatusIndicatorPanel = new InnovationStatusIndicatorPanel("status",
                Model.of(campaign));
        return innovationStatusIndicatorPanel;
    }

    protected NonCachingImage newChallengeProfilePicturePreview(Campaign challenge) {
        challengeProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(
                PictureType.PROFILE, challenge));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
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

    /**
     * @author ATB
     */
    private final class ChallengeProvider extends EntityProvider<Campaign> {
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;
        private final StateForDashboard keyword;

        public ChallengeProvider(User user, StateForDashboard keyword) {
            super();
            this.user = user;
            this.keyword = keyword;
        }

        @Override
        public Iterator<? extends Campaign> iterator(long first, long count) {
            List<Campaign> challenges = new LinkedList<>();
            if (keyword.equals(StateForDashboard.Lead)) {
                challenges = campaignService.getAllCampaignsByDescendingCreationDateAndCreatedBy(
                        Long.valueOf(first).intValue(), Long.valueOf(count).intValue(), user, sortingCriteria);
            } else if (keyword.equals(StateForDashboard.TakePart)) {
                challenges = activityService.getAllChallengesByChallengeActivityCreator(user, Long.valueOf(first).intValue(),
                        Long.valueOf(count).intValue(), sortingCriteria);
            }
            return challenges.iterator();
        }

        @Override
        public long size() {
            if (keyword.equals(StateForDashboard.Lead)) {
                return campaignService.countCampaignsForUser(user);
            } else if (keyword.equals(StateForDashboard.TakePart)) {
                return activityService.countAllChallengesByChallengeActivityCreator(user);
            }
            return 0;
        }
    }

}

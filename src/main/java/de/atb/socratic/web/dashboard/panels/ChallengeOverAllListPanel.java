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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.CampaignType;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.web.components.InnovationStatusIndicatorPanel;
import de.atb.socratic.web.components.resource.ChallengePictureResource;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.dashboard.Dashboard.EntitiySortingCriteria;
import de.atb.socratic.web.dashboard.UserDashboardPage.Status;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminActivityPage;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.selection.SelectionPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ChallengeOverAllListPanel extends GenericPanel<User> {

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

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many challenges do we show initially
    private static final int itemsPerPage = 2;

    // container holding the list of challenges
    private final WebMarkupContainer challengesContainer;

    // Repeating view showing the list of existing challenges
    private final DataView<Campaign> challengesRepeater;

    private final EntityProvider<Campaign> challengeProvider;

    private NonCachingImage challengeProfilePicture;

    private int daysLeft;

    private EntitiySortingCriteria sortingCriteria = EntitiySortingCriteria.created; // by default 

    public ChallengeOverAllListPanel(final String id, final IModel<User> model, final Status status) {
        super(id, model);

        // get the user
        final User user = getModelObject();

        // add container with list of existing challenges
        challengesContainer = new WebMarkupContainer("challengesContainer");
        add(challengesContainer.setOutputMarkupId(true));

        // add repeating view with list of existing challenges
        challengeProvider = new ChallengeProvider(user, status);
        challengesRepeater = new DataView<Campaign>("challengesRepeater", challengeProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Campaign> item) {
                item.setOutputMarkupId(true);
                ChallengeOverAllListPanel.this.populateItem(item, item.getModelObject(), status);
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

    private Link<IdeasPage> newExploreLink(final Campaign challenge, final Status dashboardState) {
        final Link<IdeasPage> link = new Link<IdeasPage>("exploreLink") {
            private static final long serialVersionUID = -310533532532643267L;

            @Override
            public void onClick() {
                if (Status.LEAD == dashboardState) {
                    setResponsePage(AdminActivityPage.class, new PageParameters().set("id", challenge.getId()));
                } else {
                    final Class<? extends IRequestablePage> targetPage =
                            challenge.getDefinitionActive() != null && challenge.getDefinitionActive()
                                    ? ChallengeDefinitionPage.class
                                    : (challenge.getIdeationActive() != null && challenge.getIdeationActive()
                                    ? IdeasPage.class : SelectionPage.class);
                    setResponsePage(targetPage, new PageParameters().set("id", challenge.getId()));
                }
            }
        };
        final String key = Status.PARTICIPATE == dashboardState ? "explore.text" : "manage.text";
        link.add(new Label("exploreLinkLabel", new StringResourceModel(key, this, null)));
        return link;
    }

    /**
     * @return
     */
    public long getChallengeListSize() {
        return challengeProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item,
                                final Campaign challenge,
                                final Status dashboardState) {

        item.setOutputMarkupId(true);
        item.add(new Label("name", new PropertyModel<String>(challenge, "name")));
        item.add(newStageIndicator(challenge));

        // challenge comments label
        Label challengeCommentLabel;
        int commentSize = 0;
        if (challenge.getComments() != null || !challenge.getComments().isEmpty()) {
            commentSize = challenge.getComments().size();
        }
        challengeCommentLabel = new Label("challengeCommentLabel", Model.of(commentSize));
        challengeCommentLabel.setOutputMarkupId(true);
        item.add(challengeCommentLabel);

        // challenge duedate label
        LocalDate dueDate = new LocalDate(getCorrespondingDateOfPhase(challenge));
        daysLeft = new Period(new LocalDate(), dueDate, PeriodType.days()).getDays();

        // this is to avoid "-x days" message 
        if (daysLeft >= 0) {
            item.add(new Label("dueDate", daysLeft + " "
                    + new StringResourceModel("days.to.go", this, null).getString() + " "
                    + new StringResourceModel(challenge.getInnovationStatus().getMessageKey(), this, null).getString()));
        } else { // if days are less than zero then change the message
            item.add(
                    new Label("dueDate", challenge.getInnovationStatus().getMessageKey() + " is finished!"));
        }

        item.add(newExploreLink(challenge, dashboardState));
        item.add(new Label("exploreMsg", new PropertyModel<String>(challenge, "callToAction")));
    }

    private Date getCorrespondingDateOfPhase(Campaign campaign) {
        switch (campaign.getInnovationStatus()) {
            case DEFINITION:
                return campaign.getChallengeOpenForDiscussionEndDate();
            case INCEPTION:
                return campaign.getIdeationEndDate();
            case PRIORITISATION:
                return campaign.getSelectionEndDate();
            default:
                return new Date();
        }
    }

    private InnovationStatusIndicatorPanel newStageIndicator(Campaign campaign) {
        InnovationStatusIndicatorPanel innovationStatusIndicatorPanel = new InnovationStatusIndicatorPanel("status", Model.of(campaign));
        innovationStatusIndicatorPanel.setVisible(campaign.getCampaignType() != CampaignType.FREE_FORM);
        return innovationStatusIndicatorPanel;
    }

    protected NonCachingImage newChallengeProfilePicturePreview(Campaign challenge) {
        challengeProfilePicture = new NonCachingImage("profilePicturePreview", ChallengePictureResource.get(
                PictureType.PROFILE, challenge));
        challengeProfilePicture.setOutputMarkupId(true);
        return challengeProfilePicture;
    }

    /**
     * @param sortingCriteria
     * @return
     */
    public ChallengeOverAllListPanel setSortingCriteria(EntitiySortingCriteria sortingCriteria) {
        this.sortingCriteria = sortingCriteria;
        return this;
    }

    /**
     * @author ATB
     */
    private final class ChallengeProvider extends EntityProvider<Campaign> {
        private static final long serialVersionUID = -1727094205049792307L;

        private final User user;
        private final Status status;

        public ChallengeProvider(User user, Status status) {
            super();
            this.user = user;
            this.status = status;
        }

        @Override
        public Iterator<? extends Campaign> iterator(long first, long count) {
            List<Campaign> challenges = new LinkedList<>();
            // based on status, call different methods
            if (status.equals(Status.PARTICIPATE)) {
                challenges = activityService.getAllChallengesByChallengeActivityCreator(user, Long
                        .valueOf(first).intValue(), Long.valueOf(count).intValue(), sortingCriteria);
            } else if (status.equals(Status.LEAD)) {
                challenges = campaignService.getAllCampaignsByDescendingCreationDateAndCreatedBy(Long.valueOf(first).intValue(),
                        Long.valueOf(count).intValue(), user, sortingCriteria);
            } else if (status.equals(Status.RECOMMENDED)) {

            }
            return challenges.iterator();
        }

        @Override
        public long size() {
            // based on status, call different methods
            if (status.equals(Status.PARTICIPATE)) {
                return activityService.countAllChallengesByChallengeActivityCreator(user);
            } else if (status.equals(Status.LEAD)) {
                return campaignService.countCampaignsForUser(user);
            } else if (status.equals(Status.RECOMMENDED)) {
                return 0;
            }
            return 0;
        }

    }
}

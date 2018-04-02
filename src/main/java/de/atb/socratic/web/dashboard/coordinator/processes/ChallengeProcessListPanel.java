package de.atb.socratic.web.dashboard.coordinator.processes;

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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.coordinator.processes.CoordinatorDashboardProcessesPage.ProcessSortingCriteria;
import de.atb.socratic.web.dashboard.coordinator.processes.CoordinatorDashboardProcessesPage.TypeOfprocess;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.CheckGroupSelector;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ChallengeProcessListPanel extends GenericPanel<Campaign> {

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

    @EJB
    UserService userService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many challenges do we show initially
    private static final int itemsPerPage = 10;

    // container holding the list of challenges
    private final WebMarkupContainer challengesContainer;

    // Repeating view showing the list of existing challenges
    private final DataView<Campaign> challengesRepeater;

    private final EntityProvider<Campaign> challengeProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private TypeOfprocess processType;

    private ProcessSortingCriteria processSortingCriteria;

    private int daysLeft;

    private CheckGroup<Campaign> challengeGroup;
    private final Form<Campaign> form;

    public ChallengeProcessListPanel(final String id, final StyledFeedbackPanel feedbackPanel, TypeOfprocess typeOfprocess) {
        super(id);

        this.feedbackPanel = feedbackPanel;

        this.processType = typeOfprocess;

        form = new Form<Campaign>("form");
        add(form);
        form.add(challengeGroup = newUserCheckGroup());

        // add container with list of existing challenges
        challengesContainer = new WebMarkupContainer("challengesContainer");
        challengeGroup.add(challengesContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("challengeGroupSelector", challengeGroup);
        challengesContainer.add(checkGroupSelector);

        // add repeating view with list of existing challenges
        challengeProvider = new ChallengeProvider();

        challengesRepeater = new DataView<Campaign>("challengesRepeater", challengeProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Campaign> item) {
                item.setOutputMarkupId(true);
                ChallengeProcessListPanel.this.populateItem(item, item.getModelObject());
            }
        };

        challengesContainer.add(challengesRepeater);

        challengeGroup.add(new BootstrapAjaxPagingNavigator("pagination", challengesRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(challengesRepeater.getPageCount() > 1);
            }
        });

    }

    /**
     * @return
     */
    private CheckGroup<Campaign> newUserCheckGroup() {
        CheckGroup<Campaign> checkGroup = new CheckGroup<Campaign>("challengeGroup", new ArrayList<Campaign>());
        checkGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = -8193184672687169923L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // updateDeleteSelectedButton(target);
            }
        });
        checkGroup.setOutputMarkupId(true);
        return checkGroup;
    }

    /**
     * @param processType
     * @param target
     * @return
     */
    public ChallengeProcessListPanel setProcessType(TypeOfprocess processType, AjaxRequestTarget target) {
        this.processType = processType;
        target.add(challengesContainer);
        target.add(challengeGroup);
        return this;
    }

    /**
     * @param processSortingCriteria
     * @return
     */
    public ChallengeProcessListPanel setProcessSortingCriteria(ProcessSortingCriteria processSortingCriteria,
                                                               AjaxRequestTarget target) {
        this.processSortingCriteria = processSortingCriteria;
        target.add(challengesContainer);
        target.add(challengeGroup);
        return this;
    }

    /**
     * @return
     */
    public long getChallengeListSize() {
        return challengeProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item, final Campaign challenge) {

        item.setOutputMarkupId(true);
        item.add(newSelectionCheck(challenge));
        item.add(new Label("createdOn", new PropertyModel<String>(challenge, "createdOn")));
        item.add(new Label("name", new PropertyModel<String>(challenge, "name")));

        // add follower label locally
        Label followersTotalNoLabel = new Label("followersTotalNoLabel",
                userService.countAllUsersByGivenFollowedChallenge(challenge));
        followersTotalNoLabel.setOutputMarkupId(true);
        item.add(followersTotalNoLabel);

        // challenge contributors
        Label contributors = new Label("contributors", activityService.countAllChallengeContributorsByCampaign(challenge));
        item.add(contributors.setOutputMarkupId(true));

        // challenge comments label
        Label challengeCommentLabel;
        int commentSize = 0;
        if (challenge.getComments() != null || !challenge.getComments().isEmpty()) {
            commentSize = challenge.getComments().size();
        }
        challengeCommentLabel = new Label("challengeCommentLabel", Model.of(commentSize));
        challengeCommentLabel.setOutputMarkupId(true);
        item.add(challengeCommentLabel);

        item.add(new Label("thumbsUpVotes", challenge.getNoOfUpVotes()));

        // challenge duedate label
        LocalDate dueDate = new LocalDate(getCorrespondingDateOfPhase(challenge));
        daysLeft = new Period(new LocalDate(), dueDate, PeriodType.days()).getDays();

        // this is to avoid "-x days" message
        if (daysLeft >= 0) {
            item.add(new Label("dueDate", daysLeft + " " + new StringResourceModel("days.to.go", this, null).getString() + " "
                    + new StringResourceModel(challenge.getInnovationStatus().getMessageKey(), this, null).getString()));
        } else { // if days are less than zero then change the message
            item.add(new Label("dueDate", challenge.getInnovationStatus().getMessageKey() + " is finished!"));
        }

        // Leader
        AjaxLink<Void> link = userImageLink("userProfileDetailLink", challenge.getCreatedBy());
        link.add(new NonCachingImage("leader_img", ProfilePictureResource.get(PictureType.THUMBNAIL, challenge.getCreatedBy())));
        item.add(link);
        item.add(new Label("leader_name", challenge.getCreatedBy().getNickName()));
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

    /**
     * @param challenge
     * @return
     */
    private Check<Campaign> newSelectionCheck(final Campaign challenge) {
        Check<Campaign> check = new Check<Campaign>("challengeCheck", new Model<>(challenge), challengeGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
    }

    public CheckGroup<Campaign> getChallengeGroup() {
        return challengeGroup;
    }

    /**
     * @author ATB
     */
    private final class ChallengeProvider extends EntityProvider<Campaign> {
        private static final long serialVersionUID = -1727094205049792307L;

        @Override
        public Iterator<? extends Campaign> iterator(long first, long count) {
            List<Campaign> challenges = new LinkedList<>();
            // based on status, call different methods
            if (processType.equals(TypeOfprocess.Challenge_Definition)) {
                // all challenges which are in definition phase
                challenges = campaignService.getAllCampaignsWithInnovationStatusByDescendingCreationDate(Long.valueOf(first)
                        .intValue(), Long.valueOf(count).intValue(), InnovationStatus.DEFINITION);
            } else if (processType.equals(TypeOfprocess.Challenge_Ideation)) {
                // all challenges which are in ideation phase
                challenges = campaignService.getAllCampaignsWithInnovationStatusByDescendingCreationDate(Long.valueOf(first)
                        .intValue(), Long.valueOf(count).intValue(), InnovationStatus.INCEPTION);
            } else if (processType.equals(TypeOfprocess.Challenge_Idea_Selection)) {
                // all challenges which are in selection phase
                challenges = campaignService.getAllCampaignsWithInnovationStatusByDescendingCreationDate(Long.valueOf(first)
                        .intValue(), Long.valueOf(count).intValue(), InnovationStatus.PRIORITISATION);
            } else if (processType.equals(TypeOfprocess.Challenge_Idea_Implementation)) {
                // all challenges which has finished selection phase
                challenges = campaignService.getAllCampaignsWithInnovationStatusByDescendingCreationDate(Long.valueOf(first)
                        .intValue(), Long.valueOf(count).intValue(), InnovationStatus.IMPLEMENTATION);
            } else if (processType.equals(TypeOfprocess.All)) {
                challenges = campaignService.getAll(Long.valueOf(first).intValue(), Long.valueOf(count).intValue());
            }
            return challenges.iterator();
        }

        @Override
        public long size() {
            if (processType.equals(TypeOfprocess.Challenge_Definition)) {
                // all challenges which are in definition phase
                long val = campaignService.countAllCampaignsWithInnovationStatus(InnovationStatus.DEFINITION);
                return val;
            } else if (processType.equals(TypeOfprocess.Challenge_Ideation)) {
                // all challenges which are in ideation phase
                return campaignService.countAllCampaignsWithInnovationStatus(InnovationStatus.INCEPTION);
            } else if (processType.equals(TypeOfprocess.Challenge_Idea_Selection)) {
                // all challenges which are in selection phase
                return campaignService.countAllCampaignsWithInnovationStatus(InnovationStatus.PRIORITISATION);
            } else if (processType.equals(TypeOfprocess.Challenge_Idea_Implementation)) {
                // all challenges which has finished selection phase
                return campaignService.countAllCampaignsWithInnovationStatus(InnovationStatus.IMPLEMENTATION);
            } else if (processType.equals(TypeOfprocess.All)) {
                return campaignService.countAll();
            }
            return 0;
        }

    }
}

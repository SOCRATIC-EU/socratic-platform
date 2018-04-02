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
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.IdeaService;
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
public class IdeaProcessListPanel extends GenericPanel<Idea> {

    /**
     *
     */
    private static final long serialVersionUID = -159659429992094182L;

    // inject the EJB for managing ideas
    // use @EJB annotation to inject EJBs to have proper proxying that works
    // with Wicket page store
    @EJB
    IdeaService ideaService;

    @EJB
    ActivityService activityService;

    @EJB
    UserService userService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many ideas do we show initially
    private static final int itemsPerPage = 10;

    // container holding the list of ideas
    private final WebMarkupContainer ideasContainer;

    // Repeating view showing the list of existing ideas
    private final DataView<Idea> ideasRepeater;

    private final EntityProvider<Idea> ideaProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private TypeOfprocess processType;

    private int daysLeft;

    private ProcessSortingCriteria processSortingCriteria;

    private CheckGroup<Idea> ideaGroup;
    private final Form<Idea> form;

    public IdeaProcessListPanel(final String id, final StyledFeedbackPanel feedbackPanel, TypeOfprocess typeOfprocess) {
        super(id);

        this.feedbackPanel = feedbackPanel;

        this.processType = typeOfprocess;

        form = new Form<Idea>("form");
        add(form);
        form.add(ideaGroup = newUserCheckGroup());

        // add container with list of existing ideas
        ideasContainer = new WebMarkupContainer("ideasContainer");
        ideaGroup.add(ideasContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("ideaGroupSelector", ideaGroup);
        ideasContainer.add(checkGroupSelector);

        // add repeating view with list of existing ideas
        ideaProvider = new IdeaProvider();

        ideasRepeater = new DataView<Idea>("ideasRepeater", ideaProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Idea> item) {
                item.setOutputMarkupId(true);
                IdeaProcessListPanel.this.populateItem(item, item.getModelObject());
            }
        };

        ideasContainer.add(ideasRepeater);

        ideaGroup.add(new BootstrapAjaxPagingNavigator("pagination", ideasRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(ideasRepeater.getPageCount() > 1);
            }
        });

    }

    /**
     * @return
     */
    private CheckGroup<Idea> newUserCheckGroup() {
        CheckGroup<Idea> checkGroup = new CheckGroup<Idea>("ideaGroup", new ArrayList<Idea>());
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
     * @param processType
     * @param target
     * @return
     */
    public IdeaProcessListPanel setProcessType(TypeOfprocess processType, AjaxRequestTarget target) {
        this.processType = processType;
        target.add(ideasContainer);
        target.add(ideaGroup);
        return this;
    }

    /**
     * @param processSortingCriteria
     * @return
     */
    public IdeaProcessListPanel setProcessSortingCriteria(ProcessSortingCriteria processSortingCriteria,
                                                          AjaxRequestTarget target) {
        this.processSortingCriteria = processSortingCriteria;
        target.add(ideasContainer);
        target.add(ideaGroup);
        return this;
    }

    /**
     * @return
     */
    public long getIdeaListSize() {
        return ideaProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item, final Idea idea) {

        item.setOutputMarkupId(true);
        item.add(newSelectionCheck(idea));
        item.add(new Label("createdOn", new PropertyModel<String>(idea, "postedAt")));
        item.add(new Label("name", new PropertyModel<String>(idea, "shortText")));

        // add follower label locally
        Label followersTotalNoLabel = new Label("followersTotalNoLabel", userService.countAllUsersByGivenFollowedIdea(idea));
        followersTotalNoLabel.setOutputMarkupId(true);
        item.add(followersTotalNoLabel);

        // idea contributors
        Label contributors = new Label("contributors", activityService.countAllContributorsByIdea(idea));
        item.add(contributors.setOutputMarkupId(true));

        // idea comments label
        Label ideaCommentLabel;
        int commentSize = 0;
        if (idea.getComments() != null || !idea.getComments().isEmpty()) {
            commentSize = idea.getComments().size();
        }
        ideaCommentLabel = new Label("ideaCommentLabel", Model.of(commentSize));
        ideaCommentLabel.setOutputMarkupId(true);
        item.add(ideaCommentLabel);

        item.add(new Label("thumbsUpVotes", idea.getNoOfUpVotes()));

        // idea duedate label
        LocalDate dueDate = new LocalDate(getCorrespondingDateOfPhase(idea.getCampaign()));
        daysLeft = new Period(new LocalDate(), dueDate, PeriodType.days()).getDays();

        // this is to avoid "-x days" message
        if (daysLeft >= 0) {
            item.add(new Label("dueDate", daysLeft + " " + new StringResourceModel("days.to.go", this, null).getString() + " "
                    + new StringResourceModel(idea.getCampaign().getInnovationStatus().getMessageKey(), this, null).getString()));
        } else { // if days are less than zero then change the message
            item.add(new Label("dueDate", idea.getCampaign().getInnovationStatus().getMessageKey() + " is finished!"));
        }

        // Leader
        AjaxLink<Void> link = userImageLink("userProfileDetailLink", idea.getPostedBy());
        link.add(new NonCachingImage("leader_img", ProfilePictureResource.get(PictureType.THUMBNAIL, idea.getPostedBy())));
        item.add(link);
        item.add(new Label("leader_name", idea.getPostedBy().getNickName()));
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
     * @param idea
     * @return
     */
    private Check<Idea> newSelectionCheck(final Idea idea) {
        Check<Idea> check = new Check<Idea>("ideaCheck", new Model<>(idea), ideaGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
    }

    public CheckGroup<Idea> getIdeaGroup() {
        return ideaGroup;
    }

    /**
     * @author ATB
     */
    private final class IdeaProvider extends EntityProvider<Idea> {
        private static final long serialVersionUID = -1727094205049792307L;

        @Override
        public Iterator<? extends Idea> iterator(long first, long count) {
            List<Idea> ideas = new LinkedList<>();
            // based on status, call different methods
            if (processType.equals(TypeOfprocess.Challenge_Idea) || processType.equals(TypeOfprocess.All)) {
                // all ideas
                ideas = ideaService.getAll(Long.valueOf(first).intValue(), Long.valueOf(count).intValue());
            }
            return ideas.iterator();
        }

        @Override
        public long size() {
            if (processType.equals(TypeOfprocess.Challenge_Idea) || processType.equals(TypeOfprocess.All)) {
                return ideaService.countAll();
            }
            return 0;
        }

    }
}

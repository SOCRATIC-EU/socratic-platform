package de.atb.socratic.web.dashboard.iLead.idea;

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

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.Activity;
import de.atb.socratic.model.ActivityType;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.IdeaActivity;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminParticipantsPage.ParticipantsSortingCriteria;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.selection.IdeaSelectionDetailsPage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

public abstract class AdminIdeaActivitiesListPanel extends GenericPanel<Idea> {
    private static final long serialVersionUID = -257930933985282429L;
    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;
    // how many participants do we show initially
    private static final int itemsPerPage = 6;

    // container holding the list of participants
    private final WebMarkupContainer participantsContainer;

    // Repeating view showing the list of existing participants
    private final DataView<Activity> activitiesRepeater;

    private final EntityProvider<Activity> activityProvider;

    private ParticipantsSortingCriteria sortingCriteria;

    @EJB
    ActivityService activityService;

    final Idea idea;

    public AdminIdeaActivitiesListPanel(final String id, final IModel<Idea> model, final StyledFeedbackPanel feedbackPanel) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        // get the campaigns participants
        idea = getModelObject();

        // add container with list of existing participants
        participantsContainer = new WebMarkupContainer("activitiesContainer");
        add(participantsContainer.setOutputMarkupId(true));

        // add repeating view with list of existing participants
        activityProvider = new ActivityProvider(idea);
        activitiesRepeater = new DataView<Activity>("activitiesRepeater", activityProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Activity> item) {
                item.setOutputMarkupId(true);
                AdminIdeaActivitiesListPanel.this.populateItem(item, item.getModelObject());
            }
        };
        participantsContainer.add(activitiesRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", activitiesRepeater) {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(activitiesRepeater.getPageCount() > 1);
            }
        });
    }

    protected void populateItem(final WebMarkupContainer item, final Activity activity) {
        item.setOutputMarkupId(true);
        item.add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.PROFILE,
                activity.getPerformedBy())));
        item.add(new Label("nickName", new PropertyModel<String>(activity.getPerformedBy(), "nickName")));

        if (activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_IDEA)) {
            item.add(new Label("commonText", new StringResourceModel("idea.newComment", this, null)));
            item.add(new BookmarkablePageLink<IdeaDetailsPage>("viewLink", IdeaDetailsPage.class, new PageParameters().set(
                    "id", idea.getCampaign().getId()).set("ideaId", idea.getId())));
        } else if (activity.getActivityType().equals(ActivityType.IDEA_LIKE)) {
            item.add(new Label("commonText", new StringResourceModel("idea.liked", this, null)));
            item.add(new BookmarkablePageLink<IdeaDetailsPage>("viewLink", IdeaDetailsPage.class, new PageParameters().set(
                    "id", idea.getCampaign().getId()).set("ideaId", idea.getId())));
        } else if (activity.getActivityType().equals(ActivityType.IDEA_VOTE)) {
            item.add(new Label("commonText", new StringResourceModel("idea.vote", this, null)));
            item.add(new BookmarkablePageLink<IdeaSelectionDetailsPage>("viewLink", IdeaSelectionDetailsPage.class,
                    new PageParameters().set("id", idea.getCampaign().getId()).set("ideaId", idea.getId())));
        }

        DateTime endDate = new DateTime(); // current date
        DateTime startDate = new DateTime(activity.getPerformedAt());
        Days days = Days.daysBetween(startDate, endDate);
        if (days.getDays() <= 1) {
            Hours hours = Hours.hoursBetween(startDate, endDate);
            if (hours.getHours() <= 0) {
                Minutes minutes = Minutes.minutesBetween(startDate, endDate);
                item.add(new Label("noOfDays", String.format(getString("activity.info.minutes"), minutes.getMinutes())));
            } else {
                item.add(new Label("noOfDays", String.format(getString("activity.info.hours"), hours.getHours())));
            }
        } else {
            item.add(new Label("noOfDays", String.format(getString("activity.info.days"), days.getDays())));
        }
    }

    /**
     * @author ATB
     */
    private final class ActivityProvider extends EntityProvider<Activity> {

        /**
         *
         */
        private static final long serialVersionUID = -1727094205049792307L;

        private final Idea idea;

        public ActivityProvider(Idea idea) {
            super();
            this.idea = idea;
        }

        @Override
        public Iterator<? extends Activity> iterator(long first, long count) {
            List<IdeaActivity> activities = null;
            activities = activityService.getAllIdeaActivitiesByDescendingCreationDateAndIdea(idea, Long.valueOf(first).intValue(), Long
                    .valueOf(count).intValue());
            return activities.iterator();
        }

        @Override
        public long size() {
            return activityService.countAllIdeaActivitiesByIdea(idea);
        }

    }
}

package de.atb.socratic.web.dashboard.coordinator.activities;

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
import de.atb.socratic.model.ActionActivity;
import de.atb.socratic.model.Activity;
import de.atb.socratic.model.ActivityType;
import de.atb.socratic.model.ChallengeActivity;
import de.atb.socratic.model.IdeaActivity;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.CommentService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.coordinator.activities.CoordinatorDashboardActivitiesPage.CreationDate;
import de.atb.socratic.web.dashboard.coordinator.activities.CoordinatorDashboardActivitiesPage.TypeOfActivity;
import de.atb.socratic.web.dashboard.coordinator.activities.CoordinatorDashboardActivitiesPage.TypeOfActivityProcess;
import de.atb.socratic.web.profile.UserProfileDetailsPage;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.commons.lang.time.DateUtils;
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
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ActivityListPanel extends GenericPanel<Activity> {

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

    @EJB
    ActionService actionService;

    @EJB
    CommentService commentService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    // how many activities do we show initially
    private static final int itemsPerPage = 10;

    // container holding the list of activities
    private final WebMarkupContainer activitiesContainer;

    // Repeating view showing the list of existing activities
    private final DataView<Activity> activitiesRepeater;

    private final EntityProvider<Activity> activityProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private TypeOfActivityProcess selectedActivityProcessType = TypeOfActivityProcess.All;
    private TypeOfActivity selectedActivityType = TypeOfActivity.all;
    private CreationDate creationDate = CreationDate.All_Time;

    private Date creationStartDate;
    private Date creationEndDate;

    private CheckGroup<Activity> activityGroup;
    private final Form<Activity> form;

    public ActivityListPanel(final String id, final StyledFeedbackPanel feedbackPanel) {
        super(id);

        this.feedbackPanel = feedbackPanel;

        form = new Form<Activity>("form");
        add(form);
        form.add(activityGroup = newUserCheckGroup());

        // add container with list of existing activities
        activitiesContainer = new WebMarkupContainer("activitiesContainer");
        activityGroup.add(activitiesContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("activitiesGroupSelector", activityGroup);
        activitiesContainer.add(checkGroupSelector);

        // add repeating view with list of existing activities
        activityProvider = new ActivityProvider();

        activitiesRepeater = new DataView<Activity>("activitiesRepeater", activityProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Activity> item) {
                item.setOutputMarkupId(true);
                ActivityListPanel.this.populateItem(item, item.getModelObject());
            }
        };

        activitiesContainer.add(activitiesRepeater);

        activityGroup.add(new BootstrapAjaxPagingNavigator("pagination", activitiesRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(activitiesRepeater.getPageCount() > 1);
            }
        });

    }

    public CheckGroup<Activity> getActivityGroup() {
        return activityGroup;
    }

    /**
     * @return
     */
    private CheckGroup<Activity> newUserCheckGroup() {
        CheckGroup<Activity> checkGroup = new CheckGroup<Activity>("activityGroup", new ArrayList<Activity>());
        checkGroup.add(new AjaxFormChoiceComponentUpdatingBehavior() {
            private static final long serialVersionUID = -8193184672687169923L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
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
    public ActivityListPanel setProcessType(TypeOfActivityProcess processType, AjaxRequestTarget target) {
        this.selectedActivityProcessType = processType;
        target.add(activitiesContainer);
        target.add(activityGroup);
        return this;
    }

    /**
     * @param activityType
     * @param target
     * @return
     */
    public ActivityListPanel setActivityType(TypeOfActivity activityType, AjaxRequestTarget target) {
        this.selectedActivityType = activityType;
        target.add(activitiesContainer);
        target.add(activityGroup);
        return this;
    }

    /**
     * @param creationDate
     * @param target
     * @return
     */
    public ActivityListPanel setActivityCreationDate(CreationDate creationDate, AjaxRequestTarget target) {
        this.creationDate = creationDate;
        target.add(activitiesContainer);
        target.add(activityGroup);
        return this;
    }

    /**
     * @param creationStartDate
     * @param target
     * @return
     */
    public ActivityListPanel setActivityCreationStartDate(Date creationStartDate, AjaxRequestTarget target) {
        this.creationStartDate = creationStartDate;
        target.add(activitiesContainer);
        target.add(activityGroup);
        return this;
    }

    /**
     * @param creationEndDate
     * @param target
     * @return
     */
    public ActivityListPanel setActivityCreationEndDate(Date creationEndDate, AjaxRequestTarget target) {
        this.creationEndDate = creationEndDate;
        target.add(activitiesContainer);
        target.add(activityGroup);
        return this;
    }

    /**
     * @return
     */
    public long getChallengeListSize() {
        return activityProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item, final Activity activity) {

        item.setOutputMarkupId(true);
        item.add(newSelectionCheck(activity));
        item.add(new Label("performedAt", new PropertyModel<String>(activity, "performedAt")));
        item.add(new Label("activityType", new StringResourceModel(activity.getActivityType().getNameKey(),
                ActivityListPanel.this, null).getString()));
        item.add(new Label("processName", getCorrospondingProcessName(activity)));

        // this will by only set when comment is posted as activity
        item.add(new Label("content", getContentFromActivity(activity)).setOutputMarkupId(true).setEscapeModelStrings(false));

        // Leader
        AjaxLink<Void> link = userImageLink("userProfileDetailLink", activity.getPerformedBy());
        link.add(new NonCachingImage("leader_img", ProfilePictureResource.get(PictureType.THUMBNAIL, activity.getPerformedBy())));
        item.add(link);
        item.add(new Label("leader_name", activity.getPerformedBy().getNickName()));
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
     * This method returns process name based on Activity. If Activity is related to challenge, challenge title would be return.
     * If Activity is related to idea, idea title would be return. Same for Action, Iteration and Business Model(currently it is
     * action title for BM)
     *
     * @param activity
     * @return
     */
    private String getCorrospondingProcessName(Activity activity) {

        if (activity.getActivityType().equals(ActivityType.ADD_IDEA_TO_CHALLENGE)
                || activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_CHALLENGE)
                || activity.getActivityType().equals(ActivityType.CHALLENGE_LIKE)
                || activity.getActivityType().equals(ActivityType.ADD_REFERENCE_TO_CHALLENGE)) {
            return ((ChallengeActivity) activity).getCampaign().getName();
        } else if (activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_IDEA)
                || activity.getActivityType().equals(ActivityType.IDEA_LIKE)
                || activity.getActivityType().equals(ActivityType.IDEA_VOTE)) {
            return ((IdeaActivity) activity).getIdea().getShortText();
        } else if (activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_ACTION)
                || activity.getActivityType().equals(ActivityType.ACTION_LIKE)
                || activity.getActivityType().equals(ActivityType.IDEA_VOTE)) {
            return ((ActionActivity) activity).getAction().getShortText();
        } else if (activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_ITERATION)
                || activity.getActivityType().equals(ActivityType.ITERATION_LIKE)) {
            return actionService.getLatestIterationOfAction(((ActionActivity) activity).getAction().getId()).getTitle();
        } else if (activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_BUSINESS_MODEL)
                || activity.getActivityType().equals(ActivityType.BUSINESS_MODEL_LIKE)) {
            // decide which entity of business model should be displayed as process text?
            return ((ActionActivity) activity).getAction().getShortText();
        }

        return null;
    }

    /**
     * This method will return content of comment as string.
     *
     * @param activity
     * @return
     */
    private String getContentFromActivity(Activity activity) {

        if (activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_CHALLENGE)
                || activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_IDEA)
                || activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_ACTION)
                || activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_ITERATION)
                || activity.getActivityType().equals(ActivityType.ADD_COMMENT_TO_BUSINESS_MODEL)) {

            return commentService.getById(activity.getCommentId()).getCommentText();
        }

        return null;
    }

    /**
     * @param activity
     * @return
     */
    private Check<Activity> newSelectionCheck(final Activity activity) {
        Check<Activity> check = new Check<Activity>("activityCheck", new Model<>(activity), activityGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
    }

    public CheckGroup<Activity> getChallengeGroup() {
        return activityGroup;
    }

    /**
     * this method returns list of ActivityType from selectedProcessType and selectedActivityType
     *
     * @return
     */
    public List<ActivityType> getActivityTypeFromSelectedParameters(TypeOfActivityProcess typeOfActivityProcess, TypeOfActivity typeOfActivity) {

        List<ActivityType> returnActivityTypes = new LinkedList<>();
        if (typeOfActivityProcess.equals(TypeOfActivityProcess.All)) {
            if (typeOfActivity.equals(TypeOfActivity.all)) {
                // if both parameters are set to all that means all of the activities should be listed
                for (ActivityType at : ActivityType.values()) {
                    returnActivityTypes.add(at);
                }
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.comments)) {
                // set return activity type to comments
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_CHALLENGE);
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_IDEA);
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_ACTION);
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_ITERATION);
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_BUSINESS_MODEL);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.likes)) {
                // set return activity type to likes
                returnActivityTypes.add(ActivityType.CHALLENGE_LIKE);
                returnActivityTypes.add(ActivityType.IDEA_LIKE);
                returnActivityTypes.add(ActivityType.ACTION_LIKE);
                returnActivityTypes.add(ActivityType.ITERATION_LIKE);
                returnActivityTypes.add(ActivityType.BUSINESS_MODEL_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.vote)) {
                // set return activity type to votes
                returnActivityTypes.add(ActivityType.IDEA_VOTE);
                return returnActivityTypes;
            }
        } else if (typeOfActivityProcess.equals(TypeOfActivityProcess.Challenge_Definition)) {
            if (typeOfActivity.equals(TypeOfActivity.all)) {
                // return all activities related to challenge definition phase
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_CHALLENGE);
                returnActivityTypes.add(ActivityType.CHALLENGE_LIKE);
                returnActivityTypes.add(ActivityType.ADD_REFERENCE_TO_CHALLENGE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.comments)) {
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_CHALLENGE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.likes)) {
                returnActivityTypes.add(ActivityType.CHALLENGE_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.vote)) {
                // this combination should not return anything thus empty list
                return new LinkedList<>();
            }
        } else if (typeOfActivityProcess.equals(TypeOfActivityProcess.Challenge_Idea)) {
            if (typeOfActivity.equals(TypeOfActivity.all)) {
                // return all activities related to challenge ideation phase and idea
                returnActivityTypes.add(ActivityType.ADD_IDEA_TO_CHALLENGE);
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_IDEA);
                returnActivityTypes.add(ActivityType.IDEA_LIKE);
                returnActivityTypes.add(ActivityType.IDEA_VOTE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.comments)) {
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_IDEA);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.likes)) {
                returnActivityTypes.add(ActivityType.IDEA_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.vote)) {
                returnActivityTypes.add(ActivityType.IDEA_VOTE);
                return returnActivityTypes;
            }
        } else if (typeOfActivityProcess.equals(TypeOfActivityProcess.Challenge_Idea_Selection)) {
            if (typeOfActivity.equals(TypeOfActivity.all)) {
                // return all activities related to challenge selection phase
                returnActivityTypes.add(ActivityType.IDEA_VOTE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.comments)) {
                // this combination should not return anything thus empty list
                return new LinkedList<>();
            } else if (typeOfActivity.equals(TypeOfActivity.likes)) {
                // this combination should not return anything thus empty list
                return new LinkedList<>();
            } else if (typeOfActivity.equals(TypeOfActivity.vote)) {
                returnActivityTypes.add(ActivityType.IDEA_VOTE);
                return returnActivityTypes;
            }
        } else if (typeOfActivityProcess.equals(TypeOfActivityProcess.Action_Solution)) {
            if (typeOfActivity.equals(TypeOfActivity.all)) {
                // return all activities related to action solution
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_ACTION);
                returnActivityTypes.add(ActivityType.ACTION_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.comments)) {
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_ACTION);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.likes)) {
                returnActivityTypes.add(ActivityType.ACTION_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.vote)) {
                // this combination should not return anything thus empty list
                return new LinkedList<>();
            }
        } else if (typeOfActivityProcess.equals(TypeOfActivityProcess.Action_Iteration)) {
            if (typeOfActivity.equals(TypeOfActivity.all)) {
                // return all activities related to action iteration
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_ITERATION);
                returnActivityTypes.add(ActivityType.ITERATION_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.comments)) {
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_ITERATION);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.likes)) {
                returnActivityTypes.add(ActivityType.ITERATION_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.vote)) {
                // this combination should not return anything thus empty list
                return new LinkedList<>();
            }
        } else if (typeOfActivityProcess.equals(TypeOfActivityProcess.Business_Model)) {
            if (typeOfActivity.equals(TypeOfActivity.all)) {
                // return all activities related to action BM
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_BUSINESS_MODEL);
                returnActivityTypes.add(ActivityType.BUSINESS_MODEL_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.comments)) {
                returnActivityTypes.add(ActivityType.ADD_COMMENT_TO_BUSINESS_MODEL);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.likes)) {
                returnActivityTypes.add(ActivityType.BUSINESS_MODEL_LIKE);
                return returnActivityTypes;
            } else if (typeOfActivity.equals(TypeOfActivity.vote)) {
                // this combination should not return anything thus empty list
                return new LinkedList<>();
            }
        }

        return new LinkedList<>();
    }

    /**
     * this method will return basically start date and end date for creation date filtering of activities First date in the
     * list(position 0) will be start date and second date will be end date.
     *
     * @return
     */
    public List<Date> getDatesBasedOnSelectedCreationDate(CreationDate dateType, Date startDate, Date endDate) {
        List<Date> returnsDates = new LinkedList<>();
        if (dateType.equals(CreationDate.All_Time)) {

            DateTime input = new DateTime();
            final DateMidnight startOfLastCentury = new DateMidnight(input.minusYears(100));

            // at position 0, start date would be placed
            returnsDates.add(startOfLastCentury.toDate());

            // at position 1, most current end date or higher date would be placed
            returnsDates.add(new Date());

        } else if (dateType.equals(CreationDate.Last_Week)) {
            DateTime input = new DateTime();
            final DateMidnight startOfLastWeek =
                    new DateMidnight(input.minusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY));
            // at position 0, start date would be placed
            returnsDates.add(startOfLastWeek.toDate());


            // at position 1, most current end date or higher date would be placed
            final DateMidnight endOfLastWeek = startOfLastWeek.plusWeeks(1);
            returnsDates.add(endOfLastWeek.toDate());
        } else if (dateType.equals(CreationDate.Last_Month)) {
            DateTime input = new DateTime();
            final DateMidnight startOfLastMonth =
                    new DateMidnight(input.minusMonths(1).withDayOfMonth(1));

            // at position 0, start date would be placed
            returnsDates.add(startOfLastMonth.toDate());

            // at position 1, most current end date or higher date would be placed
            final DateMidnight endOfLastMonth = startOfLastMonth.plusMonths(1);
            returnsDates.add(endOfLastMonth.toDate());
        } else if (dateType.equals(CreationDate.Last_Year)) {
            DateTime input = new DateTime();
            final DateMidnight startOfLastYear =
                    new DateMidnight(input.minusYears(1).withDayOfYear(1));
            // at position 0, start date would be placed
            returnsDates.add(startOfLastYear.toDate());

            // at position 1, most current end date or higher date would be placed
            final DateMidnight endOfLastYear = startOfLastYear.plusYears(1);
            returnsDates.add(endOfLastYear.toDate());
        } else if (dateType.equals(CreationDate.Between_Dates)) {

            // at position 0, start date would be placed
            returnsDates.add(startDate);

            // at position 1, most current end date or higher date would be placed
            returnsDates.add(DateUtils.addDays(endDate, 1));
        }

        return returnsDates;
    }

    /**
     * @author ATB
     */
    private final class ActivityProvider extends EntityProvider<Activity> {
        private static final long serialVersionUID = -1727094205049792307L;

        @Override
        public Iterator<? extends Activity> iterator(long first, long count) {
            List<Activity> activities = new LinkedList<>();
            List<Date> dates = getDatesBasedOnSelectedCreationDate(creationDate, creationStartDate, creationEndDate);
            activities = activityService.getAllActivitiesByActivityType(
                    getActivityTypeFromSelectedParameters(selectedActivityProcessType, selectedActivityType), dates.get(0),
                    dates.get(1), Long.valueOf(first).toString(), Long.valueOf(count).toString());
            return activities.iterator();
        }

        @Override
        public long size() {
            List<Date> dates = getDatesBasedOnSelectedCreationDate(creationDate, creationStartDate, creationEndDate);
            return activityService.countAllActivitiesByActivityType(
                    getActivityTypeFromSelectedParameters(selectedActivityProcessType, selectedActivityType), dates.get(0),
                    dates.get(1));
        }
    }
}

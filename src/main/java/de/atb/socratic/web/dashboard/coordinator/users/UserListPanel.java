package de.atb.socratic.web.dashboard.coordinator.users;

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
import de.atb.socratic.web.dashboard.coordinator.activities.CoordinatorDashboardActivitiesPage.TypeOfActivityProcess;
import de.atb.socratic.web.dashboard.coordinator.users.CoordinatorDashboardUsersPage.UserType;
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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class UserListPanel extends GenericPanel<User> {

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
    private final WebMarkupContainer usersContainer;

    // Repeating view showing the list of existing users
    private final DataView<User> usersRepeater;

    private final EntityProvider<User> userProvider;

    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    private TypeOfActivityProcess selectedActivityProcessType = TypeOfActivityProcess.All;

    private CreationDate registrationDateType = CreationDate.All_Time;
    private UserType selectedUserType = UserType.All;

    private Date registrationStartDate;
    private Date registrationEndDate;

    private CheckGroup<User> userGroup;
    private final Form<User> form;

    public UserListPanel(final String id, final StyledFeedbackPanel feedbackPanel) {
        super(id);

        this.feedbackPanel = feedbackPanel;

        form = new Form<User>("form");
        add(form);
        form.add(userGroup = newUserCheckGroup());

        // add container with list of existing users
        usersContainer = new WebMarkupContainer("usersContainer");
        userGroup.add(usersContainer.setOutputMarkupId(true));

        CheckGroupSelector checkGroupSelector = new CheckGroupSelector("usersGroupSelector", userGroup);
        usersContainer.add(checkGroupSelector);

        // add repeating view with list of existing users
        userProvider = new UserProvider();

        usersRepeater = new DataView<User>("usersRepeater", userProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<User> item) {
                item.setOutputMarkupId(true);
                UserListPanel.this.populateItem(item, item.getModelObject());
            }
        };
        usersContainer.add(usersRepeater);

        userGroup.add(new BootstrapAjaxPagingNavigator("pagination", usersRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(usersRepeater.getPageCount() > 1);
            }
        });

    }

    public CheckGroup<User> getUserGroup() {
        return userGroup;
    }

    /**
     * this method will return basically start date and end date for creation date filtering of users First date in the
     * list(position 0) will be start date and second date will be end date.
     *
     * @return
     */
    public List<Date> getDatesBasedOnSelectedCreationDate(CreationDate dateType, Date startDate, Date endDate) {
        List<Date> returnsDates = new LinkedList<>();
        if (dateType != null && dateType.equals(CreationDate.All_Time)) {

            DateTime input = new DateTime();
            final DateMidnight startOfLastCentury = new DateMidnight(input.minusYears(100));

            // at position 0, start date would be placed
            returnsDates.add(startOfLastCentury.toDate());

            // at position 1, most current end date or higher date would be placed
            returnsDates.add(new Date());

        } else if (dateType != null && dateType.equals(CreationDate.Last_Week)) {
            DateTime input = new DateTime();
            final DateMidnight startOfLastWeek =
                    new DateMidnight(input.minusWeeks(1).withDayOfWeek(DateTimeConstants.MONDAY));
            // at position 0, start date would be placed
            returnsDates.add(startOfLastWeek.toDate());


            // at position 1, most current end date or higher date would be placed
            final DateMidnight endOfLastWeek = startOfLastWeek.plusWeeks(1);
            returnsDates.add(endOfLastWeek.toDate());
        } else if (dateType != null && dateType.equals(CreationDate.Last_Month)) {
            DateTime input = new DateTime();
            final DateMidnight startOfLastMonth =
                    new DateMidnight(input.minusMonths(1).withDayOfMonth(1));

            // at position 0, start date would be placed
            returnsDates.add(startOfLastMonth.toDate());

            // at position 1, most current end date or higher date would be placed
            final DateMidnight endOfLastMonth = startOfLastMonth.plusMonths(1);
            returnsDates.add(endOfLastMonth.toDate());
        } else if (dateType != null && dateType.equals(CreationDate.Last_Year)) {
            DateTime input = new DateTime();
            final DateMidnight startOfLastYear =
                    new DateMidnight(input.minusYears(1).withDayOfYear(1));
            // at position 0, start date would be placed
            returnsDates.add(startOfLastYear.toDate());

            // at position 1, most current end date or higher date would be placed
            final DateMidnight endOfLastYear = startOfLastYear.plusYears(1);
            returnsDates.add(endOfLastYear.toDate());
        } else if (dateType != null && dateType.equals(CreationDate.Between_Dates)) {

            // at position 0, start date would be placed
            returnsDates.add(startDate);

            // at position 1, most current end date or higher date would be placed
            returnsDates.add(DateUtils.addDays(endDate, 1));
        } else {    // if dateType is null then set by default to all time
            DateTime input = new DateTime();
            final DateMidnight startOfLastCentury = new DateMidnight(input.minusYears(100));

            // at position 0, start date would be placed
            returnsDates.add(startOfLastCentury.toDate());

            // at position 1, most current end date or higher date would be placed
            returnsDates.add(new Date());
        }

        return returnsDates;
    }

    /**
     * @return
     */
    private CheckGroup<User> newUserCheckGroup() {
        CheckGroup<User> checkGroup = new CheckGroup<User>("userGroup", new ArrayList<User>());
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
     * @param userType
     * @param target
     * @return
     */
    public UserListPanel setUserType(UserType userType, AjaxRequestTarget target) {
        this.selectedUserType = userType;
        target.add(usersContainer);
        target.add(userGroup);
        return this;
    }

    /**
     * @param registrationDateType
     * @param target
     * @return
     */
    public UserListPanel setUserRegistrationDateType(CreationDate registrationDateType, AjaxRequestTarget target) {
        this.registrationDateType = registrationDateType;
        target.add(usersContainer);
        target.add(userGroup);
        return this;
    }

    /**
     * @param registrationStartDate
     * @param target
     * @return
     */
    public UserListPanel setUserRegistrationStartDate(Date registrationStartDate, AjaxRequestTarget target) {
        this.registrationStartDate = registrationStartDate;
        target.add(usersContainer);
        target.add(userGroup);
        return this;
    }

    /**
     * @param registrationEndDate
     * @param target
     * @return
     */
    public UserListPanel setUserRegistrationEndDate(Date registrationEndDate, AjaxRequestTarget target) {
        this.registrationEndDate = registrationEndDate;
        target.add(usersContainer);
        target.add(userGroup);
        return this;
    }

    /**
     * @return
     */
    public long getChallengeListSize() {
        return userProvider.size();
    }

    protected void populateItem(final WebMarkupContainer item, final User user) {

        item.setOutputMarkupId(true);
        item.add(newSelectionCheck(user));
        // registration date
        item.add(new Label("registrationDate", new PropertyModel<String>(user, "registrationDate")));
        // user
        AjaxLink<Void> link = userImageLink("userProfileDetailLink", user);
        link.add(new NonCachingImage("user_img", ProfilePictureResource.get(PictureType.THUMBNAIL, user)));
        item.add(link);
        item.add(new Label("user_name", user.getNickName()));

        // ideas?? submitted claims??
        int totalNoOfLeadsByUser = user.getNoOfCampaignsLeads() + user.getNoOfIdeasLeads() + user.getNoOfActionsLeads();
        item.add(new Label("processesLeadedLabel", totalNoOfLeadsByUser));
        item.add(new Label("commentsLabel", user.getNoOfCommentsPosts()));
        item.add(new Label("likesGivenLabel", user.getNoOfLikesGiven()));
        item.add(new Label("likesReceivedLabel", user.getNoOfLikesReceived()));
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
     * @param user
     * @return
     */
    private Check<User> newSelectionCheck(final User user) {
        Check<User> check = new Check<User>("userCheck", new Model<>(user), userGroup) {

            private static final long serialVersionUID = -5802376197291247523L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return check;
    }

    public CheckGroup<User> getChallengeGroup() {
        return userGroup;
    }

    /**
     * @author ATB
     */
    private final class UserProvider extends EntityProvider<User> {
        private static final long serialVersionUID = -1727094205049792307L;

        @Override
        public Iterator<? extends User> iterator(long first, long count) {
            List<User> users = new LinkedList<>();
            List<Date> dates = getDatesBasedOnSelectedCreationDate(registrationDateType,
                    registrationStartDate, registrationEndDate);
            if (selectedUserType != null && selectedUserType.equals(UserType.No_Activity)) {
                users = userService.getAllUsersWithNoActionYetForAllProcesses(Long.valueOf(first).toString(), Long
                        .valueOf(count).toString(), dates.get(0), dates.get(1));
            } else if (selectedUserType != null && selectedUserType.equals(UserType.Followers)) {
                users = userService.getAllUsersFollowingAllProcesses(Long.valueOf(first).toString(), Long.valueOf(count)
                        .toString(), dates.get(0), dates.get(1));
            } else if (selectedUserType != null && selectedUserType.equals(UserType.Contributors)) {
                users = activityService.getAllContributorsFromAllProcesses(Long.valueOf(first).toString(),
                        Long.valueOf(count).toString(), dates.get(0), dates.get(1));
            } else if (selectedUserType != null && selectedUserType.equals(UserType.Leaders)) {
                users = userService.getAllUsersLeadingAllProcesses(Long.valueOf(first).toString(), Long.valueOf(count)
                        .toString(), dates.get(0), dates.get(1));
            } else {
                users = userService.getAllUsersByRegisteredDate(Long.valueOf(first).toString(), Long.valueOf(count)
                        .toString(), dates.get(0), dates.get(1));
            }

            return users.iterator();
        }

        @Override
        public long size() {
            List<Date> dates = getDatesBasedOnSelectedCreationDate(registrationDateType,
                    registrationStartDate, registrationEndDate);
            if (selectedUserType != null && selectedUserType.equals(UserType.No_Activity)) {
                return userService.countAllUsersWithNoActionYetForAllProcesses(dates.get(0), dates.get(1));
            } else if (selectedUserType != null && selectedUserType.equals(UserType.Followers)) {
                return userService.countAllUsersFollowingAllProcesses(dates.get(0), dates.get(1));
            } else if (selectedUserType != null && selectedUserType.equals(UserType.Contributors)) {
                return activityService.countAllContributorsFromAllProcesses(dates.get(0), dates.get(1));
            } else if (selectedUserType != null && selectedUserType.equals(UserType.Leaders)) {
                return userService.countAllUsersLeadingAllProcesses(dates.get(0), dates.get(1));
            } else {
                return userService.countAllUsersByRegisteredDate(dates.get(0), dates.get(1));
            }

        }
    }
}

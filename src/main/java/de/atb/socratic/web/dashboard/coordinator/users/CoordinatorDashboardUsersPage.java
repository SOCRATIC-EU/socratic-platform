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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;

import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextField;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.form.DateTextFieldConfig;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.dashboard.coordinator.DashboardCommonHeaderPage;
import de.atb.socratic.web.dashboard.coordinator.activities.CoordinatorDashboardActivitiesPage.CreationDate;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import org.apache.commons.lang.time.DateUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.joda.time.DateTime;

/**
 * @author ATB
 */
public class CoordinatorDashboardUsersPage extends DashboardCommonHeaderPage {

    private static final long serialVersionUID = -7085939814517735639L;

    @EJB
    private UserService userService;

    @EJB
    private ActivityService activityService;

    private final Form<Void> form;
    private final RadioGroup<UserType> listOfUserTypes;
    private final RadioGroup<CreationDate> listOfRegistrationDates;

    private DateTextField registrationStartDateField;
    private DateTextField registrationEndDateField;

    private ModalWindow createMessageModalWindow;
    private CreateMessagePanel messageModelPanel;
    private final DropDownChoice<UserSortingType> messageUserSortingCriteria;
    private UserListPanel usersListPanel;

    private AjaxSubmitLink filterLink;

    private UserType selectedUserType = UserType.All;
    private CreationDate selectedRegistrationDate = CreationDate.All_Time;

    static final String Session_Filter_StartDate = CoordinatorDashboardUsersPage.class.getSimpleName() + ".filter.date.start";
    static final String Session_Filter_EndDate = CoordinatorDashboardUsersPage.class.getSimpleName() + ".filter.date.end";

    public CoordinatorDashboardUsersPage(PageParameters parameters) {
        super(parameters);

        form = new InputValidationForm<>("form");
        form.setOutputMarkupId(true);
        add(form);

        // Add user filtering: type of users
        form.add(listOfUserTypes = getUserTypeRadioGroup());
        listOfUserTypes.setRenderBodyOnly(false);

        // Add user filtering: Registration Date
        form.add(listOfRegistrationDates = getRegistrationDateRadioGroup());
        listOfRegistrationDates.setRenderBodyOnly(false);

        // add activity creation's between dates...
        addActvitiyCreationDates();

        // messages tab
        form.add(createMessageButton());
        form.add(createMessageModelWindow());

        form.add(messageUserSortingCriteria = newMessageUsersSortingCriteriaDropDownChoice());

        // Activities list
        form.add(usersListPanel = getUsersListPanel());

        form.add(filterLink = newSubmitLink("filterLink"));
        filterLink.setOutputMarkupId(true);
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "usersTab");
    }

    private UserListPanel getUsersListPanel() {
        UserListPanel usersListPanel = new UserListPanel("listOfUsers", feedbackPanel) {
            private static final long serialVersionUID = -7946672996240192916L;
        };
        usersListPanel.setOutputMarkupId(true);
        return usersListPanel;
    }

    public void addActvitiyCreationDates() {
        // start and end dates
        form.add(registrationStartDateField = newDateTextField("registrationStartDateField", new Date()));
        form.add(registrationEndDateField = newDateTextField("registrationEndDateField", new Date()));

        OnChangeAjaxBehavior onStartDateChangeAjaxBehavior = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(registrationEndDateField);
                // set listOfActivityCreationDates to between dates once start date is set
                listOfRegistrationDates.setModelObject(CreationDate.Between_Dates);
                target.add(listOfRegistrationDates);

                target.add(usersListPanel.setUserRegistrationDateType(selectedRegistrationDate, target));
                target.add(usersListPanel.setUserRegistrationStartDate(registrationStartDateField.getModelObject(), target));
                target.add(usersListPanel.setUserRegistrationEndDate(registrationEndDateField.getModelObject(), target));
            }
        };
        registrationStartDateField.add(onStartDateChangeAjaxBehavior);

        OnChangeAjaxBehavior onEndDateChangeAjaxBehavior = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // set listOfActivityCreationDates to between dates once end date is set
                listOfRegistrationDates.setModelObject(CreationDate.Between_Dates);
                target.add(listOfRegistrationDates);

                target.add(usersListPanel.setUserRegistrationDateType(selectedRegistrationDate, target));
                target.add(usersListPanel.setUserRegistrationStartDate(registrationStartDateField.getModelObject(), target));
                target.add(usersListPanel.setUserRegistrationEndDate(registrationEndDateField.getModelObject(), target));
            }
        };
        registrationEndDateField.add(onEndDateChangeAjaxBehavior);
    }

    /**
     * @param id
     * @param initialDate
     * @return
     */
    private DateTextField newDateTextField(final String id, Date initialDate) {
        final DateTextFieldConfig config;
        // activity can not be in future thus, future start and end dates are disabled
        config = new DateTextFieldConfig().withFormat("dd.MM.yyyy").allowKeyboardNavigation(true).autoClose(true)
                .highlightToday(true).showTodayButton(false).withEndDate(new DateTime());
        DateTextField dateTextField = new DateTextField(id, Model.of(initialDate), config) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            protected void onConfigure() {
                // condition for end date to be reset and all earlier dates after start dates are disabled for end date.
                if (registrationStartDateField.getModelObject() != null && id.equals(registrationEndDateField.getId())) {
                    config.withStartDate(new DateTime(DateUtils.addDays(registrationStartDateField.getModelObject(), 0)))
                            .showTodayButton(true);
                }
            }
        };
        return dateTextField;

    }

    /**
     * @param target
     */
    private void updateComponents(AjaxRequestTarget target) {
        selectedRegistrationDate = listOfRegistrationDates.getModelObject();
        selectedUserType = listOfUserTypes.getModelObject();

        // update activities list
        target.add(this.usersListPanel.setUserType(listOfUserTypes.getModelObject(), target));
        target.add(this.usersListPanel.setUserRegistrationDateType(listOfRegistrationDates.getModelObject(), target));
        target.add(this.usersListPanel.setUserRegistrationStartDate(registrationStartDateField.getModelObject(), target));
        target.add(this.usersListPanel.setUserRegistrationEndDate(registrationEndDateField.getModelObject(), target));

        target.add(usersListPanel);
        target.add(listOfRegistrationDates);
        target.add(listOfUserTypes);
        target.add(registrationStartDateField);
        target.add(registrationEndDateField);
        target.add(form);
    }

    private RadioGroup<CreationDate> getRegistrationDateRadioGroup() {
        // RegistrationDate
        final RadioGroup<CreationDate> listOfRegistrationDate = new RadioGroup<CreationDate>("typeOfRegistrationDates",
                new PropertyModel<CreationDate>(this, "selectedRegistrationDate"));
        listOfRegistrationDate.setOutputMarkupId(true);
        listOfRegistrationDate.setRenderBodyOnly(false);
        listOfRegistrationDate.add(new Radio<>("allTime", Model.of(CreationDate.All_Time)));
        listOfRegistrationDate.add(new Radio<>("lastWeek", Model.of(CreationDate.Last_Week)));
        listOfRegistrationDate.add(new Radio<>("lastMonth", Model.of(CreationDate.Last_Month)));
        listOfRegistrationDate.add(new Radio<>("lastYear", Model.of(CreationDate.Last_Year)));
        listOfRegistrationDate.add(new Radio<>("betweenDates", Model.of(CreationDate.Between_Dates)));
        return listOfRegistrationDate;
    }

    private RadioGroup<UserType> getUserTypeRadioGroup() {
        // UserType
        final RadioGroup<UserType> listOfSelectedUserType = new RadioGroup<UserType>("userTypeRadioGroup",
                new PropertyModel<UserType>(this, "selectedUserType"));
        listOfSelectedUserType.setOutputMarkupId(true);

        listOfSelectedUserType.add(new Radio<>("all", Model.of(UserType.All)));
        listOfSelectedUserType.add(new Radio<>("noActivity", Model.of(UserType.No_Activity)));
        listOfSelectedUserType.add(new Radio<>("followers", Model.of(UserType.Followers)));
        listOfSelectedUserType.add(new Radio<>("contributors", Model.of(UserType.Contributors)));
        listOfSelectedUserType.add(new Radio<>("leaders", Model.of(UserType.Leaders)));
        return listOfSelectedUserType;
    }

    /**
     * @param
     * @return
     */
    private AjaxSubmitLink createMessageButton() {
        AjaxSubmitLink ajaxSubmitLink = new AjaxSubmitLink("createMessage") {

            private static final long serialVersionUID = 7675565566981782898L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(createMessageModalWindow);

                // get users
                Collection<User> recipients = getSelectedUsersForProcess();

                // pass non repeated users to be displayed in recipients field.
                Set<User> noRepeatedUsers = new HashSet<>(recipients);
                messageModelPanel = new CreateMessagePanel(createMessageModalWindow.getContentId(), createMessageModalWindow,
                        noRepeatedUsers) {
                    private static final long serialVersionUID = 1L;
                };

                createMessageModalWindow.setContent(messageModelPanel.setOutputMarkupId(true));
                createMessageModalWindow.show(target);

            }
        };
        return ajaxSubmitLink;
    }

    public List<User> getSelectedUsersForProcess() {
        List<User> users = new LinkedList<>();

        // at first, find out which users are selected
        if (messageUserSortingCriteria.getModelObject().equals(UserSortingType.All_Users)) {
            List<Date> dates = usersListPanel.getDatesBasedOnSelectedCreationDate(listOfRegistrationDates.getModelObject(),
                    registrationStartDateField.getModelObject(), registrationEndDateField.getModelObject());

            if (listOfUserTypes != null && listOfUserTypes.getModelObject().equals(UserType.No_Activity)) {
                // find all users without any activity: comment, follow or likes
                users = userService.getAllUsersWithNoActionYetForAllProcesses(null, null, dates.get(0), dates.get(1));
            } else if (listOfUserTypes != null && listOfUserTypes.getModelObject().equals(UserType.Followers)) {
                // find all users who follows either challenges, ideas or actions                
                users = userService.getAllUsersFollowingAllProcesses(null, null, dates.get(0), dates.get(1));
            } else if (listOfUserTypes != null && listOfUserTypes.getModelObject().equals(UserType.Contributors)) {
                // find all users who contributes to either challenges, idea or actions
                users = activityService.getAllContributorsFromAllProcesses(null, null, dates.get(0), dates.get(1));
            } else if (listOfUserTypes != null && listOfUserTypes.getModelObject().equals(UserType.Leaders)) {
                // find all users who leads either challenges, idea or actions                
                users = userService.getAllUsersLeadingAllProcesses(null, null, dates.get(0), dates.get(1));
            } else {
                // if non of above is selected then find all users... 
                users = userService.getAllUsersByRegisteredDate(null, null, dates.get(0), dates.get(1));
            }

        } else if (messageUserSortingCriteria.getModelObject().equals(UserSortingType.Selected_Users)) {
            users = (List<User>) usersListPanel.getUserGroup().getModelObject();
        }

        return users;
    }

    /**
     * @param
     * @return
     */
    private ModalWindow createMessageModelWindow() {
        createMessageModalWindow = new ModalWindow("createMessageModalWindow") {

            private static final long serialVersionUID = -6118683848343086655L;

            @Override
            public void show(AjaxRequestTarget target) {
                super.show(target);
                target.appendJavaScript(""//
                        + "var thisWindow = Wicket.Window.get();\n"
                        + "if (thisWindow) {\n"
                        + "thisWindow.window.style.width = \"1500px\";\n"
                        + "thisWindow.content.style.height = \"1000px\";\n"
                        + "thisWindow.center();\n" + "}");
                setOutputMarkupId(true);
            }
        };

        // createMessageModalWindow.setTitle("Compose Message");
        createMessageModalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {

            private static final long serialVersionUID = -9143847141081283640L;

            @Override
            public boolean onCloseButtonClicked(AjaxRequestTarget target) {
                return true;
            }
        }).setOutputMarkupId(true);

        createMessageModalWindow.setResizable(true);
        createMessageModalWindow.setAutoSize(true);
        createMessageModalWindow.setInitialWidth(1400);
        createMessageModalWindow.setInitialHeight(1400);

        return createMessageModalWindow;
    }

    /**
     * @return
     */
    private DropDownChoice<UserSortingType> newMessageUsersSortingCriteriaDropDownChoice() {
        List<UserSortingType> listOfSorrtingCriteria = Arrays.asList(UserSortingType.values());
        final DropDownChoice<UserSortingType> sortingCriteriaChoice = new DropDownChoice<UserSortingType>(
                "messageUserSortingChoice", new Model<UserSortingType>(), listOfSorrtingCriteria,
                new IChoiceRenderer<UserSortingType>() {
                    private static final long serialVersionUID = -4910707158069588234L;

                    @Override
                    public Object getDisplayValue(UserSortingType object) {
                        return new StringResourceModel(object.getNameKey(), CoordinatorDashboardUsersPage.this, null).getString();
                    }

                    @Override
                    public String getIdValue(UserSortingType object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(UserSortingType.All_Users);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = -2840672822107134374L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                target.add(messageUserSortingCriteria);
            }
        });
        return sortingCriteriaChoice;
    }

    /**
     * @return
     */
    private AjaxSubmitLink newSubmitLink(String id) {
        return new AjaxSubmitLink(id) {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                this.add(new AttributeModifier("value", new StringResourceModel("typeOfUser.filter.button", this, null)
                        .getString()));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                // change
                updateComponents(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(usersListPanel);
            }
        };
    }

    public enum UserType {

        All("userType.all"),
        No_Activity("userType.noActivity"),
        Followers("userType.followers"),
        Contributors("userType.contributors"),
        Leaders("userType.leaders");

        private final String nameKey;

        UserType(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }

    public enum UserSortingType {

        All_Users("sort.allUsers"),
        Selected_Users("sort.selectedUsers");

        private final String nameKey;

        UserSortingType(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }
}

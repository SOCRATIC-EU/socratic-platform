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
import de.atb.socratic.model.Activity;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.dashboard.coordinator.DashboardCommonHeaderPage;
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
public class CoordinatorDashboardActivitiesPage extends DashboardCommonHeaderPage {
    private static final long serialVersionUID = -7085939814517735639L;

    private ModalWindow createMessageModalWindow;
    private CreateMessagePanel messageModelPanel;
    private final DropDownChoice<ActivityMessageProcessSortingCriteria> messageActivitySortingCriteria;
    private final DropDownChoice<ActivityMessageUserSortingCriteria> messageUserSortingCriteria;

    private final Form<Void> form;
    private final DropDownChoice<TypeOfActivityProcess> listOfTypesOfActivityProcesses;
    private final DropDownChoice<TypeOfActivity> listOfTypesOfActivities;
    private final RadioGroup<CreationDate> listOfActivityCreationDates;

    private CreationDate selectedActivityCreationDate = CreationDate.All_Time;
    private ActivityListPanel activityListPanel;

    private AjaxSubmitLink filterLink;

    private DateTextField activityCreationStartDateField;
    private DateTextField activityCreationEndDateField;

    @EJB
    ActivityService activityService;

    public CoordinatorDashboardActivitiesPage(PageParameters parameters) {
        super(parameters);

        form = new InputValidationForm<>("form");
        form.setOutputMarkupId(true);
        add(form);

        // Add activity filtering dropdown lists
        form.add(listOfTypesOfActivityProcesses = getProcessTypeDropDownList());
        form.add(listOfTypesOfActivities = getActivityTypeDropDownList());
        // Add activity filtering creation dates radio groups
        form.add(listOfActivityCreationDates = getActivityCreationDateRadioGroup());
        listOfActivityCreationDates.setRenderBodyOnly(false);

        // add activity creation's between dates...
        addActvitiyCreationDates();

        // messages tab
        form.add(createMessageButton());
        form.add(createMessageModelWindow());
        form.add(messageActivitySortingCriteria = newMessageProcessSortingCriteriaDropDownChoice());
        form.add(messageUserSortingCriteria = newMessageUsersSortingCriteriaDropDownChoice());

        // Activities list
        form.add(activityListPanel = getActionsListPanel());

        // private ChallengeProcessListPanel challengeProcessListPanel;
        form.add(filterLink = newSubmitLink("filterLink"));
        filterLink.setOutputMarkupId(true);
    }

    private ActivityListPanel getActionsListPanel() {
        ActivityListPanel actionProcessListPanel = new ActivityListPanel("listOfActivities", feedbackPanel) {
            private static final long serialVersionUID = -7946672996240192916L;
        };
        actionProcessListPanel.setOutputMarkupId(true);
        return actionProcessListPanel;
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
                this.add(new AttributeModifier("value", new StringResourceModel("typeOfActivity.filter.button", this, null)
                        .getString()));
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                // change
                updateComponents(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(activityListPanel);
            }
        };
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "activitiesTab");
    }

    /**
     * @param target
     */
    private void updateComponents(AjaxRequestTarget target) {
        selectedActivityCreationDate = listOfActivityCreationDates.getModelObject();

        // update activities list
        target.add(this.activityListPanel.setProcessType(listOfTypesOfActivityProcesses.getModelObject(), target));
        target.add(this.activityListPanel.setActivityType(listOfTypesOfActivities.getModelObject(), target));
        target.add(this.activityListPanel.setActivityCreationDate(selectedActivityCreationDate, target));
        target
                .add(this.activityListPanel.setActivityCreationStartDate(activityCreationStartDateField.getModelObject(), target));
        target.add(this.activityListPanel.setActivityCreationEndDate(activityCreationEndDateField.getModelObject(), target));

        target.add(activityListPanel);
        target.add(listOfActivityCreationDates);
        target.add(activityCreationStartDateField);
        target.add(activityCreationEndDateField);
        target.add(listOfTypesOfActivityProcesses);
        target.add(listOfTypesOfActivities);
        target.add(form);
    }

    /**
     * @return
     */
    private DropDownChoice<TypeOfActivityProcess> getProcessTypeDropDownList() {
        final DropDownChoice<TypeOfActivityProcess> processesCriteriaChoice = new DropDownChoice<>("processesCriteriaChoice",
                new Model<TypeOfActivityProcess>(), Arrays.asList(TypeOfActivityProcess.values()),
                new IChoiceRenderer<TypeOfActivityProcess>() {
                    private static final long serialVersionUID = 7632151907039246612L;

                    @Override
                    public Object getDisplayValue(TypeOfActivityProcess object) {
                        return new StringResourceModel(object.getNameKey(), CoordinatorDashboardActivitiesPage.this, null)
                                .getString();
                    }

                    @Override
                    public String getIdValue(TypeOfActivityProcess object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        processesCriteriaChoice.setNullValid(false);
        processesCriteriaChoice.setDefaultModelObject(TypeOfActivityProcess.All);
        processesCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 4630143654574571697L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {

            }
        });
        return processesCriteriaChoice;

    }

    /**
     * @return
     */
    private DropDownChoice<TypeOfActivity> getActivityTypeDropDownList() {
        final DropDownChoice<TypeOfActivity> processesCriteriaChoice = new DropDownChoice<>("activityCriteriaChoice",
                new Model<TypeOfActivity>(), Arrays.asList(TypeOfActivity.values()));

        // always include a "Please choose" option
        processesCriteriaChoice.setNullValid(false);
        processesCriteriaChoice.setDefaultModelObject(TypeOfActivity.all);
        processesCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 4630143654574571697L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        return processesCriteriaChoice;

    }

    public void addActvitiyCreationDates() {
        // start and end dates
        form.add(activityCreationStartDateField = newDateTextField("activityCreationStartDateField", new Date()));
        form.add(activityCreationEndDateField = newDateTextField("activityCreationEndDateField", new Date()));
        OnChangeAjaxBehavior onStartDateChangeAjaxBehavior = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                target.add(activityCreationEndDateField);

                // set listOfActivityCreationDates to between dates once start date is set
                listOfActivityCreationDates.setModelObject(CreationDate.Between_Dates);
                target.add(listOfActivityCreationDates);

                target.add(activityListPanel.setActivityCreationDate(selectedActivityCreationDate, target));
                target.add(activityListPanel.setActivityCreationStartDate(activityCreationStartDateField.getModelObject(), target));
                target.add(activityListPanel.setActivityCreationEndDate(activityCreationEndDateField.getModelObject(), target));
            }
        };
        activityCreationStartDateField.add(onStartDateChangeAjaxBehavior);

        OnChangeAjaxBehavior onEndDateChangeAjaxBehavior = new OnChangeAjaxBehavior() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // set listOfActivityCreationDates to between dates once start date is set
                listOfActivityCreationDates.setModelObject(CreationDate.Between_Dates);
                target.add(listOfActivityCreationDates);

                target.add(activityListPanel.setActivityCreationDate(selectedActivityCreationDate, target));
                target.add(activityListPanel.setActivityCreationStartDate(activityCreationStartDateField.getModelObject(), target));
                target.add(activityListPanel.setActivityCreationEndDate(activityCreationEndDateField.getModelObject(), target));
            }
        };
        activityCreationEndDateField.add(onEndDateChangeAjaxBehavior);
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
                if (activityCreationStartDateField.getModelObject() != null && id.equals(activityCreationEndDateField.getId())) {
                    config.withStartDate(new DateTime(DateUtils.addDays(activityCreationStartDateField.getModelObject(), 0)))
                            .showTodayButton(true);
                }
            }
        };
        return dateTextField;

    }

    /**
     * @return
     */
    private DateTextField newActivtiyCreationDateTextField(final String id) {
        final DateTextFieldConfig config;
        config = new DateTextFieldConfig().withFormat("dd.MM.yyyy").allowKeyboardNavigation(true).autoClose(true)
                .highlightToday(true).withStartDate(new DateTime(new Date())).showTodayButton(false);
        DateTextField dateTextField = new DateTextField(id, config) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            protected void onConfigure() {
                if (activityCreationStartDateField.getModelObject() != null && id.equals(activityCreationEndDateField.getId())) {
                    config.withStartDate(new DateTime(DateUtils.addDays(activityCreationStartDateField.getModelObject(), 0)))
                            .showTodayButton(true);
                }
            }
        };
        return dateTextField;
    }

    private RadioGroup<CreationDate> getActivityCreationDateRadioGroup() {
        // ActivityCreationDate
        RadioGroup<CreationDate> listOfTypesActivityCreationDate = new RadioGroup<CreationDate>(
                "typeOfActivityCreationDates", new PropertyModel<CreationDate>(this, "selectedActivityCreationDate"));
        listOfTypesActivityCreationDate.setOutputMarkupId(true);

        listOfTypesActivityCreationDate.add(new Radio<>("allTime", Model.of(CreationDate.All_Time)));
        listOfTypesActivityCreationDate.add(new Radio<>("lastWeek", Model.of(CreationDate.Last_Week)));
        listOfTypesActivityCreationDate.add(new Radio<>("lastMonth", Model.of(CreationDate.Last_Month)));
        listOfTypesActivityCreationDate.add(new Radio<>("lastYear", Model.of(CreationDate.Last_Year)));
        listOfTypesActivityCreationDate.add(new Radio<>("betweenDates", Model.of(CreationDate.Between_Dates)));
        return listOfTypesActivityCreationDate;
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

                // get activity creators 
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

        // at first, find out which activities are selected
        List<Activity> activities = new LinkedList<>();
        if (messageActivitySortingCriteria.getModelObject().equals(ActivityMessageProcessSortingCriteria.All_Activities)) {
            // find all activities with given filtering criteria... 
            List<Date> dates = activityListPanel.getDatesBasedOnSelectedCreationDate(
                    listOfActivityCreationDates.getModelObject(), activityCreationStartDateField.getModelObject(),
                    activityCreationEndDateField.getModelObject());

            activities = activityService.getAllActivitiesByActivityType(activityListPanel
                    .getActivityTypeFromSelectedParameters(listOfTypesOfActivityProcesses.getModelObject(),
                            listOfTypesOfActivities.getModelObject()), dates.get(0), dates.get(1), null, null);
        } else if (messageActivitySortingCriteria.getModelObject().equals(ActivityMessageProcessSortingCriteria.Selected_Activities)) {
            // find selected activities with given filtering criteria...
            activities = (List<Activity>) activityListPanel.getActivityGroup().getModelObject();
        }

        if (!activities.isEmpty()) {
            users = activityService.getAllActivityCreatorsFromGivenActivities(activities);
        }
        return users;
    }

    /**
     * @return
     */
    private DropDownChoice<ActivityMessageProcessSortingCriteria> newMessageProcessSortingCriteriaDropDownChoice() {
        List<ActivityMessageProcessSortingCriteria> listOfSorrtingCriteria = Arrays
                .asList(ActivityMessageProcessSortingCriteria.values());
        final DropDownChoice<ActivityMessageProcessSortingCriteria> sortingCriteriaChoice = new DropDownChoice<ActivityMessageProcessSortingCriteria>(
                "messageProcessSortingChoice", new Model<ActivityMessageProcessSortingCriteria>(), listOfSorrtingCriteria,
                new IChoiceRenderer<ActivityMessageProcessSortingCriteria>() {
                    private static final long serialVersionUID = -4910707158069588234L;

                    @Override
                    public Object getDisplayValue(ActivityMessageProcessSortingCriteria object) {
                        return new StringResourceModel(object.getNameKey(), CoordinatorDashboardActivitiesPage.this, null)
                                .getString();
                    }

                    @Override
                    public String getIdValue(ActivityMessageProcessSortingCriteria object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(ActivityMessageProcessSortingCriteria.Selected_Activities);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = -2840672822107134374L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                target.add(messageActivitySortingCriteria);
            }
        });
        return sortingCriteriaChoice;
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
    private DropDownChoice<ActivityMessageUserSortingCriteria> newMessageUsersSortingCriteriaDropDownChoice() {
        List<ActivityMessageUserSortingCriteria> listOfSorrtingCriteria = Arrays.asList(ActivityMessageUserSortingCriteria
                .values());
        final DropDownChoice<ActivityMessageUserSortingCriteria> sortingCriteriaChoice = new DropDownChoice<ActivityMessageUserSortingCriteria>(
                "messageUserSortingChoice", new Model<ActivityMessageUserSortingCriteria>(), listOfSorrtingCriteria,
                new IChoiceRenderer<ActivityMessageUserSortingCriteria>() {
                    private static final long serialVersionUID = -4910707158069588234L;

                    @Override
                    public Object getDisplayValue(ActivityMessageUserSortingCriteria object) {
                        return new StringResourceModel(object.getNameKey(), CoordinatorDashboardActivitiesPage.this, null)
                                .getString();
                    }

                    @Override
                    public String getIdValue(ActivityMessageUserSortingCriteria object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(ActivityMessageUserSortingCriteria.activityCreators);
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

    public enum ActivityMessageUserSortingCriteria {
        activityCreators("message.sort.activityCreators");
        private final String nameKey;

        ActivityMessageUserSortingCriteria(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }

    public enum ActivityMessageProcessSortingCriteria {

        All_Activities("sort.allActivities"),
        Selected_Activities("sort.selectedActivities");

        private final String nameKey;

        ActivityMessageProcessSortingCriteria(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }

    public enum TypeOfActivity {
        all,
        likes,
        comments,
        vote;
    }

    public enum CreationDate {

        All_Time("activity.allTime"),
        Last_Week("activity.lastWeek"),
        Last_Month("activity.lastMonth"),
        Last_Year("activity.lastYear"),
        Between_Dates("activity.betweenDates");

        private final String nameKey;

        CreationDate(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }

    public enum TypeOfActivityProcess {
        All("typeOfProcess.all"),
        Challenge_Definition("typeOfProcess.challengeDefinition"),
        Challenge_Idea_Selection("typeOfProcess.challengeIdeaSelection"),
        Challenge_Idea("typeOfProcess.challengeIdea"),
        Action_Solution("typeOfProcess.actionSolution"),
        Action_Iteration("typeOfProcess.actionIteration"),
        Business_Model("typeOfProcess.businessModel");

        private final String nameKey;

        public String getNameKey() {
            return nameKey;
        }

        TypeOfActivityProcess(final String nameKey) {
            this.nameKey = nameKey;
        }
    }

}

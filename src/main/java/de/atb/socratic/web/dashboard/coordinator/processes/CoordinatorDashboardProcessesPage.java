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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ejb.EJB;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.BusinessModel;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.service.inception.ActionIterationService;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.InputValidationForm;
import de.atb.socratic.web.dashboard.coordinator.DashboardCommonHeaderPage;
import de.atb.socratic.web.dashboard.message.CreateMessagePanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class CoordinatorDashboardProcessesPage extends DashboardCommonHeaderPage {
    private static final long serialVersionUID = -7085939814517735639L;

    @EJB
    CampaignService campaignService;

    @EJB
    IdeaService ideaService;

    @EJB
    ActionService actionService;

    @EJB
    ActionIterationService actionIterationService;

    @EJB
    ActivityService activityService;

    @EJB
    UserService userService;

    private final DropDownChoice<ProcessSortingCriteria> sortingCriteriaChoice;

    private final DropDownChoice<TypeOfprocess> listOfTypesOfProcesses;

    private TypeOfprocess selectedProcess = TypeOfprocess.All;

    private ChallengeProcessListPanel challengeProcessListPanel;
    private IdeaProcessListPanel ideaProcessListPanel;

    private ActionProcessListPanel actionSolutionProcessListPanel;
    private ActionIterationProcessListPanel actionIterationProcessListPanel;
    private BusinessModelProcessListPanel actionBusinessModelProcessListPanel;

    private ModalWindow createMessageModalWindow;
    private CreateMessagePanel messageModelPanel;
    private final DropDownChoice<MessageProcessSortingCriteria> messageProcessSortingCriteria;
    private final DropDownChoice<MessageUserSortingCriteria> messageUserSortingCriteria;

    private final Form<Void> form;

    public CoordinatorDashboardProcessesPage(PageParameters parameters) {
        super(parameters);

        form = new InputValidationForm<>("form");
        form.setOutputMarkupId(true);
        add(form);

        // Add process
        form.add(listOfTypesOfProcesses = getProcessTypeDropDownChoice());

        // Add sorting
        form.add(sortingCriteriaChoice = newSortingCriteriaDropDownChoice());

        // messages tab
        form.add(createMessageButton());
        form.add(createMessageModelWindow());
        form.add(messageProcessSortingCriteria = newMessageProcessSortingCriteriaDropDownChoice());
        form.add(messageUserSortingCriteria = newMessageUsersSortingCriteriaDropDownChoice());

        // add panel with list of campaigns
        form.add(challengeProcessListPanel = getChallengesListPanel());

        // Idea list
        form.add(ideaProcessListPanel = getIdeaListPanel());

        // Action list
        form.add(actionSolutionProcessListPanel = getActionsListPanel());

        // Iteration list
        form.add(actionIterationProcessListPanel = getIterationsListPanel());

        // Business Model list
        form.add(actionBusinessModelProcessListPanel = getBusinessModelListPanel());

    }

    private ChallengeProcessListPanel getChallengesListPanel() {
        ChallengeProcessListPanel challengeProcessListPanel = new ChallengeProcessListPanel("listOfChallengeProcesses",
                feedbackPanel, selectedProcess) {
            private static final long serialVersionUID = -7946672996240192916L;
        };

        challengeProcessListPanel.setOutputMarkupId(true);

        return challengeProcessListPanel;
    }

    private IdeaProcessListPanel getIdeaListPanel() {
        IdeaProcessListPanel ideaProcessListPanel = new IdeaProcessListPanel("listOfIdeaProcesses", feedbackPanel,
                selectedProcess) {
            private static final long serialVersionUID = -7946672996240192916L;
        };

        // only visible if selection is related to idea
        ideaProcessListPanel.setVisible(selectedProcess.equals(TypeOfprocess.All)
                || selectedProcess.equals(TypeOfprocess.Challenge_Idea));

        ideaProcessListPanel.setOutputMarkupId(true);
        return ideaProcessListPanel;
    }

    private ActionProcessListPanel getActionsListPanel() {
        ActionProcessListPanel actionProcessListPanel = new ActionProcessListPanel("listOfActionProcesses", feedbackPanel,
                selectedProcess) {
            private static final long serialVersionUID = -7946672996240192916L;
        };

        // only visible if selection is related to action
        actionProcessListPanel.setVisible(selectedProcess.equals(TypeOfprocess.All)
                || selectedProcess.equals(TypeOfprocess.Action_Solution));

        actionProcessListPanel.setOutputMarkupId(true);
        return actionProcessListPanel;
    }

    private ActionIterationProcessListPanel getIterationsListPanel() {
        ActionIterationProcessListPanel actionIterationProcessListPanel = new ActionIterationProcessListPanel(
                "listOfActionIterationProcesses", feedbackPanel, selectedProcess) {
            private static final long serialVersionUID = -7946672996240192916L;
        };

        // only visible if selection is related to iteration
        actionIterationProcessListPanel.setVisible(selectedProcess.equals(TypeOfprocess.All)
                || selectedProcess.equals(TypeOfprocess.Action_Iteration));

        actionIterationProcessListPanel.setOutputMarkupId(true);
        return actionIterationProcessListPanel;
    }

    private BusinessModelProcessListPanel getBusinessModelListPanel() {
        BusinessModelProcessListPanel businessModelProcessListPanel = new BusinessModelProcessListPanel(
                "listOfBusinessModelProcesses", feedbackPanel, selectedProcess) {
            private static final long serialVersionUID = -7946672996240192916L;
        };

        // only visible if selection is related to BusinessModel
        businessModelProcessListPanel.setVisible(selectedProcess.equals(TypeOfprocess.All)
                || selectedProcess.equals(TypeOfprocess.Business_Model));

        businessModelProcessListPanel.setOutputMarkupId(true);
        return businessModelProcessListPanel;
    }

    /**
     * @return
     */
    private DropDownChoice<TypeOfprocess> getProcessTypeDropDownChoice() {
        final DropDownChoice<TypeOfprocess> typeOfprocessCriteriaChoice = new DropDownChoice<>("typeOfprocess",
                new Model<TypeOfprocess>(), Arrays.asList(TypeOfprocess.values()),
                new IChoiceRenderer<TypeOfprocess>() {
                    private static final long serialVersionUID = 7632151907039246612L;

                    @Override
                    public Object getDisplayValue(TypeOfprocess object) {
                        return new StringResourceModel(object.getNameKey(), CoordinatorDashboardProcessesPage.this, null)
                                .getString();
                    }

                    @Override
                    public String getIdValue(TypeOfprocess object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        typeOfprocessCriteriaChoice.setNullValid(false);
        typeOfprocessCriteriaChoice.setDefaultModelObject(TypeOfprocess.All);
        typeOfprocessCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 4630143654574571697L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                // searchText = null;
                updateComponents(target);
            }
        });
        return typeOfprocessCriteriaChoice;

    }

    /**
     * @return
     */
    private DropDownChoice<ProcessSortingCriteria> newSortingCriteriaDropDownChoice() {
        final DropDownChoice<ProcessSortingCriteria> sortingCriteriaChoice = new DropDownChoice<>("sortingCriteriaChoice",
                new Model<ProcessSortingCriteria>(), Arrays.asList(ProcessSortingCriteria.values()),
                new IChoiceRenderer<ProcessSortingCriteria>() {
                    private static final long serialVersionUID = 7632151907039246612L;

                    @Override
                    public Object getDisplayValue(ProcessSortingCriteria object) {
                        return new StringResourceModel(object.getNameKey(), CoordinatorDashboardProcessesPage.this, null)
                                .getString();
                    }

                    @Override
                    public String getIdValue(ProcessSortingCriteria object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(ProcessSortingCriteria.creationDate);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 4630143654574571697L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                // searchText = null;
                updateComponents(target);
            }
        });
        return sortingCriteriaChoice;

    }

    /**
     * @param target
     */
    private void updateComponents(AjaxRequestTarget target) {
        selectedProcess = listOfTypesOfProcesses.getModelObject();

        // update challenges list
        target.add(this.challengeProcessListPanel.setProcessType(selectedProcess, target));
        target.add(this.challengeProcessListPanel.setProcessSortingCriteria(sortingCriteriaChoice.getModelObject(), target));

        // update ideas list
        target.add(this.ideaProcessListPanel.setProcessType(selectedProcess, target));
        target.add(this.ideaProcessListPanel.setProcessSortingCriteria(sortingCriteriaChoice.getModelObject(), target));

        // update actions list
        target.add(this.actionSolutionProcessListPanel.setProcessType(selectedProcess, target));
        target
                .add(this.actionSolutionProcessListPanel.setProcessSortingCriteria(sortingCriteriaChoice.getModelObject(), target));

        // update iterations list
        target.add(this.actionIterationProcessListPanel.setProcessType(selectedProcess, target));
        target.add(this.actionIterationProcessListPanel.setProcessSortingCriteria(sortingCriteriaChoice.getModelObject(),
                target));

        // update business models list
        target.add(this.actionBusinessModelProcessListPanel.setProcessType(selectedProcess, target));
        target.add(this.actionBusinessModelProcessListPanel.setProcessSortingCriteria(sortingCriteriaChoice.getModelObject(),
                target));
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

    private boolean isSelectedProcessActionRelated() {
        return (selectedProcess.equals(TypeOfprocess.Action_Solution) || selectedProcess.equals(TypeOfprocess.Action_Iteration)
                || selectedProcess.equals(TypeOfprocess.Business_Model) || selectedProcess.equals(TypeOfprocess.All));
    }

    private List<User> getUsersFromChallenges(InnovationStatus status, Collection<Campaign> challenges) {
        List<User> users = new LinkedList<>();

        // get users or contributors or followers for all challenges
        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.contributors)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get contributors for selected challenges
            users.addAll(activityService.getChallengesContributorsWithInnovationStatusOderedByAscNickName(status, challenges));
        }

        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.followers)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get followers for selected challenges    
            users.addAll(userService.getChallengeFollowersWithInnovationStatusOrderedByAscNickName(status, challenges));
        }

        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.leaders)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get leaders for selected challenges
            users.addAll(campaignService.getChallengeLeadersWithInnovationStatusOrderedByAscNickName(status, challenges));
        }
        return users;
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "processesTab");
    }

    private List<User> getUsersFromIdeas(Collection<Idea> ideas) {
        List<User> users = new LinkedList<>();
        // get users or contributors or followers for all ideas
        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.contributors)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get contributors for all ideas
            users.addAll(activityService.getAllIdeasContributorsByAscNickName(ideas));
        }

        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.followers)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get followers for all ideas    
            users.addAll(userService.getAllUsersByFollowingGivenIdeas(ideas));
        }

        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.leaders)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get leaders for all ideas
            users.addAll(ideaService.getIdeaLeadersForGivenIdeasByAscNickName(ideas));
        }
        return users;
    }

    private List<User> getUsersFromActions(Collection<Action> actions) {
        List<User> users = new LinkedList<>();

        // get users or contributors or followers for all actions
        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.contributors)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get contributors for all actions
            users.addAll(activityService.getActionsContributorsOderedByAscNickName(actions));
        }

        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.followers)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get followers for all actions
            users.addAll(userService.getAllUsersByFollowingGivenActions(actions));
        }

        if (messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.leaders)
                || messageUserSortingCriteria.getModelObject().equals(MessageUserSortingCriteria.users)) {
            // get leaders for all actions
            users.addAll(actionService.getAllActionLeadersByAscNickName(actions));
        }
        return users;
    }

    /**
     * 1: Check which message process criteria are selected(all_processes or selected_processes).
     * 2: if selected_processes is selected then find out which is selected by user with checkgroup
     * 3: if there is none then return empty
     * 4: if there are few processes selected by user then check which message user criteria is selected(user, leader, contributor)
     * 5: return appropriate results
     * 6: if all_processes is selected as message process criteria then find out which process type is selected
     * 7: then check  message user criteria is selected(user, leader, contributor)
     * 8: return appropriate results
     *
     * @return
     */
    public List<User> getSelectedUsersForProcess() {
        List<User> users = new LinkedList<>();
        Collection<Campaign> listOfSelectedChallenges = new LinkedList<>();
        Collection<Idea> listOfSelectedIdeas = new LinkedList<>();
        Collection<Action> listOfSelectedActions = new LinkedList<>();
        // at first, Check which message process criteria are selected(all_processes or selected_processes).        
        if (messageProcessSortingCriteria.getModelObject().equals(MessageProcessSortingCriteria.Selected_Processes)) {
            // for selected processes, check if user has selected any of the process
            // find out which are selected challenges
            listOfSelectedChallenges = challengeProcessListPanel.getChallengeGroup().getModelObject();

            // find out which are selected ideas
            listOfSelectedIdeas = ideaProcessListPanel.getIdeaGroup().getModelObject();

            // find out which are selected actions
            listOfSelectedActions = actionSolutionProcessListPanel.getActionGroup().getModelObject();

            // add action from iterations as well
            for (ActionIteration iteration : actionIterationProcessListPanel.getActionIterationGroup().getModelObject()) {
                listOfSelectedActions.add(iteration.getAction());
            }

            // add action from business model as well
            for (BusinessModel businessModel : actionBusinessModelProcessListPanel.getBusinessModelGroup().getModelObject()) {
                listOfSelectedActions.add(actionService.getActionFromBusinessModel(businessModel));
            }

            // check if any all of the above list are empty? 
            if (listOfSelectedChallenges.isEmpty() && listOfSelectedIdeas.isEmpty() && listOfSelectedActions.isEmpty()) {
                return new LinkedList<>();
            }
            // this means, now on if MessageProcessSortingCriteria is Selected_Processes then one of the list is not empty or
            // MessageProcessSortingCriteria is All_Process

            // check if listOfSelectedChallenges is not null
            if (!listOfSelectedChallenges.isEmpty()) {
                if (selectedProcess.equals(TypeOfprocess.Challenge_Definition)
                        || selectedProcess.equals(TypeOfprocess.All)) {
                    users.addAll(getUsersFromChallenges(InnovationStatus.DEFINITION, listOfSelectedChallenges));
                }
                if (selectedProcess.equals(TypeOfprocess.Challenge_Ideation)
                        || selectedProcess.equals(TypeOfprocess.All)) {
                    users.addAll(getUsersFromChallenges(InnovationStatus.INCEPTION, listOfSelectedChallenges));
                }
                if (selectedProcess.equals(TypeOfprocess.Challenge_Idea_Selection)
                        || selectedProcess.equals(TypeOfprocess.All)) {
                    users.addAll(getUsersFromChallenges(InnovationStatus.PRIORITISATION, listOfSelectedChallenges));
                }
                if (selectedProcess.equals(TypeOfprocess.Challenge_Idea_Implementation)
                        || selectedProcess.equals(TypeOfprocess.All)) {
                    users.addAll(getUsersFromChallenges(InnovationStatus.IMPLEMENTATION, listOfSelectedChallenges));
                }
            }

            // check if listOfSelectedIdeas is not null
            if (!listOfSelectedIdeas.isEmpty()) {
                if (selectedProcess.equals(TypeOfprocess.Challenge_Idea) || selectedProcess.equals(TypeOfprocess.All)) {
                    users.addAll(getUsersFromIdeas(listOfSelectedIdeas));
                }
            }

            // check if listOfSelectedActions is not null
            if (!listOfSelectedActions.isEmpty()) {
                if (isSelectedProcessActionRelated()) {
                    users.addAll(getUsersFromActions(listOfSelectedActions));
                }
            }
        } else if (messageProcessSortingCriteria.getModelObject().equals(MessageProcessSortingCriteria.All_Processes)) {
            if (selectedProcess.equals(TypeOfprocess.Challenge_Definition)
                    || selectedProcess.equals(TypeOfprocess.All)) {
                users.addAll(getUsersFromChallenges(InnovationStatus.DEFINITION, null));
            }
            if (selectedProcess.equals(TypeOfprocess.Challenge_Ideation)
                    || selectedProcess.equals(TypeOfprocess.All)) {
                users.addAll(getUsersFromChallenges(InnovationStatus.INCEPTION, null));
            }
            if (selectedProcess.equals(TypeOfprocess.Challenge_Idea_Selection)
                    || selectedProcess.equals(TypeOfprocess.All)) {
                users.addAll(getUsersFromChallenges(InnovationStatus.PRIORITISATION, null));
            }
            if (selectedProcess.equals(TypeOfprocess.Challenge_Idea_Implementation)
                    || selectedProcess.equals(TypeOfprocess.All)) {
                users.addAll(getUsersFromChallenges(InnovationStatus.IMPLEMENTATION, null));
            }

            if (selectedProcess.equals(TypeOfprocess.Challenge_Idea) || selectedProcess.equals(TypeOfprocess.All)) {
                users.addAll(getUsersFromIdeas(null));
            }

            if (isSelectedProcessActionRelated()) {
                users.addAll(getUsersFromActions(null));
            }

        }

        return users;
    }

    /**
     * @return
     */
    private DropDownChoice<MessageProcessSortingCriteria> newMessageProcessSortingCriteriaDropDownChoice() {
        List<MessageProcessSortingCriteria> listOfSorrtingCriteria = Arrays.asList(MessageProcessSortingCriteria.values());
        final DropDownChoice<MessageProcessSortingCriteria> sortingCriteriaChoice = new DropDownChoice<MessageProcessSortingCriteria>(
                "messageProcessSortingChoice", new Model<MessageProcessSortingCriteria>(), listOfSorrtingCriteria,
                new IChoiceRenderer<MessageProcessSortingCriteria>() {
                    private static final long serialVersionUID = -4910707158069588234L;

                    @Override
                    public Object getDisplayValue(MessageProcessSortingCriteria object) {
                        return new StringResourceModel(object.getNameKey(), CoordinatorDashboardProcessesPage.this, null)
                                .getString();
                    }

                    @Override
                    public String getIdValue(MessageProcessSortingCriteria object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(MessageProcessSortingCriteria.Selected_Processes);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = -2840672822107134374L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                target.add(messageProcessSortingCriteria);
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
    private DropDownChoice<MessageUserSortingCriteria> newMessageUsersSortingCriteriaDropDownChoice() {
        List<MessageUserSortingCriteria> listOfSorrtingCriteria = Arrays.asList(MessageUserSortingCriteria.values());
        final DropDownChoice<MessageUserSortingCriteria> sortingCriteriaChoice = new DropDownChoice<MessageUserSortingCriteria>(
                "messageUserSortingChoice", new Model<MessageUserSortingCriteria>(), listOfSorrtingCriteria) {
            private static final long serialVersionUID = 5984464971426012330L;
        };

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(MessageUserSortingCriteria.users);
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

    public enum TypeOfprocess {
        All("typeOfProcess.all"),
        Challenge_Definition("typeOfProcess.challengeDefinition"),
        Challenge_Ideation("typeOfProcess.challengeIdeation"),
        Challenge_Idea_Selection("typeOfProcess.challengeIdeaSelection"),
        Challenge_Idea_Implementation("typeOfProcess.challengeIdeaImplementation"),
        Challenge_Idea("typeOfProcess.challengeIdea"),
        Action_Solution("typeOfProcess.actionSolution"),
        Action_Iteration("typeOfProcess.actionIteration"),
        Business_Model("typeOfProcess.businessModel");

        private final String nameKey;

        public String getNameKey() {
            return nameKey;
        }

        TypeOfprocess(final String nameKey) {
            this.nameKey = nameKey;
        }
    }

    public enum ProcessSortingCriteria {
        creationDate("sort.creationDate"),
        commentsMost("sort.comments.most"),
        commentsLess("sort.comments.less"),
        LikesMost("sort.likes.most"),
        LikesLess("sort.likes.less"),
        FollowersMost("sort.followers.most"),
        FollowersLess("sort.followers.less"),
        ContributorsMost("sort.contributors.most"),
        ContributorsLess("sort.contributors.less");

        private final String nameKey;

        ProcessSortingCriteria(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }

    public enum MessageProcessSortingCriteria {

        All_Processes("sort.allProcesses"),
        Selected_Processes("sort.selectedProcesses");

        private final String nameKey;

        MessageProcessSortingCriteria(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }

    public enum MessageUserSortingCriteria {
        users,
        followers,
        contributors,
        leaders;
    }
}

package de.atb.socratic.web.dashboard;

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

import javax.ejb.EJB;

import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.web.dashboard.panels.ActionOverAllListPanel;
import de.atb.socratic.web.dashboard.panels.ChallengeOverAllListPanel;
import de.atb.socratic.web.dashboard.panels.IdeaOverAllListPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * @author ATB
 */
public class UserDashboardPage extends Dashboard {
    private static final long serialVersionUID = -7085939814517735639L;
    private final ChallengeOverAllListPanel challengeParticipationListPanel;
    private final IdeaOverAllListPanel ideaParticipationListPanel;
    private final ActionOverAllListPanel actionParticipationListPanel;

    private final ChallengeOverAllListPanel challengeLeadListPanel;
    private final IdeaOverAllListPanel ideaLeadListPanel;
    private final ActionOverAllListPanel actionLeadListPanel;

    private final DropDownChoice<EntitiySortingCriteria> challengeParticipationSortingCriteriaChoice;
    private final DropDownChoice<EntitiySortingCriteria> challengeLeadSortingCriteriaChoice;

    private final DropDownChoice<EntitiySortingCriteria> ideaParticipationSortingCriteriaChoice;
    private final DropDownChoice<EntitiySortingCriteria> ideaLeadSortingCriteriaChoice;

    private final DropDownChoice<EntitiySortingCriteria> actionParticipationSortingCriteriaChoice;
    private final DropDownChoice<EntitiySortingCriteria> actionLeadSortingCriteriaChoice;

    @EJB
    private ActivityService activityService;

    public UserDashboardPage(PageParameters parameters) {
        super(parameters);

        Label takePartHeaderTextLabel = new Label("takePartHeaderText", new StringResourceModel("header.take_part", this, null,
                Model.of(activityService.countAllActivitiesPerformedByUser(loggedInUser))));
        takePartHeaderTextLabel.setOutputMarkupId(true);
        add(takePartHeaderTextLabel);

        // Add sorting form: Participation
        Form<Void> challengeParticipationSortingCriteriaForm = new Form<Void>("challengeParticipationSortingCriteriaForm");
        add(challengeParticipationSortingCriteriaForm);
        challengeParticipationSortingCriteriaForm.add(challengeParticipationSortingCriteriaChoice = newSortingCriteriaDropDownChoice("challengeParticipationSortingCriteriaChoice"));

        // add panel with list of Participation challenges
        challengeParticipationListPanel = new ChallengeOverAllListPanel("challengesParticipationList", Model.of(loggedInUser),
                Status.PARTICIPATE) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(challengeParticipationListPanel.setOutputMarkupId(true));

        // Add sorting form: Participation
        Form<Void> ideaParticipationSortingCriteriaForm = new Form<Void>("ideaParticipationSortingCriteriaForm");
        add(ideaParticipationSortingCriteriaForm);
        ideaParticipationSortingCriteriaForm.add(ideaParticipationSortingCriteriaChoice = newSortingCriteriaDropDownChoice("ideaParticipationSortingCriteriaChoice"));

        // add panel with list of Participation ideas
        ideaParticipationListPanel = new IdeaOverAllListPanel("ideasParticipationList", Model.of(loggedInUser),
                Status.PARTICIPATE) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(ideaParticipationListPanel.setOutputMarkupId(true));

        // Add sorting form: Participation
        Form<Void> actionParticipationSortingCriteriaForm = new Form<Void>("actionParticipationSortingCriteriaForm");
        add(actionParticipationSortingCriteriaForm);
        actionParticipationSortingCriteriaForm.add(actionParticipationSortingCriteriaChoice = newSortingCriteriaDropDownChoice("actionParticipationSortingCriteriaChoice"));

        // add panel with list of Participation challenges
        actionParticipationListPanel = new ActionOverAllListPanel("actionsParticipationList", Model.of(loggedInUser),
                Status.PARTICIPATE) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(actionParticipationListPanel.setOutputMarkupId(true));


        int totalNoOfLeadsByUser = loggedInUser.getNoOfCampaignsLeads() + loggedInUser.getNoOfIdeasLeads() + loggedInUser.getNoOfActionsLeads();
        Label leadHeaderTextLabel = new Label("leadHeaderText", new StringResourceModel("header.lead", this, null,
                Model.of(totalNoOfLeadsByUser)));
        leadHeaderTextLabel.setOutputMarkupId(true);
        add(leadHeaderTextLabel);

        // Add sorting form: Lead
        Form<Void> challengeLeadSortingCriteriaForm = new Form<Void>("challengeLeadSortingCriteriaForm");
        add(challengeLeadSortingCriteriaForm);
        challengeLeadSortingCriteriaForm.add(challengeLeadSortingCriteriaChoice = newSortingCriteriaDropDownChoice("challengeLeadSortingCriteriaChoice"));

        // add panel with list of Lead challenges
        challengeLeadListPanel = new ChallengeOverAllListPanel("challengesLeadList", Model.of(loggedInUser),
                Status.LEAD) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(challengeLeadListPanel.setOutputMarkupId(true));

        // Add sorting form: Lead
        Form<Void> ideaLeadSortingCriteriaForm = new Form<Void>("ideaLeadSortingCriteriaForm");
        add(ideaLeadSortingCriteriaForm);
        ideaLeadSortingCriteriaForm.add(ideaLeadSortingCriteriaChoice = newSortingCriteriaDropDownChoice("ideaLeadSortingCriteriaChoice"));

        // add panel with list of Lead ideas
        ideaLeadListPanel = new IdeaOverAllListPanel("ideasLeadList", Model.of(loggedInUser),
                Status.LEAD) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(ideaLeadListPanel.setOutputMarkupId(true));

        // Add sorting form: Lead
        Form<Void> actionLeadSortingCriteriaForm = new Form<Void>("actionLeadSortingCriteriaForm");
        add(actionLeadSortingCriteriaForm);
        actionLeadSortingCriteriaForm.add(actionLeadSortingCriteriaChoice = newSortingCriteriaDropDownChoice("actionLeadSortingCriteriaChoice"));

        // add panel with list of Lead challenges
        actionLeadListPanel = new ActionOverAllListPanel("actionsLeadList", Model.of(loggedInUser),
                Status.LEAD) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(actionLeadListPanel.setOutputMarkupId(true));
    }

    /**
     * @return
     */
    private DropDownChoice<EntitiySortingCriteria> newSortingCriteriaDropDownChoice(String wicketId) {
        final DropDownChoice<EntitiySortingCriteria> sortingCriteriaChoice = new DropDownChoice<>(
                wicketId,
                new Model<EntitiySortingCriteria>(),
                Arrays.asList(EntitiySortingCriteria.values()),
                new IChoiceRenderer<EntitiySortingCriteria>() {
                    private static final long serialVersionUID = -3507943582789662873L;

                    @Override
                    public Object getDisplayValue(EntitiySortingCriteria object) {
                        return new StringResourceModel(object.getKey(), UserDashboardPage.this, null).getString();
                    }

                    @Override
                    public String getIdValue(EntitiySortingCriteria object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(EntitiySortingCriteria.created);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = 4630143654574571697L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateComponents(target);
            }
        });
        return sortingCriteriaChoice;
    }

    /**
     * @param target
     */
    private void updateComponents(AjaxRequestTarget target) {
        target.add(challengeParticipationListPanel.setSortingCriteria(challengeParticipationSortingCriteriaChoice.getModelObject()));
        target.add(challengeLeadListPanel.setSortingCriteria(challengeLeadSortingCriteriaChoice.getModelObject()));

        target.add(ideaParticipationListPanel.setSortingCriteria(ideaParticipationSortingCriteriaChoice.getModelObject()));
        target.add(ideaLeadListPanel.setSortingCriteria(ideaLeadSortingCriteriaChoice.getModelObject()));

        target.add(actionParticipationListPanel.setSortingCriteria(actionParticipationSortingCriteriaChoice.getModelObject()));
        target.add(actionLeadListPanel.setSortingCriteria(actionLeadSortingCriteriaChoice.getModelObject()));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "dashboardTab");
    }

    public enum Status {
        PARTICIPATE,
        LEAD,
        RECOMMENDED;
    }
}

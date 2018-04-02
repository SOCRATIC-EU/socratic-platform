package de.atb.socratic.web.dashboard.iParticipate;

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

import de.atb.socratic.web.dashboard.Dashboard;
import de.atb.socratic.web.dashboard.iLead.action.AdminActionShowAllPage;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminChallengeShowAllPage;
import de.atb.socratic.web.dashboard.iLead.idea.AdminIdeaShowAllPage;
import de.atb.socratic.web.dashboard.panels.ActionListPanel;
import de.atb.socratic.web.dashboard.panels.ChallengeListPanel;
import de.atb.socratic.web.dashboard.panels.IdeasListPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
public class UserParticipationDashboardPage extends Dashboard {

    private static final long serialVersionUID = -1334435493001211126L;
    private final ChallengeListPanel challengeListPanel;
    private final IdeasListPanel ideaListPanel;
    private final ActionListPanel actionListPanel;

    // how many challenges do we show initially
    private static final int challengesPerPage = 3;

    // how many ideas do we show initially
    private static final int ideasPerPage = 3;

    // how many actions do we show initially
    private static final int actionsPerPage = 3;

    private final DropDownChoice<EntitiySortingCriteria> challengeSortingCriteriaChoice;
    private final DropDownChoice<EntitiySortingCriteria> ideaSortingCriteriaChoice;
    private final DropDownChoice<EntitiySortingCriteria> actionSortingCriteriaChoice;

    public UserParticipationDashboardPage(PageParameters parameters) {
        super(parameters);

        // add show all challenges link
        AjaxLink<AdminChallengeShowAllPage> adminChallengeShowAllPageLink = new AjaxLink<AdminChallengeShowAllPage>("showAllChallenges") {

            private static final long serialVersionUID = -2547036150493181142L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminChallengeShowAllPage.class, new PageParameters().set("state", StateForDashboard.TakePart));
            }
        };
        add(adminChallengeShowAllPageLink);

        // Add sorting form
        Form<Void> challengeSortingCriteriaForm = new Form<Void>("challengeSortingCriteriaForm");
        add(challengeSortingCriteriaForm);
        challengeSortingCriteriaForm.add(challengeSortingCriteriaChoice = newSortingCriteriaDropDownChoice("challengeSortingCriteriaChoice"));

        // add panel with list of existing challenges
        challengeListPanel = new ChallengeListPanel("challengesList", Model.of(loggedInUser), feedbackPanel,
                StateForDashboard.TakePart, challengesPerPage) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(challengeListPanel.setOutputMarkupId(true));

        // add show all ideas link
        AjaxLink<AdminIdeaShowAllPage> adminIdeaShowAllPageLink = new AjaxLink<AdminIdeaShowAllPage>("showAllIdeas") {

            private static final long serialVersionUID = -2547036150493181142L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminIdeaShowAllPage.class, new PageParameters().set("state", StateForDashboard.TakePart));
            }
        };
        add(adminIdeaShowAllPageLink);

        // add panel with list of existing ideas
        ideaListPanel = new IdeasListPanel("ideasList", Model.of(loggedInUser), feedbackPanel, StateForDashboard.TakePart,
                ideasPerPage) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(ideaListPanel.setOutputMarkupId(true));

        // Add sorting form
        Form<Void> ideaSortingCriteriaForm = new Form<Void>("ideaSortingCriteriaForm");
        add(ideaSortingCriteriaForm);
        ideaSortingCriteriaForm.add(ideaSortingCriteriaChoice = newSortingCriteriaDropDownChoice("ideaSortingCriteriaChoice"));

        // add show all actions link
        AjaxLink<AdminActionShowAllPage> adminActionShowAllPageLink = new AjaxLink<AdminActionShowAllPage>("showAllActions") {

            private static final long serialVersionUID = -2547036150493181142L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionShowAllPage.class, new PageParameters().set("state", StateForDashboard.TakePart));
            }
        };
        add(adminActionShowAllPageLink);

        // Add sorting form
        Form<Void> actionSortingCriteriaForm = new Form<Void>("actionSortingCriteriaForm");
        add(actionSortingCriteriaForm);
        actionSortingCriteriaForm.add(actionSortingCriteriaChoice = newSortingCriteriaDropDownChoice("actionSortingCriteriaChoice"));

        // add panel with list of existing action
        actionListPanel = new ActionListPanel("actionsList", Model.of(loggedInUser), feedbackPanel, StateForDashboard.TakePart, actionsPerPage) {
            private static final long serialVersionUID = 6393614856591095877L;
        };
        add(actionListPanel.setOutputMarkupId(true));
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
                        return new StringResourceModel(object.getKey(), UserParticipationDashboardPage.this, null).getString();
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
        target.add(challengeListPanel.setSortingCriteria(challengeSortingCriteriaChoice.getModelObject()));
        target.add(ideaListPanel.setSortingCriteria(ideaSortingCriteriaChoice.getModelObject()));
        target.add(actionListPanel.setSortingCriteria(actionSortingCriteriaChoice.getModelObject()));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "participationTab");
    }

}

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

import java.util.Arrays;

import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.web.dashboard.Dashboard;
import de.atb.socratic.web.dashboard.panels.IdeasListPanel;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
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
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class AdminIdeaShowAllPage extends Dashboard {
    private static final long serialVersionUID = -5559578453943490669L;

    private final IdeasListPanel ideaListPanel;

    // how many ideas do we show initially
    private static final int ideasPerPage = 9;

    private StateForDashboard state;

    private String stateForRenderMethod;

    @Inject
    @LoggedInUser
    protected User loggedInUser;

    private final DropDownChoice<EntitiySortingCriteria> ideaSortingCriteriaChoice;

    public AdminIdeaShowAllPage(final PageParameters parameters) {
        super(parameters);

        String stateForDashboard = parameters.get("state").toOptionalString();
        if (stateForDashboard.equals(StateForDashboard.Lead.toString())) {
            this.state = StateForDashboard.Lead;
            this.stateForRenderMethod = "leadTab";
        } else {
            this.state = StateForDashboard.TakePart;
            this.stateForRenderMethod = "participationTab";
        }

        // Add sorting form
        Form<Void> ideaSortingCriteriaForm = new Form<Void>("ideaSortingCriteriaForm");
        add(ideaSortingCriteriaForm);
        ideaSortingCriteriaForm.add(ideaSortingCriteriaChoice = newSortingCriteriaDropDownChoice("ideaSortingCriteriaChoice"));

        // add panel with list of existing ideas
        ideaListPanel = new IdeasListPanel("ideasList", Model.of(loggedInUser), feedbackPanel, state, ideasPerPage) {
            private static final long serialVersionUID = 8513797732098055780L;
        };
        add(ideaListPanel.setOutputMarkupId(true));
    }

    /**
     * @return
     */
    private DropDownChoice<EntitiySortingCriteria> newSortingCriteriaDropDownChoice(String wicketId) {
        final DropDownChoice<EntitiySortingCriteria> sortingCriteriaChoice = new DropDownChoice<>(wicketId,
                new Model<EntitiySortingCriteria>(), Arrays.asList(EntitiySortingCriteria.values()),
                new IChoiceRenderer<EntitiySortingCriteria>() {
                    private static final long serialVersionUID = -3507943582789662873L;

                    @Override
                    public Object getDisplayValue(EntitiySortingCriteria object) {
                        return new StringResourceModel(object.getKey(), AdminIdeaShowAllPage.this, null).getString();
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
        target.add(ideaListPanel.setSortingCriteria(ideaSortingCriteriaChoice.getModelObject()));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, stateForRenderMethod);
    }
}

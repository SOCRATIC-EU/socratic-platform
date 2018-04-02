/**
 *
 */
package de.atb.socratic.web.dashboard.iLead.challenge;

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
import java.util.List;

import de.atb.socratic.model.InnovationStatus;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class AdminIdeaSelectionPage extends AdminChallenge {
    private static final long serialVersionUID = -5559578453943490669L;

    private final AdminIdeaListPanel adminIdeaListPanel;

    public AdminIdeaSelectionPage(PageParameters parameters) {
        super(parameters);

        adminIdeaListPanel = new AdminIdeaListPanel("ideaList", Model.of(theChallenge), feedbackPanel) {

            private static final long serialVersionUID = 8087201252939474804L;

            @Override
            protected void onConfigure() {
                super.onConfigure();

                // set List of ideas visible; if selection phase is over
                setVisible(InnovationStatus.IMPLEMENTATION == theChallenge.getInnovationStatus());
            }
        };
        add(adminIdeaListPanel.setOutputMarkupId(true));

        add(newSortingCriteriaDropDownChoice());
    }

    /**
     * @return
     */
    private DropDownChoice<IdeaSelectionSortingCriteria> newSortingCriteriaDropDownChoice() {
        List<IdeaSelectionSortingCriteria> listOfSorrtingCriteria = Arrays.asList(IdeaSelectionSortingCriteria.values());
        final DropDownChoice<IdeaSelectionSortingCriteria> sortingCriteriaChoice = new DropDownChoice<IdeaSelectionSortingCriteria>(
                "sortingCriteriaChoice", new Model<IdeaSelectionSortingCriteria>(), listOfSorrtingCriteria);

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.add(new AjaxFormComponentUpdatingBehavior("change") {
            private static final long serialVersionUID = -2730480336562437755L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                // reset any search term we may have entered before
                target.add(adminIdeaListPanel.setSortingCriteria(sortingCriteriaChoice.getModelObject()));
            }
        });
        return sortingCriteriaChoice;
    }

    public enum IdeaSelectionSortingCriteria {
        OverallRating,
        Relevancy,
        Feasibility,
        Date,
        Comments,
        Likes;
    }
}

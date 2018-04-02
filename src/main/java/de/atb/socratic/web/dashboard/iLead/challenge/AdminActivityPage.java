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

import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class AdminActivityPage extends AdminChallenge {
    private static final long serialVersionUID = -5559578453943490669L;

    private final AdminActivitiesListPanel adminActivityListPanel;

    public AdminActivityPage(PageParameters parameters) {
        super(parameters);

        adminActivityListPanel = new AdminActivitiesListPanel("activitiesList", Model.of(theChallenge), feedbackPanel) {

            private static final long serialVersionUID = 8087201252939474804L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        add(adminActivityListPanel.setOutputMarkupId(true));

        //add(newSortingCriteriaDropDownChoice());
    }

    public enum ParticipantsSortingCriteria {
        DefinitionComments,
        References,
        Ideas,
        Date
    }
}

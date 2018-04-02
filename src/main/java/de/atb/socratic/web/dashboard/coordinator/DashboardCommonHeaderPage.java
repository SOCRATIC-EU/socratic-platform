package de.atb.socratic.web.dashboard.coordinator;

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

import javax.inject.Inject;

import de.atb.socratic.model.User;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.dashboard.coordinator.activities.CoordinatorDashboardActivitiesPage;
import de.atb.socratic.web.dashboard.coordinator.messages.CoordinatorDashboardMessagesInboxPage;
import de.atb.socratic.web.dashboard.coordinator.overview.CoordinatorDashboardOverviewPage;
import de.atb.socratic.web.dashboard.coordinator.processes.CoordinatorDashboardProcessesPage;
import de.atb.socratic.web.dashboard.coordinator.users.CoordinatorDashboardUsersPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class DashboardCommonHeaderPage extends BasePage {
    private static final long serialVersionUID = -9092430357787574242L;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    protected User loggedInUser;

    public DashboardCommonHeaderPage(final PageParameters parameters) {
        super(parameters);

        add(new AjaxLink<Void>("overview") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(CoordinatorDashboardOverviewPage.class);
            }
        });

        add(new AjaxLink<Void>("processes") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                this.add(new AttributeModifier("class", "active"));
                setResponsePage(CoordinatorDashboardProcessesPage.class);
            }
        });

        add(new AjaxLink<Void>("activities") {
            private static final long serialVersionUID = -6633994061963892062L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(CoordinatorDashboardActivitiesPage.class);
            }
        });

        add(new AjaxLink<Void>("users") {
            private static final long serialVersionUID = -3486178522694616314L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(CoordinatorDashboardUsersPage.class);
            }
        });

        add(new AjaxLink<Void>("messages") {
            private static final long serialVersionUID = -3486178522694616314L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(CoordinatorDashboardMessagesInboxPage.class);
            }
        });
    }

    protected void activateCurrentTab(IHeaderResponse response, final String currentTabId) {
        // make current tab "active", all others "inactive"
        response.render(OnDomReadyHeaderItem.forScript("$('#tabs > li').removeClass('active');$('#" + currentTabId + "').addClass('active');"));
    }

    /*
     * (non-Javadoc)
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }
}

package de.atb.socratic.web.dashboard.iLead.action;

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

import javax.ejb.EJB;

import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.action.detail.ActionSolutionPage;
import de.atb.socratic.web.dashboard.Dashboard;
import de.atb.socratic.web.dashboard.iLead.UserLeadDashboardPage;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * @author ATB
 */
public class AdminAction extends Dashboard {
    private static final long serialVersionUID = -5559578453943490669L;

    protected Action theAction;

    @EJB
    ActionService actionService;

    public AdminAction(final PageParameters parameters) {
        super(parameters);

        loadAction(parameters.get("id"));

        // shortText
        add(new Label("shortText", new PropertyModel<String>(theAction, "shortText")));

        // explore Action
        BookmarkablePageLink<ActionSolutionPage> exploreLink = newExploreLink(parameters);
        add(exploreLink);

        add(new AjaxLink<Void>("recentActivity") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionActivityPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("editActionSolution") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                parameters.set("ideaId", theAction.getIdea().getId());
                setResponsePage(AdminActionSolutionEditPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("iterations") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionIterationsListPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("status") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionStatusEditPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("team") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionTeamPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("participants") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(AdminActionParticipantsPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("businessModel") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                parameters.set("businessModelId", theAction.getBusinessModel().getId());
                setResponsePage(AdminActionBusinessModelAddEditPage.class, parameters);
            }
        });

        // add a back link
        add(new AjaxLink<User>("back") {
            private static final long serialVersionUID = -4776506958975416730L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(UserLeadDashboardPage.class);
            }

            @Override
            protected void onConfigure() {
                setVisible(true);
                super.onConfigure();
            }
        });
    }

    protected void activateCurrentSideTab(IHeaderResponse response, final String currentTabId) {
        // make current tab "active", all others "inactive"
        response.render(OnDomReadyHeaderItem.forScript("$('#sideTabs > li').removeClass('active');$('#" + currentTabId + "').addClass('active');"));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "leadTab");
    }

    /**
     * @return
     */
    private BookmarkablePageLink<ActionSolutionPage> newExploreLink(final PageParameters parameters) {
        BookmarkablePageLink<ActionSolutionPage> bookmarkablePageLink = new BookmarkablePageLink<ActionSolutionPage>(
                "exploreLink", ActionSolutionPage.class, parameters) {
            private static final long serialVersionUID = 2000408686308968065L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        return bookmarkablePageLink;
    }

    private void loadAction(final StringValue id) {
        if (id != null && !id.isEmpty()) {
            try {
                theAction = actionService.getById(id.toOptionalLong());
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }
}

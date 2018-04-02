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

import javax.ejb.EJB;

import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.web.ErrorPage;
import de.atb.socratic.web.dashboard.Dashboard;
import de.atb.socratic.web.dashboard.iLead.UserLeadDashboardPage;
import de.atb.socratic.web.inception.idea.IdeaAddEditPage;
import de.atb.socratic.web.inception.idea.details.IdeaDetailsPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * @author ATB
 */
public class AdminIdeaPage extends Dashboard {
    private static final long serialVersionUID = -5559578453943490669L;

    protected Idea theIdea;
    @EJB
    IdeaService ideaService;

    public AdminIdeaPage(final PageParameters parameters) {
        super(parameters);

        loadIdea(parameters.get("id"));
        // name
        add(new Label("shortText", new PropertyModel<String>(theIdea, "shortText")));
        add(new BookmarkablePageLink<IdeaDetailsPage>("exploreLink", IdeaDetailsPage.class, new PageParameters().set("id",
                theIdea.getCampaign().getId()).set("ideaId", theIdea.getId())));

        add(new AjaxLink<Void>("recentActivity") {
            private static final long serialVersionUID = 6187425397578151838L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminIdeaActivityPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("callToAction") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminIdeaCallToActionEditPage.class, parameters);
            }
        });

        add(new AjaxLink<Void>("edit") {
            private static final long serialVersionUID = -3486178522694616314L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(IdeaAddEditPage.class,
                        new PageParameters().set("id", theIdea.getId()).set("campaignId", theIdea.getCampaign().getId()));
            }
        });

        add(new AjaxLink<Void>("participants") {
            private static final long serialVersionUID = 3452938390646078262L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                add(new AttributeModifier("class", "active"));
                setResponsePage(AdminIdeaParticipantsPage.class, parameters);
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

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "leadTab");
    }

    private void loadIdea(final StringValue id) {
        if (id != null && !id.isEmpty()) {
            try {
                theIdea = ideaService.getById(id.toOptionalLong());
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        }
    }
}

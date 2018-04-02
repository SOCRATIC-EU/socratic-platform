package de.atb.socratic.web.dashboard.panels;

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
import javax.inject.Inject;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.User;
import de.atb.socratic.service.implementation.ActivityService;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.components.resource.ProfilePictureResource;
import de.atb.socratic.web.dashboard.Dashboard.StateForDashboard;
import de.atb.socratic.web.dashboard.iLead.challenge.AdminActivityPage;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import de.atb.socratic.web.selection.SelectionPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ChallengePanel extends GenericPanel<Campaign> {

    /**
     *
     */
    private static final long serialVersionUID = -522989596052194414L;

    @Inject
    Logger logger;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    @EJB
    ActivityService activityService;

    private final Campaign challenge;

    /**
     * @param id
     * @param model
     */
    public ChallengePanel(final String id, final IModel<Campaign> model, final StateForDashboard dashboardState) {
        super(id, model);

        setOutputMarkupId(true);

        challenge = getModelObject();

        // add challenge data
        WebMarkupContainer headLine = new WebMarkupContainer("headline");
        add(headLine);
        headLine.add(new Label("name", new PropertyModel<String>(challenge, "name")));

        add(new NonCachingImage("profilePicture", ProfilePictureResource.get(PictureType.THUMBNAIL, challenge.getCreatedBy())));

        add(new Label("createdBy.nickName", new PropertyModel<String>(challenge, "createdBy.nickName")));
        add(new Label("createdBy.city", new PropertyModel<String>(challenge, "createdBy.city")));
        add(new Label("createdBy.country", new PropertyModel<String>(challenge, "createdBy.country")));

        Label contributors = new Label("contributors", activityService.countAllChallengeContributorsByCampaign(challenge));
        add(contributors.setOutputMarkupId(true));

        WebMarkupContainer wmc = new WebMarkupContainer("container") {
            private static final long serialVersionUID = -484431348509395229L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (StateForDashboard.TakePart == dashboardState) {
                    this.add(new AttributeModifier("class", "well"));
                }
            }
        };
        add(wmc);

        // add help text(if dashboardState == Participate)
        wmc.add(newHelpText(dashboardState));
        // add explore or manage button
        wmc.add(newButton(challenge, dashboardState));
    }

    private Label newHelpText(final StateForDashboard dashboardState) {
        return new Label("helpText", new PropertyModel<String>(challenge, "callToAction")) {
            private static final long serialVersionUID = 3779700606433655558L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (StateForDashboard.TakePart == dashboardState) {
                    setVisible(true);
                } else {
                    setVisible(false);
                }
            }
        };
    }

    private AjaxLink<?> newButton(final Campaign challenge, final StateForDashboard dashboardState) {
        return new AjaxLink<Void>("exploreOrManageButton") {
            private static final long serialVersionUID = -8233439456118623954L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (StateForDashboard.Lead == dashboardState && challenge.getCreatedBy().equals(loggedInUser)) {
                    setResponsePage(AdminActivityPage.class, new PageParameters().set("id", challenge.getId()));
                } else {
                    final Class<? extends IRequestablePage> targetPage = challenge.getDefinitionActive() != null
                            && challenge.getDefinitionActive() ? ChallengeDefinitionPage.class
                            : (challenge.getIdeationActive() != null && challenge.getIdeationActive() ? IdeasPage.class
                            : SelectionPage.class);
                    setResponsePage(targetPage, new PageParameters().set("id", challenge.getId()));
                }
            }

            @Override
            protected void onConfigure() {
                if (StateForDashboard.Lead == dashboardState && challenge.getCreatedBy().equals(loggedInUser)) {
                    this.add(new Label("buttonLabel", new StringResourceModel("challenges.manage.button", this, null)
                            .getString()));
                } else {
                    this.add(new Label("buttonLabel", new StringResourceModel("challenges.explore.button", this, null)
                            .getString()));
                }
                setOutputMarkupId(true);
            }

            ;

        };
    }
}

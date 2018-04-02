package de.atb.socratic.web.action.detail;

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

import java.io.File;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.User;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ActionIterationsListPage extends BasePage {

    private static final long serialVersionUID = 7378255719158818040L;

    @Inject
    Logger logger;

    @EJB
    ActionService actionService;

    private Action action;

    @Inject
    @LoggedInUser
    User loggedInUser;

    private DownloadLink methodologyDownloadLink;

    final CommonActionResourceHeaderPanel<Action> headerPanel;

    public ActionIterationsListPage(final PageParameters parameters) {
        super(parameters);

        final Long actionId = parameters.get("id").toOptionalLong();
        if (actionId != null) {
            action = actionService.getById(parameters.get("id").toOptionalLong());
        } else {
            action = null;
        }

        headerPanel = new CommonActionResourceHeaderPanel<Action>("commonHeaderPanel", Model.of(action), feedbackPanel) {
            private static final long serialVersionUID = 4494582353460389258L;

            @Override
            protected void onFollowersUpdate(AjaxRequestTarget target) {
            }
        };
        add(headerPanel);

        // download Prototyping Guidelines
        ServletContext servletContext = ((WebApplication) getApplication()).getServletContext();
        String pdfPath = servletContext.getRealPath("assets" + File.separator + "pdf" + File.separator);
        File methodology = new File(pdfPath + File.separator + "From_ideas_to_projects.pdf");
        methodologyDownloadLink = new DownloadLink("methodologyDownloadLink", methodology, "From_ideas_to_projects.pdf") {
            /**
             *
             */
            private static final long serialVersionUID = -4581595270070914054L;

            @Override
            public void onClick() {
                super.onClick();
                logger.info("The SOCRATIC Prototyping Guidelines was downloaded!");
            }
        };
        add(methodologyDownloadLink);

        add(addToolTipWebMarkupContainer("iterationHelpText", new StringResourceModel("iterationsText.help", this, null),
                TooltipConfig.Placement.right));

        // list of all iterations
        final ActionIterationsListPanel actionIterationsListPanel = new ActionIterationsListPanel("iterationsPanel", action,
                feedbackPanel) {
            private static final long serialVersionUID = 4494582353460389258L;
        };
        add(actionIterationsListPanel);

    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "actionsTab");
        headerPanel.activateCurrentActionTab(response, "iterationsTab");
    }

}

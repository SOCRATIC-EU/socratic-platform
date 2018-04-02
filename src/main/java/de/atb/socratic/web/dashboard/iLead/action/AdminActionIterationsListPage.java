/**
 *
 */
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

import java.util.List;

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.ActionIterationState;
import de.atb.socratic.web.ErrorPage;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * @author ATB
 */
public class AdminActionIterationsListPage extends AdminAction {
    private static final long serialVersionUID = -5559578453943490669L;

    private final AdminActionIterationsListPanel adminActionIterationsListPanel;

    private Action theAction;

    public AdminActionIterationsListPage(PageParameters parameters) {
        super(parameters);

        loadAction(parameters.get("id"));
        add(addToolTipWebMarkupContainer("iterationHelpText", new StringResourceModel("iterationsText.help", this, null),
                TooltipConfig.Placement.right));
        add(newCreateActionIterationLink(new PageParameters().set("id", theAction.getId()).set("iterationId", null)));

        adminActionIterationsListPanel = new AdminActionIterationsListPanel("iterationsList", Model.of(theAction),
                feedbackPanel) {

            private static final long serialVersionUID = 8087201252939474804L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
            }
        };
        add(adminActionIterationsListPanel.setOutputMarkupId(true));

    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentSideTab(response, "iterationsTab");
    }

    /**
     * @return
     */
    private BookmarkablePageLink<AdminActionIterationAddEditPage> newCreateActionIterationLink(PageParameters parameter) {
        final BookmarkablePageLink<AdminActionIterationAddEditPage> addLink = new BookmarkablePageLink<AdminActionIterationAddEditPage>(
                "addLink", AdminActionIterationAddEditPage.class, parameter) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                List<ActionIteration> iterations = actionService.getAllActionIterationsByDescendingCreationDate(
                        theAction.getId(), Integer.MAX_VALUE, Integer.MAX_VALUE);
                // if one the iterations of an action is still active, then do not allow to create a new iteration.
                for (ActionIteration iteration : iterations) {
                    if (iteration != null && iteration.getState().equals(ActionIterationState.Active)) {
                        this.setEnabled(false);
                        this.setVisible(false);
                        break;
                    } else {
                        this.setEnabled(true);
                        this.setVisible(true);
                    }
                }

            }
        };
        addLink.add(AttributeModifier.append("title", "Add new Iteration after finishing or putting current iteration on halt."));
        addLink.setOutputMarkupId(true);
        return addLink;
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

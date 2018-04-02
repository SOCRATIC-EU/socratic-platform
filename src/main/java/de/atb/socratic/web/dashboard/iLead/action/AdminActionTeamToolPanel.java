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

import java.util.LinkedList;
import java.util.List;

import javax.ejb.EJB;

import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.markup.html.bootstrap.components.TooltipConfig;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionTeamTool;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.ActionTeamToolService;
import de.atb.socratic.web.components.InputBorder;
import de.atb.socratic.web.components.StyledFeedbackPanel;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

public abstract class AdminActionTeamToolPanel extends GenericPanel<Action> {
    private static final long serialVersionUID = -257930933985282429L;
    // A feedback panel to show info and error messages
    private final StyledFeedbackPanel feedbackPanel;

    // action team tools
    private List<ActionTeamTool> teamToolsMap = new LinkedList<>();
    private ListView<ActionTeamTool> teamToolsListView;
    private final TextField<String> teamToolsDescriptionTextArea, teamToolsUrlTextArea;
    protected final InputBorder<String> teamToolsDescriptionValidationBorder;
    private String teamToolsDescription, teamToolsUrl;
    private final AjaxSubmitLink addLink;
    private final WebMarkupContainer teamToolContainer;

    @EJB
    ActionService actionService;

    @EJB
    ActionTeamToolService actionTeamToolService;

    final Action action;

    public AdminActionTeamToolPanel(final String id, final IModel<Action> model, final StyledFeedbackPanel feedbackPanel, final Form<Void> actionForm) {
        super(id, model);

        this.feedbackPanel = feedbackPanel;

        // get the campaigns participants
        action = getModelObject();
        if (!action.getActionTeamTools().isEmpty()) {
            teamToolsMap = action.getActionTeamTools();
        }

        // action team tools
        teamToolContainer = new WebMarkupContainer("teamTools");
        teamToolContainer.setOutputMarkupId(true);
        add(teamToolContainer);

        teamToolsUrlTextArea = new TextField<>("teamToolsUrl", new PropertyModel<String>(this, "teamToolsUrl"));
        teamToolsUrlTextArea.setOutputMarkupId(true);
        teamToolContainer.add(new InputBorder<>("teamToolsUrlValidationBorder", teamToolsUrlTextArea));

        teamToolsDescriptionTextArea = new TextField<>("teamToolsDescription", new PropertyModel<String>(this,
                "teamToolsDescription"));
        teamToolsDescriptionTextArea.setOutputMarkupId(true);
        teamToolContainer.add(teamToolsDescriptionValidationBorder = new InputBorder<>("teamToolsDescriptionValidationBorder",
                teamToolsDescriptionTextArea));
        teamToolContainer.add(addToolTipWebMarkupContainer("teamToolsHelpText", new StringResourceModel(
                "optionalFields.teamTools.desc.label", this, null), TooltipConfig.Placement.right));

        // Add team tool list view
        teamToolsListView = newListViewForTeamTools("allTeamTools", "toolDescriptionViewLabel", "actionRemove",
                Model.ofList(teamToolsMap));
        teamToolContainer.add(teamToolsListView.setOutputMarkupId(true));

        // add "add link" button
        addLink = newAjaxSubmitLinkToAddTeamTool("addTeamToolLink", actionForm);
        teamToolContainer.add(addLink);

    }

    public List<ActionTeamTool> getTeamToolsMap() {
        return teamToolsMap;
    }

    protected Component addToolTipWebMarkupContainer(final String wicketId, final IModel<String> textModel,
                                                     final TooltipConfig.Placement placement) {
        return new WebMarkupContainer(wicketId).setOutputMarkupPlaceholderTag(true).add(
                new TooltipBehavior(textModel, new TooltipConfig().withPlacement(placement)));
    }

    public ListView<ActionTeamTool> newListViewForTeamTools(final String wicketId, final String externalLinkWicketId,
                                                            final String removeLinkWicketId, final IModel<List<? extends ActionTeamTool>> listIModel) {
        return new ListView<ActionTeamTool>(wicketId, listIModel) {
            private static final long serialVersionUID = -6208493438326553258L;

            @Override
            protected void populateItem(final ListItem<ActionTeamTool> item) {
                final ActionTeamTool key = item.getModelObject();
                item.add(new ExternalLink(externalLinkWicketId, key.getUrl(), key.getToolName()));
                item.add(new AjaxLink<Void>(removeLinkWicketId) {
                    private static final long serialVersionUID = 938782818114991296L;

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        // remove action team tool
                        teamToolsMap.remove(key);

                        // for each action, team tools will be unique set of entities saved in database. Thus, if Action leader
                        // removes that tool from action then delete it from DB as well. In future if we link one tool to
                        // multiple actions then we need to change delete method here.
                        if (key.getId() != null) {
                            actionTeamToolService.delete(key.getId());
                        }
                        target.add(teamToolContainer);
                    }
                });
            }
        };
    }

    public AjaxSubmitLink newAjaxSubmitLinkToAddTeamTool(String wicketId, Form<Void> form) {
        return new AjaxSubmitLink(wicketId, form) {
            private static final long serialVersionUID = 7928112586362700857L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

                if (teamToolsDescriptionTextArea.getModelObject() != null
                        && !teamToolsDescriptionTextArea.getModelObject().isEmpty()
                        && teamToolsUrlTextArea.getModelObject() != null && !teamToolsUrlTextArea.getModelObject().isEmpty()) {
                    // map will hold tool description as key and tool url as value
                    ActionTeamTool tool = new ActionTeamTool();
                    tool.setToolName(teamToolsDescriptionTextArea.getModelObject());
                    tool.setUrl(teamToolsUrlTextArea.getModelObject());
                    // save team tool to DB
                    tool = actionTeamToolService.createOrUpdate(tool);
                    teamToolsMap.add(tool);
                }

                if (teamToolsDescriptionTextArea.getModelObject() == null
                        || teamToolsDescriptionTextArea.getModelObject().isEmpty()) {
                    teamToolsDescriptionTextArea.error(new StringResourceModel("optionalFields.teamToolsDescription.error",
                            this, null).getString());
                    target.add(feedbackPanel);
                }

                if (teamToolsUrlTextArea.getModelObject() == null || teamToolsUrlTextArea.getModelObject().isEmpty()) {
                    teamToolsUrlTextArea.error(new StringResourceModel("optionalFields.teamToolsurl.error", this, null)
                            .getString());
                    target.add(feedbackPanel);
                }

                teamToolsUrlTextArea.setDefaultModelObject(null);
                teamToolsDescriptionTextArea.setDefaultModelObject(null);
                target.add(teamToolsUrlTextArea);
                target.add(teamToolsDescriptionTextArea);
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };
    }
}

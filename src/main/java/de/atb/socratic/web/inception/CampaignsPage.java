package de.atb.socratic.web.inception;

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

import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.EFFSession;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class CampaignsPage extends BasePage {

    private static final long serialVersionUID = -5697250926331599304L;

    private final DropDownChoice<SortingCriteria> sortingCriteriaChoice;

    EFFSession session = (EFFSession) EFFSession.get();

    // panel for displaying list of existing campaigns
    private final CampaignListPanel campaignListPanel;

    /**
     * Constructor building the page
     *
     * @param parameters
     */
    public CampaignsPage(final PageParameters parameters) {

        super(parameters);

        // Add sorting form
        Form<Void> sortingCriteriaForm = new Form<Void>("sortingCriteriaForm");
        add(sortingCriteriaForm);
        sortingCriteriaForm.add(sortingCriteriaChoice = newSortingCriteriaDropDownChoice());
        // add panel with list of campaigns
        campaignListPanel = new CampaignListPanel("campaignList", feedbackPanel);
        add(campaignListPanel.setOutputMarkupId(true));
    }

    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        activateCurrentTab(response, "challengesTab");
    }

    /**
     * @return
     */
    private DropDownChoice<SortingCriteria> newSortingCriteriaDropDownChoice() {
        final DropDownChoice<SortingCriteria> sortingCriteriaChoice = new DropDownChoice<>(
                "sortingCriteriaChoice",
                new Model<SortingCriteria>(),
                Arrays.asList(SortingCriteria.values()),
                new IChoiceRenderer<SortingCriteria>() {
                    private static final long serialVersionUID = -3507943582789662873L;

                    @Override
                    public Object getDisplayValue(SortingCriteria object) {
                        return new StringResourceModel(object.getNameKey(), CampaignsPage.this, null).getString();
                    }

                    @Override
                    public String getIdValue(SortingCriteria object, int index) {
                        return String.valueOf(index);
                    }
                });

        // always include a "Please choose" option
        sortingCriteriaChoice.setNullValid(false);
        sortingCriteriaChoice.setDefaultModelObject(SortingCriteria.Date);
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
        target.add(campaignListPanel.setSortingCriteria(sortingCriteriaChoice.getModelObject()));
    }

    /*
     * (non-Javadoc)
     *
     * @see de.atb.eff.web.BasePage#getPageTitleModel()
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

    public enum SortingCriteria {
        Date("sort.date"),
        DefinitionPhase("sort.definition-phase"),
        IdeationPhase("sort.ideation-phase"),
        SelectionPhase("sort.selection-phase");

        private final String nameKey;

        SortingCriteria(final String nameKey) {
            this.nameKey = nameKey;
        }

        public String getNameKey() {
            return nameKey;
        }
    }
}

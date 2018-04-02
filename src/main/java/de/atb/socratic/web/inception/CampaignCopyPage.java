/**
 *
 */
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

import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.web.ErrorPage;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

/**
 * @author ATB
 */
public class CampaignCopyPage extends CampaignAddEditPage {

    /**
     *
     */
    private static final long serialVersionUID = 8808216947509905034L;

    private Campaign originalCampaign;

    /**
     * @param parameters
     */
    public CampaignCopyPage(final PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected void onAfterRender() {
        super.onAfterRender();
        campaignService.detach(originalCampaign);
    }

    /*
     * (non-Javadoc)
     * @see de.atb.eff.web.inception.CampaignAddEditPage#loadCampaign(org.apache.wicket.util.string.StringValue)
     */
    @Override
    protected void loadCampaign(StringValue idParam) {
        if (!idParam.isEmpty()) {
            try {
                // set the campaign we got from previous page
                originalCampaign = campaignService.getById(idParam.toOptionalLong());
                // set page info
                setPageTitle(new StringResourceModel("page.copy.title", this, null));
                add(new Label("header", new StringResourceModel("form.copy.header",
                        this, Model.of(originalCampaign))));
                // copy the original campaign to the new campaign
                copyFromOriginalCampaign();
            } catch (Exception e) {
                throw new RestartResponseException(ErrorPage.class);
            }
        } else {
            throw new RestartResponseException(ErrorPage.class);
        }
    }

    /*
     * (non-Javadoc)
     * @see de.atb.eff.web.inception.CampaignAddEditPage#save(org.apache.wicket.ajax.AjaxRequestTarget)
     */
    @Override
    protected void save(AjaxRequestTarget target) {
        if (getTheCampaign().getName().equals(originalCampaign.getName())) {
            // if the name has NOT been changed --> show error message
            getPage().error("You have to provide a different name than the original campaign");
            target.add(feedbackPanel);
            nameValidationBorder.add(new CssClassNameAppender(Model.of("error")) {
                private static final long serialVersionUID = -2217923560506054681L;

                @Override
                public boolean isTemporary(Component component) {
                    return true;
                }
            });
            showErrors(target);
        } else {
            // if it has been changed --> proceed
            super.save(target);
        }
    }

    /**
     *
     */
    private void copyFromOriginalCampaign() {
        setTheCampaign(new Campaign());
        getTheCampaign().setName(originalCampaign.getName());
        getTheCampaign().setDescription(originalCampaign.getDescription());
        getTheCampaign().setInnovationObjective(originalCampaign.getInnovationObjective());
        getTheCampaign().setDueDate(originalCampaign.getDueDate());
        getTheCampaign().setActive(originalCampaign.getActive());
        getTheCampaign().setTags(originalCampaign.getTags());

        // what about the scope?!
    }
}

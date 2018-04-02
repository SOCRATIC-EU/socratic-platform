package de.atb.socratic.web.inception.campaign;

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

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.InnovationStatus;
import de.atb.socratic.web.inception.CampaignAddEditPage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public abstract class CampaignActionsPanel extends Panel {

    private final Campaign campaignToHandle;

    public CampaignActionsPanel(final String id, IModel<Campaign> model, final InnovationStatus desiredStatus) {
        super(id, model);

        campaignToHandle = model.getObject();

        // add button to show AddEditPage
        add(new BookmarkablePageLink<CampaignAddEditPage>("edit", CampaignAddEditPage.class, forCampaign(campaignToHandle)));

        add(newStartLink(campaignToHandle, desiredStatus));
        add(newStopLink(campaignToHandle, desiredStatus));
    }

    private static PageParameters forCampaign(Campaign campaign) {
        return new PageParameters().set("id", campaign.getId());
    }

    public abstract void stopClicked(AjaxRequestTarget target, Campaign campaignToHandle);

    public abstract void startClicked(AjaxRequestTarget target, Campaign campaignToHandle);

    private AjaxLink<Void> newStopLink(final Campaign campaign, final InnovationStatus desiredStatus) {
        AjaxLink<Void> stop = new AjaxLink<Void>("stopLink") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(desiredStatus.equals(campaign.getInnovationStatus()) && isCurrentStatusActive(campaign));
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                stopClicked(target, campaign);
            }
        };
        return stop;
    }

    private AjaxLink<Void> newStartLink(final Campaign campaign, final InnovationStatus desiredStatus) {
        AjaxLink<Void> start = new AjaxLink<Void>("startLink") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(false);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                startClicked(target, campaign);
            }
        };
        return start;
    }

    private boolean isCurrentStatusActive(final Campaign campaign) {
        if (InnovationStatus.DEFINITION.equals(campaign.getInnovationStatus())) {
            return campaign.getDefinitionActive() != null && campaign.getDefinitionActive();
        } else if (InnovationStatus.INCEPTION.equals(campaign.getInnovationStatus())) {
            return campaign.getIdeationActive() != null && campaign.getIdeationActive();
        } else if (InnovationStatus.PRIORITISATION.equals(campaign.getInnovationStatus())) {
            return campaign.getSelectionActive() != null && campaign.getSelectionActive();
        } else {
            return false;
        }
    }

}

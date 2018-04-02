package de.atb.socratic.web.components.navbar.notifications;

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
import de.atb.socratic.model.notification.UpdateCampaignImplementationNotification;
import de.atb.socratic.web.action.detail.ActionsListPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class UpdateCampaignImplementationNotificationLink extends
        NotificationBookmarkablePageLink<UpdateCampaignImplementationNotification> {

    private static final long serialVersionUID = 6341070512879577697L;

    public UpdateCampaignImplementationNotificationLink(String id, IModel<UpdateCampaignImplementationNotification> model) {
        this(id, new PageParameters(), model);
    }

    public UpdateCampaignImplementationNotificationLink(String id, PageParameters parameters,
                                                        IModel<UpdateCampaignImplementationNotification> model) {
        super(id, ActionsListPage.class, (parameters == null ? new PageParameters().add("id", model.getObject().getCampaign()
                .getId()) : parameters.add("id", model.getObject().getCampaign().getId())), model);

        add(new Label("campaignName", getCampaign().getName())); // get Campaign name
        // set different messages based on Campaign Innovation Status
        Label messageTextLabel = new Label("campaignState", new StringResourceModel("campaign.implementation.advance", this,
                null));
        add(messageTextLabel);
    }

    public Campaign getCampaign() {
        return getModelObject() != null ? getModelObject().getCampaign() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
        return new PanelMarkupSourcingStrategy(true);
    }
}

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
import de.atb.socratic.model.notification.ParticipateInCampaignNotification;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.definition.challenge.ChallengeDefinitionPage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * ParticipateInCampaignNotificationLink
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class ParticipateInCampaignNotificationLink extends NotificationBookmarkablePageLink<ParticipateInCampaignNotification> {

    /**
     *
     */
    private static final long serialVersionUID = 6277856095704051161L;


    public ParticipateInCampaignNotificationLink(String id, IModel<ParticipateInCampaignNotification> model) {
        this(id, new PageParameters(), model);
    }

    public ParticipateInCampaignNotificationLink(String id,
                                                 PageParameters parameters,
                                                 IModel<ParticipateInCampaignNotification> model) {
        super(
                id,
                getTargetPageClass(model.getObject().getCampaign()),
                (parameters == null
                        ? new PageParameters().add("id", model.getObject().getCampaign().getId())
                        : parameters.add("id", model.getObject().getCampaign().getId())), model
        );
        add(new Label("campaignCreator", getCampaign().getCreatedBy().getNickName()));
        add(new Label("campaignName", getCampaign().getName()));
    }

    /**
     * This method should only return phase based on challenge creation. It should either return Definition or Ideation. Once
     * challenge has been created and moved to next stages method should not change challenge phase.
     *
     * @param campaign
     * @return
     */
    private static Class<? extends BasePage> getTargetPageClass(Campaign campaign) {
        if (campaign.getOpenForDiscussion()) {
            return ChallengeDefinitionPage.class;
        } else {
            return IdeasPage.class;
        }
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

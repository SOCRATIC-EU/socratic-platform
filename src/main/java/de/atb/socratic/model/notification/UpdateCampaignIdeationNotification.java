package de.atb.socratic.model.notification;

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

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Query;
import javax.xml.bind.annotation.XmlRootElement;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.web.components.navbar.notifications.NotificationBookmarkablePageLink;
import de.atb.socratic.web.components.navbar.notifications.UpdateCampaignIdeationNotificationLink;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@Entity
@XmlRootElement
// required as discrimator type is varchar(31) (to short!)
@DiscriminatorValue(value = "UCIN")
public class UpdateCampaignIdeationNotification extends Notification {

    private static final long serialVersionUID = 3448302761918945194L;

    public UpdateCampaignIdeationNotification() {
        this.setNotificationType(NotificationType.CAMPAIGN_UPDATE_IDEATION);
    }

    /**
     * @return an campaign
     */
    public Campaign getCampaign() {
        return campaign;
    }

    /**
     * @param campaign an campaign to set
     */
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N extends Notification, P extends Page, T extends NotificationBookmarkablePageLink<N>> T getLink(String id,
                                                                                                             IModel<N> model) {
        return (T) new UpdateCampaignIdeationNotificationLink(id, (IModel<UpdateCampaignIdeationNotification>) model);

    }

    @SuppressWarnings("unchecked")
    @Override
    public <N extends Notification, P extends Page, T extends NotificationBookmarkablePageLink<N>> T getLink(String id,
                                                                                                             PageParameters parameters, IModel<N> model) {
        return (T) new UpdateCampaignIdeationNotificationLink(id, (IModel<UpdateCampaignIdeationNotification>) model);
    }

    @Override
    public void setParameters(Query q) {
        q.setParameter("obj", getCampaign());
        q.setParameter("dType", "UCIN"); // this should match @DiscriminatorValue on top of this class
    }

    @Override
    public String getColumnName() {
        return "campaign";
    }

}

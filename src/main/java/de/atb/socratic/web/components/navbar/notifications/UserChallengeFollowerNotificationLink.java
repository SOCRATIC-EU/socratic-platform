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
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.notification.UserChallengeFollowerNotification;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * This class is used for notification of user following Challenge.
 *
 * @author ATB
 */
public class UserChallengeFollowerNotificationLink extends NotificationBookmarkablePageLink<UserChallengeFollowerNotification> {

    private static final long serialVersionUID = -6847965169284935767L;

    public UserChallengeFollowerNotificationLink(String id, IModel<UserChallengeFollowerNotification> model) {
        this(id, new PageParameters(), model);
    }

    public UserChallengeFollowerNotificationLink(String id, PageParameters parameters, IModel<UserChallengeFollowerNotification> model) {
        super(id, getTargetPageClassForCampaign(model.getObject().getCampaign()), forCampaign(model.getObject().getCampaign()), model);

        add(new Label("challengeName", getCampaign().getName()));
        if (model.getObject().getNotificationType().equals(NotificationType.CAMPAIGN_FOLLOWED)) {
            add(new Label("challenge.interest", new StringResourceModel("challenge.interest.comment", this, null)));
        } else if (model.getObject().getNotificationType().equals(NotificationType.IDEA_CREATED)) {
            add(new Label("challenge.interest", new StringResourceModel("challenge.interest.ideaPosted", this, null)));
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

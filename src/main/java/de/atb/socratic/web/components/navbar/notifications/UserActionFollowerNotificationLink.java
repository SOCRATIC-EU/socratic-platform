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

import de.atb.socratic.model.Action;
import de.atb.socratic.model.notification.UserActionFollowerNotification;
import de.atb.socratic.web.action.detail.ActionSolutionPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * This class is used for notification of user following Action.
 *
 * @author ATB
 */
public class UserActionFollowerNotificationLink extends NotificationBookmarkablePageLink<UserActionFollowerNotification> {

    private static final long serialVersionUID = -6847965169284935767L;

    public UserActionFollowerNotificationLink(String id, IModel<UserActionFollowerNotification> model) {
        this(id, new PageParameters(), model);
    }

    public UserActionFollowerNotificationLink(String id, PageParameters parameters, IModel<UserActionFollowerNotification> model) {
        super(id, ActionSolutionPage.class, new PageParameters().set("id", model.getObject().getAction().getId()), model);

        add(new Label("actionShortText", getAction().getShortText()));

    }

    public Action getAction() {
        return getModelObject() != null ? getModelObject().getAction() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
        return new PanelMarkupSourcingStrategy(true);
    }
}

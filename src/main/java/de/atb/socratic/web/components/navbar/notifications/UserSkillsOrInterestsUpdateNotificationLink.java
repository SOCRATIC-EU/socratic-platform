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

import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.UserSkillsOrInterestsUpdateNotification;
import de.atb.socratic.web.profile.EditUserPage;
import org.apache.wicket.markup.html.panel.IMarkupSourcingStrategy;
import org.apache.wicket.markup.html.panel.PanelMarkupSourcingStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author ATB
 */
public class UserSkillsOrInterestsUpdateNotificationLink extends NotificationBookmarkablePageLink<UserSkillsOrInterestsUpdateNotification> {

    private static final long serialVersionUID = -3687349718251374845L;

    public UserSkillsOrInterestsUpdateNotificationLink(String id, IModel<UserSkillsOrInterestsUpdateNotification> model) {
        this(id, new PageParameters(), model);
    }

    public UserSkillsOrInterestsUpdateNotificationLink(String id, PageParameters parameters, IModel<UserSkillsOrInterestsUpdateNotification> model) {
        super(id, EditUserPage.class, new PageParameters().set("id", model.getObject().getUser().getId()), model);
    }

    public User getUser() {
        return getModelObject() != null ? getModelObject().getUser() : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IMarkupSourcingStrategy newMarkupSourcingStrategy() {
        return new PanelMarkupSourcingStrategy(true);
    }
}

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

import de.atb.socratic.model.User;
import de.atb.socratic.web.components.navbar.notifications.NotificationBookmarkablePageLink;
import de.atb.socratic.web.components.navbar.notifications.ShowAllNotificationLink;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@Entity
@XmlRootElement
// required as discrimator type is varchar(31) (to short!)
@DiscriminatorValue(value = "SA")
public class ShowAllNotification extends Notification {

    private static final long serialVersionUID = 3448302761918945194L;

    public ShowAllNotification() {
        this.setNotificationType(NotificationType.SHOW_ALL);
    }

    /**
     * @return an action
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user an action to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N extends Notification, P extends Page, T extends NotificationBookmarkablePageLink<N>> T getLink(String id,
                                                                                                             IModel<N> model) {
        return (T) new ShowAllNotificationLink(id, (IModel<ShowAllNotification>) model);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <N extends Notification, P extends Page, T extends NotificationBookmarkablePageLink<N>> T getLink(String id,
                                                                                                             PageParameters parameters, IModel<N> model) {
        return (T) new ShowAllNotificationLink(id, (IModel<ShowAllNotification>) model);
    }

    @Override
    public void setParameters(Query q) {
        q.setParameter("obj", getUser());
        q.setParameter("dType", "SA");
    }

    @Override
    public String getColumnName() {
        return "user";
    }

}

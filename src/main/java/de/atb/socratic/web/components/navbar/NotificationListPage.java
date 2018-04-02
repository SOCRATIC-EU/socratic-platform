package de.atb.socratic.web.components.navbar;

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

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import de.agilecoders.wicket.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;
import de.atb.socratic.model.User;
import de.atb.socratic.model.notification.Notification;
import de.atb.socratic.service.notification.NotificationService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.navbar.notifications.NotificationBookmarkablePageLink;
import de.atb.socratic.web.provider.EntityProvider;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class NotificationListPage extends BasePage {

    private static final long serialVersionUID = 1L;

    // how many Notifications do we show per page
    private static final int itemsPerPage = 10;

    // container holding the list of Notifications
    private final WebMarkupContainer notificationsContainer;

    private NotificationProvider notificationProvider;

    // Repeating view showing the list of existing Notifications
    private final DataView<Notification> notificationsRepeater;

    @Inject
    @Named
    NotificationService notificationService;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    public NotificationListPage(PageParameters parameters) {
        super(parameters);

        // add container with list of existing Notifications
        notificationsContainer = new WebMarkupContainer("notificationsContainer");
        notificationsContainer.setOutputMarkupId(true);
        add(notificationsContainer);

        // add repeating view with list of existing Notifications
        notificationProvider = new NotificationProvider();
        notificationsRepeater = new DataView<Notification>("notifications", notificationProvider, itemsPerPage) {

            private static final long serialVersionUID = -8282367075859241719L;

            @Override
            protected void populateItem(Item<Notification> item) {
                item.setOutputMarkupId(true);
                NotificationListPage.this.populateItem(item, item.getModelObject());
            }
        };
        notificationsContainer.add(notificationsRepeater);

        add(new BootstrapAjaxPagingNavigator("pagination", notificationsRepeater) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(notificationsRepeater.getPageCount() > 1);
            }
        });
    }

    protected void populateItem(final WebMarkupContainer item, final Notification notification) {
        item.setOutputMarkupId(true);

        // link
        final NotificationBookmarkablePageLink<Notification> link = notification.getLink("link", Model.of(notification));
        link.add(new AjaxEventBehavior("onclick") {

            private static final long serialVersionUID = 4672742723274611057L;

            @Override
            protected void onEvent(AjaxRequestTarget target) {
                notificationService.setAsReaded(link.getModelObject(), loggedInUserProvider.getLoggedInUser());
                setResponsePage(link.getPageClass(), link.getPageParameters());
                target.add(link);
            }
        });
        item.add(link);
    }

    /**
     * @author ATB
     */
    private final class NotificationProvider extends EntityProvider<Notification> {

        private static final long serialVersionUID = 1360608566900210699L;

        @Override
        public Iterator<? extends Notification> iterator(long first, long count) {

            List<Notification> allTopicCampaigns = notificationService.getAllNotificationsForGivenUser(loggedInUser, Long
                    .valueOf(first).intValue(), Long.valueOf(count).intValue());

            return allTopicCampaigns.iterator();
        }

        @Override
        public long size() {
            return notificationService.countAllNotificationsForGivenUser(loggedInUser);
        }
    }
}

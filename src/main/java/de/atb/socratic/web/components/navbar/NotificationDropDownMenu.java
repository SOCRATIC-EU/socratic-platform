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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;

import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonList;
import de.agilecoders.wicket.markup.html.bootstrap.button.dropdown.DropDownButton;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarDropDownButton;
import de.atb.socratic.model.notification.ActionTeamMemberInvitationNotification;
import de.atb.socratic.model.notification.Notification;
import de.atb.socratic.model.notification.ShowAllNotification;
import de.atb.socratic.service.inception.ActionService;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.inception.ScopeService;
import de.atb.socratic.service.notification.NotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.components.CSSAppender;
import de.atb.socratic.web.components.navbar.notifications.NotificationBookmarkablePageLink;
import de.atb.socratic.web.provider.LoggedInUserProvider;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.Model;

/**
 * NotificationDropDownMenu
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class NotificationDropDownMenu extends NavbarDropDownButton {

    @Inject
    @Named
    NotificationService notificationService;

    @Inject
    CampaignService campaignService;

    @Inject
    IdeaService ideaService;

    @Inject
    UserService userService;

    @Inject
    ScopeService scopeService;

    @EJB
    ActionService actionService;

    @Inject
    protected LoggedInUserProvider loggedInUserProvider;

    private NotificationBookmarkablePageLink<ShowAllNotification> showAllLink;

    private static final long serialVersionUID = -3347766247612574717L;
    private final int LIMIT = 6;

    public NotificationDropDownMenu() {
        super(Model.of("0"));
        setOutputMarkupId(true);
        setIconType(IconType.globe);
    }

    @Override
    protected void onConfigure() {
        //Clear previous notification using reflection
        try {
            Field field = DropDownButton.class.getDeclaredField("buttonListView");
            field.setAccessible(true);
            ButtonList value = (ButtonList) field.get(this);
            value.getList().clear();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        List<Notification> notifications = notificationService.getAllNotificationsForUser(loggedInUserProvider.getLoggedInUser());

        int countNew = checkNewNotifications(notifications);
        if (countNew > 0) {
            this.getBaseButton().add(CSSAppender.append("class", "blue"));
        }
        this.getBaseButton().get("label").setDefaultModelObject(String.valueOf(countNew));
        Collections.sort(notifications);
        int index = 0;
        for (final Notification notification : notifications) {
            if (index >= LIMIT) {
                break;
            } else {
                index++;
            }
            final NotificationBookmarkablePageLink<Notification> link =
                    notification.getLink(ButtonList.getButtonMarkupId(), Model.of(notification));
            link.add(new AjaxEventBehavior("onclick") {

                private static final long serialVersionUID = 4672742723274611057L;

                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    if (notification instanceof ActionTeamMemberInvitationNotification) {
                        actionService.addTeamMemberToList(((ActionTeamMemberInvitationNotification) notification).getAction(), notification.getUser());
                    }
                    notificationService.setAsReaded(link.getModelObject(), loggedInUserProvider.getLoggedInUser());
                    setResponsePage(link.getPageClass(), link.getPageParameters());
                }
            });
            addButton(link);
        }

        ShowAllNotification showAllNotification = new ShowAllNotification();
        showAllNotification.setUser(loggedInUserProvider.getLoggedInUser());
        showAllLink = showAllNotification.getLink(ButtonList.getButtonMarkupId(), Model.of(showAllNotification));
        showAllLink.add(new AjaxEventBehavior("onclick") {

            private static final long serialVersionUID = 4672742723274611057L;

            @Override
            protected void onEvent(AjaxRequestTarget target) {
                notificationService.setAsReaded(showAllLink.getModelObject(), loggedInUserProvider.getLoggedInUser());
                setResponsePage(showAllLink.getPageClass(), showAllLink.getPageParameters());
            }
        });
        addButton(showAllLink);
    }

    private int checkNewNotifications(List<Notification> notif) {
        int nNotif = 0;
        for (Notification n : notif) {
            if (n.getReadDate() == null) {
                nNotif++;
            }
        }
        return nNotif;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarDropDownButton
     * #onInitialize()
     */
    @Override
    protected void onInitialize() {
        super.onInitialize();
        this.setRenderBodyOnly(true);
    }

}

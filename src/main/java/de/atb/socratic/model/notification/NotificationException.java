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

import java.util.EnumSet;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import de.atb.socratic.model.AbstractEntity;
import de.atb.socratic.model.User;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * NotificationException
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Entity
@XmlRootElement
@Table(name = "notification_exceptions")
public class NotificationException extends AbstractEntity {

    private static final long serialVersionUID = -4550444026934494086L;

    @NotNull
    @ManyToOne
    @IndexedEmbedded
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @JoinTable(name = "notexc_discardedmethods")
    private Set<NotificationMethod> discardedMethods = EnumSet.allOf(NotificationMethod.class);

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the discardedMethods
     */
    public Set<NotificationMethod> getDiscardedMethods() {
        return discardedMethods;
    }

    /**
     * @param discardedMethods the discardedMethods to set
     */
    public void setDiscardedMethods(Set<NotificationMethod> discardedMethods) {
        this.discardedMethods = discardedMethods;
    }

    /**
     * @return the ntoficationType
     */
    public NotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * @param notificationType the ntoficationType to set
     */
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

}

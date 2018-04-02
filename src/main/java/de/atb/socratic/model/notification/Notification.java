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

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import de.atb.socratic.model.AbstractEntity;
import de.atb.socratic.model.Action;
import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.web.components.navbar.notifications.NotificationBookmarkablePageLink;
import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.search.annotations.IndexedEmbedded;

/**
 * Notification
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Entity
@XmlRootElement
@Table(name = "notifications")
public abstract class Notification extends AbstractEntity implements Comparable<Notification>, IParametrized {

    private static final long serialVersionUID = 4381307459206542113L;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    protected Date creationDate = new Date();

    @NotNull
    @Enumerated(EnumType.ORDINAL)
    protected NotificationType notificationType = NotificationType.UNDEFINED;

    @IndexedEmbedded
    @ManyToOne(targetEntity = Idea.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    //@JoinColumn(name="idea_id")
    protected Idea idea;

    @IndexedEmbedded
    @ManyToOne(targetEntity = Action.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    protected Action action;

    @IndexedEmbedded
    @ManyToOne(targetEntity = ActionIteration.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    protected ActionIteration actionIteration;

    @IndexedEmbedded
    @ManyToOne(targetEntity = Campaign.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    //@JoinColumn(name="campaign_id")
    protected Campaign campaign;

    @IndexedEmbedded
    @ManyToOne(targetEntity = User.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    //@JoinColumn(name="user_id")
    protected User user;

    @Temporal(TemporalType.TIMESTAMP)
    protected Date readDate;


    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * @return the notificationType
     */
    public NotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * @param notificationType the notificationType to set
     */
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

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
     * @return the readDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * @param readDate the readDate to set
     */
    public void setReadDate(Date readDate) {
        this.readDate = readDate;
    }

    public abstract <N extends Notification, P extends Page, T extends NotificationBookmarkablePageLink<N>> T getLink(String id,
                                                                                                                      IModel<N> model);

    public abstract <N extends Notification, P extends Page, T extends NotificationBookmarkablePageLink<N>> T getLink(final String id,
                                                                                                                      final PageParameters parameters, IModel<N> model);

    @Override
    public int compareTo(Notification o) {
        return o.getCreationDate().compareTo(getCreationDate());
    }

    @Override
    public String toString() {
        return "Noptification [ id = " + getId() + " , creationDate = " + getCreationDate() + " , readDate = " + getReadDate() + " , user = " + getUser() + " , notificationType = " + getNotificationType() + " ]";
    }

}

/**
 *
 */
package de.atb.socratic.model;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "messages")
@Indexed
public class Message extends AbstractEntity {

    private static final long serialVersionUID = 1670579040501182598L;

    @NotNull
    @Size(min = 3, max = 140)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String subject;

    @NotNull
    @Size(min = 3, max = 500)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String messageText;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date postedAt;

    @NotNull
    @ManyToOne
    @IndexedEmbedded
    private User sender;

    private boolean hasSenderRemovedMessage = false;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "message_receivers")
    private List<User> receivers = new ArrayList<User>();

    // this list will hold people who still did not read the message once user reads message he/she will be removed from the
    // list
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "read_messages")
    private List<User> isRedByUser = new LinkedList<>();

    // once user deletes message, he7she will be removed from the list, this does not include sender
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "delete_messages")
    private List<User> isDeletedByReceiver = new LinkedList<>();

    /**
     *
     */
    public Message() {
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public Date getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Date postedAt) {
        this.postedAt = postedAt;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public List<User> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<User> receivers) {
        this.receivers = receivers;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isHasSenderRemovedMessage() {
        return hasSenderRemovedMessage;
    }

    public void setHasSenderRemovedMessage(boolean hasSenderRemovedMessage) {
        this.hasSenderRemovedMessage = hasSenderRemovedMessage;
    }

    public List<User> getIsRedByUser() {
        return isRedByUser;
    }

    public void setIsRedByUser(List<User> isRedByUser) {
        this.isRedByUser = isRedByUser;
    }

    public List<User> getIsDeletedByReceiver() {
        return isDeletedByReceiver;
    }

    public void setIsDeletedByReceiver(List<User> isDeletedByReceiver) {
        this.isDeletedByReceiver = isDeletedByReceiver;
    }

    @Override
    public String toString() {
        return "Message [subject=" + subject + ", messageText=" + messageText + ", postedAt=" + postedAt
                + ", sender=" + sender + ", hasSenderRemovedMessage=" + hasSenderRemovedMessage + ", receivers=" + receivers + "]";
    }
}

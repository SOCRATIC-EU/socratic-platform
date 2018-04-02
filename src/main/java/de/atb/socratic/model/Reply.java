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

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
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
@Table(name = "replies")
@Indexed
public class Reply extends AbstractEntity {

    private static final long serialVersionUID = -6801303445273937097L;

    @NotNull
    @Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String replyText;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date postedAt;

    @NotNull
    @ManyToOne(targetEntity = User.class, cascade = CascadeType.ALL)
    @IndexedEmbedded
    private User postedBy;

    @NotNull
    @ManyToOne(targetEntity = Comment.class, cascade = CascadeType.ALL)
    @IndexedEmbedded
    private Comment comment;

    public Reply() {
    }

    public String getReplyText() {
        return replyText;
    }

    /**
     * @param replyText the replyText to set
     */
    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public Comment getComment() {
        return comment;
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    /**
     * @return the postedAt
     */
    public Date getPostedAt() {
        return postedAt;
    }

    /**
     * @param postedAt the postedAt to set
     */
    public void setPostedAt(Date postedAt) {
        this.postedAt = postedAt;
    }

    /**
     * @return the postedBy
     */
    public User getPostedBy() {
        return postedBy;
    }

    /**
     * @param postedBy the postedBy to set
     */
    public void setPostedBy(User postedBy) {
        this.postedBy = postedBy;
    }

    @Override
    public String toString() {
        return "Reply [replyText=" + replyText + ", postedAt=" + postedAt
                + ", postedBy=" + postedBy + ", getId()=" + getId() + "]";
    }

}

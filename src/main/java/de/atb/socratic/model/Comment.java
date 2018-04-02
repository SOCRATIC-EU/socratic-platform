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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "comments")
@Indexed
public class Comment extends AbstractEntity {

    private static final long serialVersionUID = -2376133726917649715L;

    @NotNull
    @Size(min = 3, max = 10240)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String commentText;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date postedAt;

    @NotNull
    @ManyToOne
    @IndexedEmbedded
    private User postedBy;

    @OneToMany(targetEntity = Reply.class, mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reply> replies = new ArrayList<Reply>();

    @Enumerated(EnumType.STRING)
    private InnovationStatus innovationStatus;

    /**
     *
     */
    public Comment() {
    }

    /**
     * @return the commentText
     */
    public String getCommentText() {
        if (commentText == null) {
            return "";
        }
        return commentText;
    }

    /**
     * @param commentText the commentText to set
     */
    public void setCommentText(String commentText) {
        this.commentText = commentText;
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

    /**
     * @return the innovationStatus
     */
    public InnovationStatus getInnovationStatus() {
        return innovationStatus;
    }

    /**
     * @param innovationStatus the innovationStatus to set
     */
    public void setInnovationStatus(InnovationStatus innovationStatus) {
        this.innovationStatus = innovationStatus;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    /**
     * @param reply
     */
    public void addReply(Reply reply) {
        this.replies.add(reply);
    }

    /**
     * @param reply
     */
    public void removeReply(Reply reply) {
        this.replies.remove(reply);
    }

    public Integer getRepliesCount() {
        return replies.size();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Comment [commentText=" + commentText + ", postedAt=" + postedAt
                + ", postedBy=" + postedBy + ", innovationStatus="
                + innovationStatus + ", getId()=" + getId() + "]";
    }

}

package de.atb.socratic.model.votes;

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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import de.atb.socratic.model.AbstractEntity;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;

@Entity
@XmlRootElement
@Table(name = "prioritisation_votes_dot")
public class PrioritisationDotSplitVotes extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = -509640088180516725L;

    @OneToOne(targetEntity = Votes.class, cascade = CascadeType.ALL)
    private Votes vote;

    @ManyToOne(targetEntity = Idea.class, cascade = CascadeType.ALL)
    private Idea idea;

    @ManyToOne(targetEntity = User.class, cascade = CascadeType.ALL)
    private User user;

    @Column(name = "comm")
    private String comment;

    public Date getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(Date votedAt) {
        this.votedAt = votedAt;
    }

    private Date votedAt;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    public PrioritisationDotSplitVotes() {
        super();
    }

    public PrioritisationDotSplitVotes(User user, Votes vote, String comment) {
        super();
        this.user = user;
        this.comment = comment;
        this.vote = vote;
        this.votedAt = new Date();
    }

    public Idea getIdea() {
        return idea;
    }

    public void setIdea(Idea idea) {
        this.idea = idea;
    }

    public Votes getVote() {
        return vote;
    }

    public void setVote(Votes vote) {
        this.vote = vote;
    }

}

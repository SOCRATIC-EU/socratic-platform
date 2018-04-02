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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "kpis")
public class Kpi extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = 506109256585327703L;

    @NotNull
    @Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String name;

    @Lob
    @Size(max = 65535)
    @Column(length = 65535)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String shortText;

    private Boolean deleted = Boolean.FALSE;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date postedAt;

    @NotNull
    @ManyToOne
    private User postedBy;

    @ElementCollection
    private Set<Long> votes = new HashSet<Long>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<User> collaborators = new ArrayList<User>();

    // This value is for the users to vote, the collected value
    // is stored in valueStored
    // getValue is always 0.
    private double value;

    // This is the result of all the voted values
    private double valueStored;

    private double expectedValue;

    private int voteNum;

    private String unitValue;

    /**
     *
     */
    public Kpi() {
    }

    /**
     * @return the description
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the description to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the shortText
     */
    public String getShortText() {
        return shortText;
    }

    /**
     * @param shortText the shortText to set
     */
    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    /**
     * @return the deleted
     */
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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
     * @return the votes
     */
    public Set<Long> getVotes() {
        return votes;
    }

    /**
     * @param votes the votes to set
     */
    public void setVotes(Set<Long> votes) {
        this.votes = votes;
    }

    /**
     * @return
     */
    @JsonIgnore
    @XmlTransient
    public Integer getNoOfvotes() {
        // return this.votes.size();
        return voteNum;
    }

    /**
     * @return the collaborators
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public List<User> getCollaborators() {
        return collaborators;
    }

    /**
     * @param collaborators the collaborators to set
     */
    public void setCollaborators(List<User> collaborators) {
        this.collaborators = collaborators;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     * @param value Stores the value in valueStored, and votes + 1
     */
    public void setValue(double value) {
        setValueStored(getValueStored() + value);
        voteNum += 1;
    }

    /**
     * @return
     */
    public double getValueStored() {
        return valueStored;
    }

    /**
     * @param valueStored
     */
    public void setValueStored(double valueStored) {
        this.valueStored = valueStored;
    }

    /**
     * @return the expectedValue
     */
    public double getExpectedValue() {

        return expectedValue;
    }

    /**
     * @param expectedValue the expectedValue to set
     */
    public void setExpectedValue(double expectedValue) {
        this.expectedValue = expectedValue;
    }

    public int getVoteNum() {
        return voteNum;
    }

    public void setVoteNum(int voteNum) {
        this.voteNum = voteNum;
    }

    /**
     * @return the unitValue
     */
    public String getUnitValue() {
        return unitValue;
    }

    /**
     * @param unitValue the unitValue to set
     */
    public void setUnitValue(String unitValue) {
        this.unitValue = unitValue;
    }

    @Override
    public String toString() {
        return "Kpi [name=" + name + ", shortText=" + shortText + ", deleted="
                + deleted + ", postedAt=" + postedAt + ", postedBy=" + postedBy
                + ", votes=" + votes + ", collaborators=" + collaborators
                + ", value=" + value + ", valueStored=" + valueStored
                + ", expectedValue=" + expectedValue + ", unitValue="
                + unitValue + "]";
    }
}

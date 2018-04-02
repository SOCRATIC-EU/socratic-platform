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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
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
@Table(name = "businessModel")
@Indexed
public class BusinessModel extends AbstractEntity {

    private static final long serialVersionUID = -5799460994357711309L;

    @Lob
    @NotNull
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String valuePropositions;

    @Lob
    @NotNull
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String customerSegments;

    @Lob
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String customerRelationships;

    @Lob
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String channels;

    @Lob
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String keyPartners;

    @Lob
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String keyActivities;

    @Lob
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String keyResources;

    @Lob
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String revenueStream;

    @Lob
    @Size(min = 3, max = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String costStructure;

    @OneToMany(orphanRemoval = true)
    @IndexedEmbedded
    private List<Comment> comments = new ArrayList<Comment>();

    @ElementCollection
    private Set<Long> upVotes = new HashSet<Long>();

    public String getValuePropositions() {
        return valuePropositions;
    }

    public void setValuePropositions(String valuePropositions) {
        this.valuePropositions = valuePropositions;
    }

    public String getCustomerSegments() {
        return customerSegments;
    }

    public void setCustomerSegments(String customerSegments) {
        this.customerSegments = customerSegments;
    }

    public String getCustomerRelationships() {
        return customerRelationships;
    }

    public void setCustomerRelationships(String customerRelationships) {
        this.customerRelationships = customerRelationships;
    }

    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getKeyPartners() {
        return keyPartners;
    }

    public void setKeyPartners(String keyPartners) {
        this.keyPartners = keyPartners;
    }

    public String getKeyActivities() {
        return keyActivities;
    }

    public void setKeyActivities(String keyActivities) {
        this.keyActivities = keyActivities;
    }

    public String getKeyResources() {
        return keyResources;
    }

    public void setKeyResources(String keyResources) {
        this.keyResources = keyResources;
    }

    public String getRevenueStream() {
        return revenueStream;
    }

    public void setRevenueStream(String revenueStream) {
        this.revenueStream = revenueStream;
    }

    public String getCostStructure() {
        return costStructure;
    }

    public void setCostStructure(String costStructure) {
        this.costStructure = costStructure;
    }

    /**
     * @param comment
     */
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    /**
     * @param comment
     */
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }

    /**
     * @return the comments
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * @return
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public Integer getNoOfUpVotes() {
        return this.upVotes.size();
    }

    /**
     * @return the upVotes
     */
    public Set<Long> getUpVotes() {
        return upVotes;
    }

    /**
     * @param upVotes the upVotes to set
     */
    public void setUpVotes(Set<Long> upVotes) {
        this.upVotes = upVotes;
    }

    @Override
    public String toString() {
        return "BusinessModel [valuePropositions=" + valuePropositions + ", customerSegments=" + customerSegments
                + ", customerRelationships=" + customerRelationships + ", channels=" + channels + ", keyPartners=" + keyPartners
                + ", keyActivities=" + keyActivities + ", keyResources=" + keyResources + ", revenueStream=" + revenueStream
                + ", costStructure=" + costStructure + "]";
    }
}

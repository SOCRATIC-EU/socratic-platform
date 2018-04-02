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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.atb.socratic.web.components.resource.PictureType;
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
@Table(name = "actionsIteration")
@Indexed
public class ActionIteration extends AbstractEntity {

    private static final long serialVersionUID = 1670579040501182598L;

    @NotNull
    @Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String title;

    @NotNull
    @Size(min = 3, max = 1000)
    @Column(length = 1000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String aimOfExperiment;

    @NotNull
    @Size(min = 3, max = 1000)
    @Column(length = 1000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String methodology;

    @NotNull
    @Size(min = 3, max = 1000)
    @Column(length = 1000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String plan;

    @Size(min = 3, max = 1000)
    @Column(length = 1000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String lessonsLearnt;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private FileInfo iterationImage;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date postedAt = new Date();

    @OneToMany(orphanRemoval = true)
    @IndexedEmbedded
    private List<Comment> comments = new ArrayList<Comment>();

    @ElementCollection
    private Set<Long> upVotes = new HashSet<Long>();

    @NotNull
    @ManyToOne
    @IndexedEmbedded
    private User postedBy;

    @NotNull
    private ActionIterationState state = ActionIterationState.Active;

    @ManyToOne(targetEntity = Action.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    protected Action theAction;

    /**
     *
     */
    public ActionIteration() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAimOfExperiment() {
        return aimOfExperiment;
    }

    public void setAimOfExperiment(String aimOfExperiment) {
        this.aimOfExperiment = aimOfExperiment;
    }

    public String getMethodology() {
        return methodology;
    }

    public void setMethodology(String methodology) {
        this.methodology = methodology;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getLessonsLearnt() {
        return lessonsLearnt;
    }

    public void setLessonsLearnt(String lessonsLearnt) {
        this.lessonsLearnt = lessonsLearnt;
    }

    public FileInfo getIterationImage() {
        return iterationImage;
    }

    public void setIterationImage(FileInfo iterationImage) {
        this.iterationImage = iterationImage;
    }

    public Date getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Date postedAt) {
        this.postedAt = postedAt;
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

    @Transient
    @JsonIgnore
    public File getIterationImage(PictureType type) {
        String path = iterationImage.getPath();
        for (PictureType oldType : PictureType.values()) {
            path = path.replace("." + oldType.name().toLowerCase() + ".", "." + type.name().toLowerCase() + ".");
        }
        return new File(path);
    }

    public ActionIterationState getState() {
        return state;
    }

    public void setState(ActionIterationState state) {
        this.state = state;
    }

    public Action getAction() {
        return theAction;
    }

    public void setAction(Action action) {
        this.theAction = action;
    }

    @Override
    public String toString() {
        return "ActionIteration [title=" + title + ", aimOfExperiment=" + aimOfExperiment + ", methodology=" + methodology
                + ", plan=" + plan + ", lessonsLearnt=" + lessonsLearnt + ", iterationImage=" + iterationImage + ", postedAt="
                + postedAt + ", comments=" + comments + ", upVotes=" + upVotes + ", postedBy=" + postedBy + ", state=" + state
                + "]";
    }
}

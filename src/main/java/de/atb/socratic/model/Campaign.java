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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.atb.socratic.model.notification.Notification;
import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.model.validation.Inception;
import de.atb.socratic.model.validation.InnovationLifeCycle;
import de.atb.socratic.web.components.resource.PictureType;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;


/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "campaigns")
@Indexed
public class Campaign extends AbstractEntity implements Deletable {

    private static final long serialVersionUID = -4854103585104427891L;

    private Class<? extends InnovationLifeCycle> lcPhase = Inception.class;

    @NotNull
    @Size(min = 3, max = 140)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String name;

    @NotNull
    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String elevatorPitch;

    @NotNull
    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String socialChallenge;

    @NotNull
    @Size(min = 3, max = 500)
    @Column(length = 500)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String beneficiaries;

    @NotNull
    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String potentialImpact;

    @NotNull
    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String levelOfSupport;

    @NotNull
    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String ideasProposed;

    @Lob
    @Size(max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String referencesTitle;

    @Lob
    @Size(max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String referencesDescription;

    @Lob
    @Size(max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String referencesLink;

    @Size(max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String description;

    private Boolean active;

    private Boolean definitionActive;
    private Boolean ideationActive;
    private Boolean selectionActive;

    @NotNull
    private Boolean openForDiscussion = false;

    @NotNull
    private Boolean isPublic = false; // by default its private

    private Boolean deleted = Boolean.FALSE;

    //@NotNull
    @Future(groups = Inception.class)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dueDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CampaignType campaignType = CampaignType.TOPIC;

    @Column
    @ElementCollection(targetClass = UNGoalType.class)
    private List<UNGoalType> uNGoals;

    //@NotNull
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @IndexedEmbedded
    private InnovationObjective innovationObjective;

    @NotNull
    @ManyToOne(targetEntity = User.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @IndexedEmbedded
    private User createdBy;

    @OneToMany(orphanRemoval = true, mappedBy = "campaign", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Idea> ideas = new ArrayList<Idea>();

    @OneToMany(orphanRemoval = true, mappedBy = "campaign", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Notification> notifications = new ArrayList<Notification>();

    @ManyToOne(targetEntity = Scope.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Scope scope;

    @NotNull
    @ManyToOne(targetEntity = Company.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Company company;

    private Date startDate;

    /**
     * this will refer to challenge creation time when it is created. And will used for sorting or challenges based on creation
     * date
     **/
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;

    /**
     * this will refer to challenge last modified time. And will used for sorting.
     **/
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    @NotNull
    private Date ideationStartDate;

    @NotNull
    private Date ideationEndDate;

    @NotNull
    private Date selectionStartDate;

    @NotNull
    private Date selectionEndDate;

    private Date challengeOpenForDiscussionStartDate;

    private Date challengeOpenForDiscussionEndDate;

    // @ElementCollection
    // index tags in lucene
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @FieldBridge(impl = TagsBridge.class)
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Tag> tags = new ArrayList<Tag>();

    @NotNull
    @Enumerated(EnumType.STRING)
    private InnovationStatus innovationStatus = InnovationStatus.DEFINITION;

    private transient float ideaDensity = Float.NaN;

    private transient float commentDensity = Float.NaN;

    @OneToMany(orphanRemoval = true)
    @IndexedEmbedded
    private List<Comment> comments = new ArrayList<Comment>();

    @ManyToOne(cascade = CascadeType.ALL)
    private FileInfo challengeImage;

    @ElementCollection
    private Set<Long> upVotes = new HashSet<Long>();

    // by default this filed will have below text, CO should modify in I lead management section if he/she wants!
    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String callToAction = "Help needed! We are currently searching for inspiring existing projects. Help us with your wisdom!";

    // attachments
    @OneToMany(orphanRemoval = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Set<FileInfo> attachments = new LinkedHashSet<FileInfo>();
    private String attachmentsCacheId = UUID.randomUUID().toString();

    /**
     *
     */
    public Campaign() {
        this.createdOn = new Date();
        this.lastModified = new Date();
    }

    /**
     * try to find out how to (de)serialize this when using JSON.
     *
     * @return the lcPhase
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public Class<? extends InnovationLifeCycle> getLcPhase() {
        return lcPhase;
    }

    /**
     * @param lcPhase the lcPhase to set
     */
    public void setLcPhase(Class<? extends InnovationLifeCycle> lcPhase) {
        this.lcPhase = lcPhase;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getElevatorPitch() {
        return elevatorPitch;
    }

    public void setElevatorPitch(String elevatorPitch) {
        this.elevatorPitch = elevatorPitch;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        if (description == null) {
            return "";
        }
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDefinitionActive() {
        return definitionActive;
    }

    public void setDefinitionActive(Boolean definitionActive) {
        this.definitionActive = definitionActive;
    }

    public Boolean getIdeationActive() {
        return ideationActive;
    }

    public void setIdeationActive(Boolean ideationActive) {
        this.ideationActive = ideationActive;
    }

    public Boolean getSelectionActive() {
        return selectionActive;
    }

    public void setSelectionActive(Boolean selectionActive) {
        this.selectionActive = selectionActive;
    }

    /**
     * @return the deleted
     */
    @Override
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * @param deleted the deleted to set
     */
    @Override
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * @return the dueDate
     */
    public Date getDueDate() {
        return dueDate;
    }

    /**
     * @param dueDate the dueDate to set
     */
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * @return the campaignType
     */
    public CampaignType getCampaignType() {
        return campaignType;
    }

    /**
     * @param campaignType the campaignType to set
     */
    public void setCampaignType(CampaignType campaignType) {
        this.campaignType = campaignType;
    }

    /**
     * @return the innovationObjective
     */
    public InnovationObjective getInnovationObjective() {
        return innovationObjective;
    }

    /**
     * @param innovationObjective the innovationObjective to set
     */
    public void setInnovationObjective(InnovationObjective innovationObjective) {
        this.innovationObjective = innovationObjective;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * @return the ideas
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public List<Idea> getIdeas() {
        return ideas;
    }

    /**
     * @param ideas the ideas to set
     */
    public void setIdeas(List<Idea> ideas) {
        this.ideas = ideas;
    }

    /**
     * @param idea
     */
    public void addIdea(Idea idea) {
        idea.setCampaign(this);
        this.ideas.add(idea);
    }

    /**
     * @param idea
     */
    public void removeIdea(Idea idea) {
        this.ideas.remove(idea);
    }

    /**
     * @return the tags
     */
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    /**
     * Returns comma-separated list of the tags
     *
     * @return
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public String getTagsAsString() {
        return StringUtils.join(tags, ',');
    }

    /**
     * @return the scope
     */
    public Scope getScope() {
        return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(Scope scope) {
        this.scope = scope;
    }

    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @param user
     * @return
     */
    public boolean setCompanyFromCurrentUserEmployment(User user) {
        Employment employment = user.getCurrentEmployment();
        if (employment != null) {
            setCompany(employment.getCompany());
            return true;
        }
        return false;
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

    public void setInnovationStatusBasedOnDates() {
        DateMidnight todayMidnight = new DateTime().toDateMidnight();
        DateMidnight definitionStartDateMidnight = null;
        if (this.getChallengeOpenForDiscussionStartDate() != null) {
            definitionStartDateMidnight = new DateTime(this.getChallengeOpenForDiscussionStartDate().getTime())
                    .toDateMidnight();
        }
        DateMidnight definitionEndDateMidnight = null;
        if (this.getChallengeOpenForDiscussionEndDate() != null) {
            definitionEndDateMidnight = new DateTime(this.getChallengeOpenForDiscussionEndDate().getTime()).toDateMidnight();
        }

        DateMidnight ideationStartDateMidnight = null;
        if (this.getIdeationStartDate() != null) {
            ideationStartDateMidnight = new DateTime(this.getIdeationStartDate().getTime()).toDateMidnight();
        }

        DateMidnight ideationEndDateMidnight = null;
        if (this.getIdeationEndDate() != null) {
            ideationEndDateMidnight = new DateTime(this.getIdeationEndDate().getTime()).toDateMidnight();
        }

        DateMidnight selectionStartDateMidnight = null;
        if (this.getSelectionStartDate() != null) {
            selectionStartDateMidnight = new DateTime(this.getSelectionStartDate().getTime()).toDateMidnight();
        }

        DateMidnight selectionEndDateMidnight = null;
        if (this.getSelectionEndDate() != null) {
            selectionEndDateMidnight = new DateTime(this.getSelectionEndDate().getTime()).toDateMidnight();
        }
        // check if today's date is between definition start and stop dates?
        if (definitionStartDateMidnight != null && (definitionStartDateMidnight.isAfter(todayMidnight))
                && this.openForDiscussion) {
            // here user has entered definitions dates which are in future, so we need to wait until start date arrives.
            // During this phsae however challenge will be in definition phase but inActive
            this.setDefinitionActive(false);
            this.setInnovationStatus(InnovationStatus.DEFINITION);
            this.setActive(false);
            return;
        } else if (definitionStartDateMidnight != null
                && definitionEndDateMidnight != null
                && ((definitionStartDateMidnight.isBefore(todayMidnight) || definitionStartDateMidnight.isEqual(todayMidnight)) && (definitionEndDateMidnight
                .isAfter(todayMidnight) || definitionEndDateMidnight.isEqual(todayMidnight))) && this.openForDiscussion) {
            // set definition active
            this.setDefinitionActive(true);
            this.setInnovationStatus(InnovationStatus.DEFINITION);
            this.setActive(false);
            return;
        } else if (ideationStartDateMidnight != null && ideationStartDateMidnight.isAfter(todayMidnight)) {
            this.setActive(false);
            this.setIdeationActive(false);
            this.setInnovationStatus(InnovationStatus.INCEPTION);
            return;
        } else if (ideationStartDateMidnight != null && ideationEndDateMidnight != null
                && (ideationStartDateMidnight.isBefore(todayMidnight) || ideationStartDateMidnight.isEqual(todayMidnight))
                && (ideationEndDateMidnight.isAfter(todayMidnight) || ideationEndDateMidnight.isEqual(todayMidnight))) {
            this.setActive(true);
            this.setIdeationActive(true);
            this.setInnovationStatus(InnovationStatus.INCEPTION);
            this.setLcPhase(Inception.class);
            return;
        } else if (selectionStartDateMidnight != null && selectionStartDateMidnight.isAfter(todayMidnight)) {
            this.setSelectionActive(false);
            this.setInnovationStatus(InnovationStatus.PRIORITISATION);
            this.setActive(false);
            return;
        } else if (selectionStartDateMidnight != null && selectionEndDateMidnight != null
                && (selectionStartDateMidnight.isBefore(todayMidnight) || selectionStartDateMidnight.isEqual(todayMidnight))
                && (selectionEndDateMidnight.isAfter(todayMidnight) || selectionEndDateMidnight.isEqual(todayMidnight))) {
            this.setSelectionActive(true);
            this.setInnovationStatus(InnovationStatus.PRIORITISATION);
            this.setActive(false);
            return;
        }

    }

    public boolean hasOwner(User user) {
        return (this.createdBy != null) && (user != null)
                && this.createdBy.getId().equals(user.getId());
    }

    public boolean isEditableBy(User user) {
        if (user.getCurrentRole().hasAnyRoles(UserRole.ADMIN) || createdBy.equals(user)) {
            return true;
        }
        return false;
    }

    @JsonIgnore
    @XmlTransient
    public float getCommentDensity() {
        return this.commentDensity;
    }

    public void setCommentDensity(float density) {
        this.commentDensity = density;
    }

    @JsonIgnore
    @XmlTransient
    public float getIdeaDensity() {
        return this.ideaDensity;
    }

    public void setIdeaDensity(float density) {
        this.ideaDensity = density;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
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
     * The method is used to get comments for given filter
     *
     * @param commentDisplayFilter
     */
    public List<Comment> getComments(
            Collection<InnovationStatus> commentDisplayFilter) {
        final List<Comment> lReturn = new LinkedList<Comment>();
        for (Comment comment : this.getComments()) {
            if (commentDisplayFilter.contains(comment.getInnovationStatus())) {
                lReturn.add(comment);
            }
        }
        return lReturn;
    }

    public String getSocialChallenge() {
        return socialChallenge;
    }

    public void setSocialChallenge(String socialChallenge) {
        this.socialChallenge = socialChallenge;
    }

    public String getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(String beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    public String getPotentialImpact() {
        return potentialImpact;
    }

    public void setPotentialImpact(String potentialImpact) {
        this.potentialImpact = potentialImpact;
    }

    public String getLevelOfSupport() {
        return levelOfSupport;
    }

    public void setLevelOfSupport(String levelOfSupport) {
        this.levelOfSupport = levelOfSupport;
    }

    public String getIdeasProposed() {
        return ideasProposed;
    }

    public void setIdeasProposed(String ideasProposed) {
        this.ideasProposed = ideasProposed;
    }

    public String getReferencesTitle() {
        return referencesTitle;
    }

    public void setReferencesTitle(String referencesTitle) {
        this.referencesTitle = referencesTitle;
    }

    public String getReferencesDescription() {
        return referencesDescription;
    }

    public void setReferencesDescription(String referencesDescription) {
        this.referencesDescription = referencesDescription;
    }

    public String getReferencesLink() {
        return referencesLink;
    }

    public void setReferencesLink(String referencesLink) {
        this.referencesLink = referencesLink;
    }

    public Date getIdeationStartDate() {
        return ideationStartDate;
    }

    public void setIdeationStartDate(Date ideationStartDate) {
        this.ideationStartDate = ideationStartDate;
    }

    public Date getIdeationEndDate() {
        return ideationEndDate;
    }

    public void setIdeationEndDate(Date ideationEndDate) {
        this.ideationEndDate = ideationEndDate;
    }

    public Date getSelectionStartDate() {
        return selectionStartDate;
    }

    public void setSelectionStartDate(Date selectionStartDate) {
        this.selectionStartDate = selectionStartDate;
    }

    public Date getSelectionEndDate() {
        return selectionEndDate;
    }

    public void setSelectionEndDate(Date selectionEndDate) {
        this.selectionEndDate = selectionEndDate;
    }

    public Boolean getOpenForDiscussion() {
        return openForDiscussion;
    }

    public void setOpenForDiscussion(Boolean openForDiscussion) {
        this.openForDiscussion = openForDiscussion;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Date getChallengeOpenForDiscussionStartDate() {
        return challengeOpenForDiscussionStartDate;
    }

    public void setChallengeOpenForDiscussionStartDate(Date challengeOpenForDiscussionStartDate) {
        this.challengeOpenForDiscussionStartDate = challengeOpenForDiscussionStartDate;
    }

    public Date getChallengeOpenForDiscussionEndDate() {
        return challengeOpenForDiscussionEndDate;
    }

    public void setChallengeOpenForDiscussionEndDate(Date challengeOpenForDiscussionEndDate) {
        this.challengeOpenForDiscussionEndDate = challengeOpenForDiscussionEndDate;
    }

    public FileInfo getChallengeImage() {
        return challengeImage;
    }

    public List<UNGoalType> getuNGoals() {
        return uNGoals;
    }

    public void setuNGoals(List<UNGoalType> uNGoals) {
        this.uNGoals = uNGoals;
    }

    @Transient
    @JsonIgnore
    public File getChallengeImage(PictureType type) {
        String path = challengeImage.getPath();
        for (PictureType oldType : PictureType.values()) {
            path = path.replace("." + oldType.name().toLowerCase() + ".", "." + type.name().toLowerCase()
                    + ".");
        }
        return new File(path);
    }

    public void setChallengeImage(FileInfo challengeImage) {
        this.challengeImage = challengeImage;

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
     * @return
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public Integer getNoOfUpVotes() {
        return this.upVotes.size();
    }

    public String getCallToAction() {
        return callToAction;
    }

    public void setCallToAction(String callToAction) {
        this.callToAction = callToAction;
    }

    /**
     * @return the attachments
     */
    public Set<FileInfo> getAttachments() {
        return attachments;
    }

    /**
     * @param attachments the attachments to set
     */
    public void setAttachments(Set<FileInfo> attachments) {
        this.attachments = attachments;
    }

    /**
     * @return the attachmentsCacheId
     */
    public String getAttachmentsCacheId() {
        return attachmentsCacheId;
    }

    /**
     * @param attachmentsCacheId the attachmentsCacheId to set
     */
    public void setAttachmentsCacheId(String attachmentsCacheId) {
        this.attachmentsCacheId = attachmentsCacheId;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Campaign [lcPhase=" + lcPhase + ", name=" + name
                + ", description=" + description + ", active=" + active
                + ", deleted=" + deleted + ", dueDate=" + dueDate
                + ", campaignType=" + campaignType + ", innovationObjective="
                + innovationObjective + ", createdBy=" + createdBy + ", tags="
                + tags + ", getId()=" + getId() + "]";
    }
}

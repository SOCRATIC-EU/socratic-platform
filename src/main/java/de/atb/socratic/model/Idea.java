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
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.atb.socratic.model.notification.Notification;
import de.atb.socratic.model.votes.PrioritisationDotSplitVotes;
import de.atb.socratic.model.votes.VoteType;
import de.atb.socratic.model.votes.Votes;
import de.atb.socratic.web.components.resource.PictureType;
import de.atb.socratic.web.inception.idea.IdeaDevelopmentPhase;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "ideas")
@Indexed
public class Idea extends AbstractEntity implements Deletable {

    private static final long serialVersionUID = 1670579040501182598L;

    @NotNull
    @Size(min = 3, max = 140)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String shortText;

    @NotNull
    @Size(max = 65535)
    @Column(length = 65535, columnDefinition = "TEXT")
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String description;

    @NotNull
    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String elevatorPitch;

    @NotNull
    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String beneficiaries;

    @Size(min = 3, max = 500)
    @Column(length = 500)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String valueForBeneficiaries;

    @Size(min = 3, max = 500)
    @Column(length = 500)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String impactStakeholders;

    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String resourcesForIdeaImplementation;

    @Size(min = 3, max = 500)
    @Column(length = 500)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String implementationPlan;

    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String location;

    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String reasonForBringingIdeaForward;

    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String relatedInnovations;

    @Column
    @ElementCollection(targetClass = IdeaType.class)
    private List<IdeaType> ideaType;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private FileInfo ideaImage;

    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String ideaTypeText;

    private Boolean deleted = Boolean.FALSE;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date postedAt;

    /**
     * this will refer to idea last modified time. And will used for sorting.
     **/
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModified;

    @NotNull
    @ManyToOne
    @IndexedEmbedded
    private User postedBy;

    @OneToMany(targetEntity = Notification.class, mappedBy = "idea", orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<Notification>();

    @OneToMany(targetEntity = PrioritisationDotSplitVotes.class, mappedBy = "idea", orphanRemoval = true)
    private List<PrioritisationDotSplitVotes> prioVotes = new ArrayList<PrioritisationDotSplitVotes>();

    @ManyToOne
    @IndexedEmbedded
    private Campaign campaign;

    // @ElementCollection
    // index tags in lucene
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @FieldBridge(impl = TagsBridge.class)
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "idea_keywords")
    private List<Tag> keywords = new ArrayList<Tag>();

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @FieldBridge(impl = TagsBridge.class)
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "idea_skills")
    private List<Tag> skills = new ArrayList<Tag>();

    @ElementCollection
    private Set<Long> upVotes = new HashSet<Long>();

    @ElementCollection
    private Set<Long> downVotes = new HashSet<Long>();

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Set<FileInfo> attachments = new LinkedHashSet<FileInfo>();

    private String attachmentsCacheId = UUID.randomUUID().toString();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @IndexedEmbedded
    private List<User> collaborators = new ArrayList<User>();

    @OneToMany(orphanRemoval = true)
    @IndexedEmbedded
    private List<Comment> comments = new ArrayList<Comment>();

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Set<PrioritisationDotSplitVotes> prioritisationDotVotes = new LinkedHashSet<PrioritisationDotSplitVotes>();

    private double prioritisationDotFeasibilityVoteAVG;
    private double prioritisationDotRelevanceVoteAVG;
    private double prioritisationDotVoteAVG;

    @OneToOne(orphanRemoval = true, optional = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private IdeaDiscardEvent discardEvent;

    private int countUpVotes = 0;

    private int countDownVotes = 0;

    // by default phase is OnHalt
    private IdeaDevelopmentPhase ideaPhase = IdeaDevelopmentPhase.OnHalt;

    // this bool is for creating an action from idea and let people know
    private boolean isActionCreated = false;

    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String callToAction = "Do you think this a good idea? Don't hesitate to comments our first concepts.";

    public double getConfidence() {
        // Implementation of confidence value based on
        // http://en.wikipedia.org/wiki/Binomial_proportion_confidence_interval#Wilson_score_interval
        long n = upVotes.size() + downVotes.size();
        if (n == 0) {
            return 0;
        }
        double z = 1.0d; // 1.0 = 85%, 1.6 = 95% in confidence
        double phat = (double) upVotes.size() / n;
        double result = Math.sqrt((phat + ((z * z) / (2 * n)))
                - (z * (((phat * (1 - phat)) + ((z * z) / (4 * n))) / n)))
                / (1 + ((z * z) / n));
        return result;

    }

    public int getCountUpVotes() {
        return countUpVotes;
    }

    public void setCountUpVotes(int countUpVotes) {
        this.countUpVotes = countUpVotes;
    }

    public int getCountDownVotes() {
        return countDownVotes;
    }

    public void setCountDownVotes(int countDownVotes) {
        this.countDownVotes = countDownVotes;
    }


    public List<PrioritisationDotSplitVotes> getPrioVotes() {
        return prioVotes;
    }

    public void setPrioVotes(List<PrioritisationDotSplitVotes> prioVotes) {
        this.prioVotes = prioVotes;
    }

    /**
     *
     */
    public Idea() {
        this.lastModified = new Date();
        this.postedAt = new Date();
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

    public String getElevatorPitch() {
        return elevatorPitch;
    }

    public void setElevatorPitch(String elevatorPitch) {
        this.elevatorPitch = elevatorPitch;
    }

    public String getBeneficiaries() {
        return beneficiaries;
    }

    public void setBeneficiaries(String beneficiaries) {
        this.beneficiaries = beneficiaries;
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
     * @return the tags
     */
    public List<Tag> getKeywords() {
        return keywords;
    }

    /**
     * @param keywords the tags to set
     */
    public void setKeywords(List<Tag> keywords) {
        this.keywords = keywords;
    }

    public List<Tag> getSkills() {
        return skills;
    }

    public void setSkills(List<Tag> skills) {
        this.skills = skills;
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
    public String getKeywordsAsString() {
        List<String> tagNames = new LinkedList<String>();
        for (Tag tag : this.keywords) {
            tagNames.add(tag.getTag());
        }
        return StringUtils.join(tagNames, ',');
    }

    /**
     * Takes comma-separated String of tags, converts it to List to store in
     * tags property
     *
     * @param tagsAsString
     */
    public void setTagsAsString(String tagsAsString) {
        // what to-do here?!
        if (StringUtils.isNotBlank(tagsAsString)) {
            // this.tags = Arrays.asList(StringUtils.split(tagsAsString, ','));
        }
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

    /**
     * @return the downVotes
     */
    public Set<Long> getDownVotes() {
        return downVotes;
    }

    /**
     * @param downVotes the downVotes to set
     */
    public void setDownVotes(Set<Long> downVotes) {
        this.downVotes = downVotes;
    }

    /**
     * @return
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public Integer getNoOfDownVotes() {
        return this.downVotes.size();
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

    /**
     * @return the collaborators
     */
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
     * @return
     */
    public Campaign getCampaign() {
        return campaign;
    }

    /**
     * @param campaign
     */
    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    /**
     * @return
     */
    public IdeaDiscardEvent getDiscardEvent() {
        return discardEvent;
    }

    /**
     * @param discardEvent
     */
    public void setDiscardEvent(IdeaDiscardEvent discardEvent) {
        this.discardEvent = discardEvent;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Idea [shortText=" + shortText + ", description=" + description
                + ", deleted=" + deleted + ", postedAt=" + postedAt
                + ", postedBy=" + postedBy + ", tags=" + keywords + ", upVotes="
                + upVotes + ", downVotes=" + downVotes + ", attachments="
                + attachments + ", collaborators=" + collaborators
                + ", comments=" + comments + ", getId()=" + getId()
                + "]";
    }

    /**
     * The method is used to get dot votes
     */
    public Set<PrioritisationDotSplitVotes> getPrioritisationDotVotes() {
        return prioritisationDotVotes;
    }

    /**
     * The method is used to set dot votes
     *
     * @param prioritisationDotVotes
     */
    public void setPrioritisationDotVotes(
            Set<PrioritisationDotSplitVotes> prioritisationDotVotes) {
        this.prioritisationDotVotes = prioritisationDotVotes;
    }

    /**
     * The method is used to get dot vote for a user
     *
     * @param user
     */
    public Votes getPrioritisationDotVote(User user) {

        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            if (pr.getUser().equals(user)) {
                return pr.getVote();
            }
        }
        return null;
    }

    /**
     * The method is used to average Relevance vote in dot votes
     */
    public double getPrioritisationDotRelevanceVoteAVG() {
        Integer count = 0;
        double avg = 0;
        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            avg += pr.getVote().getRelevanceVote();
            count++;
        }
        if (count == 0) {
            return 0;
        }
        this.prioritisationDotRelevanceVoteAVG = round(avg, count);
        return prioritisationDotRelevanceVoteAVG;
    }

    /**
     * The method is used to average Feasibility vote in dot votes
     */
    public double getPrioritisationDotFeasibilityVoteAVG() {
        Integer count = 0;
        double avg = 0;
        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            avg += pr.getVote().getFeasibilityVote();
            count++;
        }
        if (count == 0) {
            return 0;
        }
        this.prioritisationDotFeasibilityVoteAVG = round(avg, count);
        return prioritisationDotFeasibilityVoteAVG;
    }

    /**
     * The method is used to get total votes in dot votes: Both relevance and feasible
     */
    public double getPrioritisationDotVoteList() {
        double voteList = 0;

        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            if (!(pr.getUser() == (null))) {
                voteList += pr.getVote().getRelevanceVote() + pr.getVote().getFeasibilityVote();
            }
        }
        return round(voteList, 2);
    }

    /**
     * The method is used to get all users in dot vote
     */
    public List<User> getPrioritisationDotVoteUserList() {
        List<User> dotVoteUserList = new LinkedList<>();

        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            if (pr.getVote().getRelevanceVote() != 0) {
                dotVoteUserList.add(pr.getUser());
            }
        }
        return dotVoteUserList;
    }


    public double getPrioritisationDotVoteAVG() {
        if (this.getPrioritisationDotVoteUserList().size() != 0) {
            this.prioritisationDotVoteAVG = round(getPrioritisationDotVoteList(), getPrioritisationDotVoteUserList().size());
        }
        return prioritisationDotVoteAVG;
    }

    public static double round(double nominator, int denominator) {
        BigDecimal bd = new BigDecimal(nominator / denominator);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * The method is used to get prioritisation dot vote object for a user
     *
     * @param user
     */
    public PrioritisationDotSplitVotes getPrioritisationDotVoteObject(User user) {

        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            if (pr.getUser().equals(user)) {
                return pr;
            }
        }

        return null;
    }

    /**
     * The method is used to set prioritisation dot vote for a user
     *
     * @param user
     * @param vote
     * @param comment
     */
    public void setPrioritisationDotVote(User user, Votes vote, String comment) {

        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            if (pr.getUser().equals(user)) {
                pr.setVote(vote);
                pr.setComment(comment);
                return;
            }
        }
    }

    /**
     * @param user
     * @param votedAt
     * @param vote
     */
    public void setPrioritisationDotVote(User user, Date votedAt, Votes vote) {

        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            if (pr.getUser().equals(user)) {
                pr.setVote(vote);
                pr.setVotedAt(votedAt);
                return;
            }
        }
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

    public Integer getNoOfDotVotes(String tag, int number) {
        int iReturn = 0;
        for (PrioritisationDotSplitVotes pr : getPrioritisationDotVotes()) {
            if (VoteType.relevance.toString().equalsIgnoreCase(tag)) {
                if (pr != null && pr.getVote() != null && pr.getVote().getRelevanceVote() == number) {
                    iReturn++;
                }
            } else if (VoteType.feasibility.toString().equalsIgnoreCase(tag)) {
                if (pr != null && pr.getVote() != null && pr.getVote().getFeasibilityVote() == number) {
                    iReturn++;
                }
            }
        }
        return iReturn;
    }

    public String getValueForBeneficiaries() {
        return valueForBeneficiaries;
    }

    public void setValueForBeneficiaries(String valueForBeneficiaries) {
        this.valueForBeneficiaries = valueForBeneficiaries;
    }

    public String getImpactStakeholders() {
        return impactStakeholders;
    }

    public String getResourcesForIdeaImplementation() {
        return resourcesForIdeaImplementation;
    }

    public void setResourcesForIdeaImplementation(String resourcesForIdeaImplementation) {
        this.resourcesForIdeaImplementation = resourcesForIdeaImplementation;
    }

    public String getImplementationPlan() {
        return implementationPlan;
    }

    public void setImplementationPlan(String implementationPlan) {
        this.implementationPlan = implementationPlan;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getReasonForBringingIdeaForward() {
        return reasonForBringingIdeaForward;
    }

    public void setReasonForBringingIdeaForward(String reasonForBringingIdeaForward) {
        this.reasonForBringingIdeaForward = reasonForBringingIdeaForward;
    }

    public String getRelatedInnovations() {
        return relatedInnovations;
    }

    public void setRelatedInnovations(String relatedInnovations) {
        this.relatedInnovations = relatedInnovations;
    }

    public List<IdeaType> getIdeaType() {
        return ideaType;
    }

    public void setIdeaType(List<IdeaType> ideaType) {
        this.ideaType = ideaType;
    }

    public String getIdeaTypeText() {
        return ideaTypeText;
    }

    public void setIdeaTypeText(String ideaTypeText) {
        this.ideaTypeText = ideaTypeText;
    }

    public FileInfo getIdeaImage() {
        return ideaImage;
    }

    public void setIdeaImage(FileInfo ideaImage) {
        this.ideaImage = ideaImage;
    }

    public IdeaDevelopmentPhase getIdeaPhase() {
        return ideaPhase;
    }

    public void setIdeaPhase(IdeaDevelopmentPhase ideaPhase) {
        this.ideaPhase = ideaPhase;
    }

    public boolean isActionCreated() {
        return isActionCreated;
    }

    public void setActionCreated(boolean isActionCreate) {
        this.isActionCreated = isActionCreate;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public void setCallToAction(String callToAction) {
        this.callToAction = callToAction;
    }

    public boolean isEditableBy(User user) {
        if (user.getCurrentRole().hasAnyRoles(UserRole.ADMIN) || postedBy.equals(user)) {
            return true;
        }
        return false;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Transient
    @JsonIgnore
    public File getIdeaImage(PictureType type) {
        String path = ideaImage.getPath();
        for (PictureType oldType : PictureType.values()) {
            path = path.replace("." + oldType.name().toLowerCase() + ".", "." + type.name().toLowerCase()
                    + ".");
        }
        return new File(path);
    }
}

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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
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

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "actions")
@Indexed
public class Action extends AbstractEntity implements Deletable {

    private static final long serialVersionUID = 1670579040501182598L;

    @NotNull
    @Size(min = 3, max = 140)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String shortText;

    @Lob
    @NotNull
    @Size(max = 65535)
    @Column(length = 65535)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String description;

    @NotNull
    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String elevatorPitch;

    @Lob
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
    private String resourcesForActionImplementation;

    @Size(min = 3, max = 500)
    @Column(length = 500)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String implementationPlan;

    @Lob
    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String location;

    @Lob
    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String reasonForBringingActionForward;

    @Lob
    @Size(min = 3, max = 2000)
    @Column(length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String relatedInnovations;

    @Column
    @ElementCollection(targetClass = IdeaType.class)
    private List<IdeaType> actionType;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private FileInfo actionImage;

    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String actionTypeText;

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

    @NotNull
    @OneToOne
    @IndexedEmbedded
    private Idea idea;

    // index tags in lucene
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @FieldBridge(impl = TagsBridge.class)
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "action_keywords")
    private List<Tag> keywords = new ArrayList<>();

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @FieldBridge(impl = TagsBridge.class)
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "action_skills")
    private List<Tag> skills = new ArrayList<>();

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Set<FileInfo> attachments = new LinkedHashSet<>();

    private String attachmentsCacheId = UUID.randomUUID().toString();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "teamMembers")
    private List<User> teamMembers = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "invitedTeamMembers")
    private List<User> invitedTeamMembers = new ArrayList<>();

    @OneToMany(orphanRemoval = true)
    @IndexedEmbedded
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(orphanRemoval = true, optional = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private IdeaDiscardEvent discardEvent;

    @OneToMany(orphanRemoval = true)
    @IndexedEmbedded
    private List<ActionIteration> actionIterations = new ArrayList<>();

    @Size(min = 3, max = 250)
    @Column(length = 250)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String callToAction;

    @ElementCollection
    private Set<Long> upVotes = new HashSet<>();

    @OneToOne
    @IndexedEmbedded
    private BusinessModel businessModel;

    @OneToMany(orphanRemoval = true)
    private List<ActionTeamTool> actionTeamTools = new ArrayList<>();

    /**
     *
     */
    public Action() {
        this.lastModified = new Date();
        this.postedAt = new Date();
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
        if (StringUtils.isNotBlank(tagsAsString)) {
            // this.tags = Arrays.asList(StringUtils.split(tagsAsString, ','));
        }
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

    public List<User> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<User> teamMembers) {
        this.teamMembers = teamMembers;
    }

    public List<User> getInvitedTeamMembers() {
        return invitedTeamMembers;
    }

    public void setInvitedTeamMembers(List<User> invitedTeamMembers) {
        this.invitedTeamMembers = invitedTeamMembers;
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
    public Idea getIdea() {
        return idea;
    }

    /**
     * @param idea
     */
    public void setIdea(Idea idea) {
        this.idea = idea;
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

    @Override
    public String toString() {
        return "Action [shortText=" + shortText + ", description=" + description + ", elevatorPitch=" + elevatorPitch
                + ", beneficiaries=" + beneficiaries + ", valueForBeneficiaries=" + valueForBeneficiaries + ", impactStakeholders="
                + impactStakeholders + ", resourcesForActionImplementation=" + resourcesForActionImplementation
                + ", implementationPlan=" + implementationPlan + ", location=" + location + ", reasonForBringingActionForward="
                + reasonForBringingActionForward + ", relatedInnovations=" + relatedInnovations + ", actionType=" + actionType
                + ", actionImage=" + actionImage + ", actionTypeText=" + actionTypeText + ", deleted=" + deleted + ", postedAt="
                + postedAt + ", postedBy=" + postedBy + ", idea=" + idea + ", keywords=" + keywords + ", skills=" + skills
                + ", attachments=" + attachments + ", attachmentsCacheId=" + attachmentsCacheId + ", teamMembers=" + teamMembers
                + ", comments=" + comments + ", discardEvent=" + discardEvent + ", actionIterations=" + actionIterations
                + ", callToAction=" + callToAction + "]";
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

    public String getValueForBeneficiaries() {
        return valueForBeneficiaries;
    }

    public void setValueForBeneficiaries(String valueForBeneficiaries) {
        this.valueForBeneficiaries = valueForBeneficiaries;
    }

    public String getImpactStakeholders() {
        return impactStakeholders;
    }

    public String getResourcesForActionImplementation() {
        return resourcesForActionImplementation;
    }

    public void setResourcesForActionImplementation(String resourcesForActionImplementation) {
        this.resourcesForActionImplementation = resourcesForActionImplementation;
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

    public String getReasonForBringingActionForward() {
        return reasonForBringingActionForward;
    }

    public void setReasonForBringingActionForward(String reasonForBringingActionForward) {
        this.reasonForBringingActionForward = reasonForBringingActionForward;
    }

    public String getRelatedInnovations() {
        return relatedInnovations;
    }

    public void setRelatedInnovations(String relatedInnovations) {
        this.relatedInnovations = relatedInnovations;
    }

    public List<IdeaType> getActionType() {
        return actionType;
    }

    public void setActionType(List<IdeaType> actionType) {
        this.actionType = actionType;
    }

    public String getActionTypeText() {
        return actionTypeText;
    }

    public void setActionTypeText(String actionTypeText) {
        this.actionTypeText = actionTypeText;
    }

    public FileInfo getActionImage() {
        return actionImage;
    }

    public void setActionImage(FileInfo actionImage) {
        this.actionImage = actionImage;
    }

    public List<ActionIteration> getActionIterations() {
        return actionIterations;
    }

    public void setActionIterations(List<ActionIteration> actionIterations) {
        this.actionIterations = actionIterations;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public void setCallToAction(String callToAction) {
        this.callToAction = callToAction;
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

    public BusinessModel getBusinessModel() {
        return businessModel;
    }

    public void setBusinessModel(BusinessModel businessModel) {
        this.businessModel = businessModel;
    }

    public List<ActionTeamTool> getActionTeamTools() {
        return actionTeamTools;
    }

    public void setActionTeamTools(List<ActionTeamTool> actionTeamTools) {
        this.actionTeamTools = actionTeamTools;
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

    public boolean isEditableBy(User user) {
        if (user.getCurrentRole().hasAnyRoles(UserRole.ADMIN) || postedBy.equals(user)) {
            return true;
        }
        return false;
    }

    @Transient
    @JsonIgnore
    public File getActionImage(PictureType type) {
        String path = actionImage.getPath();
        for (PictureType oldType : PictureType.values()) {
            path = path.replace("." + oldType.name().toLowerCase() + ".", "." + type.name().toLowerCase()
                    + ".");
        }
        return new File(path);
    }
}

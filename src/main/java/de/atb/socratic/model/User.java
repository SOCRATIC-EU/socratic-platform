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
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
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

import de.atb.socratic.authorization.authentication.ldap.ActiveDirectory.ActiveDirectoryUser;
import de.atb.socratic.model.notification.Notification;
import de.atb.socratic.model.votes.PrioritisationDotSplitVotes;
import de.atb.socratic.web.components.resource.PictureType;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.analysis.UAX29URLEmailTokenizerFactory;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.validator.constraints.Email;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "users")
@Indexed
public class User extends AbstractEntity {

    private static final long serialVersionUID = 5895108700231524457L;

    @NotNull
    @Size(min = 2, max = 100)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String nickName;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String firstName;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String lastName;

    @NotNull
    @Email
    @Column(length = 190, unique = true)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @TokenizerDef(factory = UAX29URLEmailTokenizerFactory.class)
    // acts as username/login
    private String email;

    private boolean registeredThroughEFF = false;

    @Column(length = 190, unique = true)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    // acts as ldap user principal
    private String ldapLogin;

    @Column(length = 190, unique = true)
    private String linkedInId;

    @Column(length = 190, unique = true)
    private String facebookId;

    private String facebookUrl;

    private String linkedInUrl;

    private String twitterUrl;

    @NotNull
    private byte[] password;

    @NotNull
    private byte[] pwSalt;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date registrationDate = new Date();

    @NotNull
    @Enumerated(EnumType.STRING)
    private RegistrationStatus registrationStatus;

    private String registrationToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date resetPWRequestDate;

    private String resetPWRequestToken;

    @OneToMany(fetch = FetchType.EAGER, targetEntity = Employment.class, mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true)
    private Set<Employment> employments = new HashSet<Employment>();

    @OneToMany(targetEntity = PrioritisationDotSplitVotes.class, mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true)
    private Set<PrioritisationDotSplitVotes> prioDotVotes = new HashSet<PrioritisationDotSplitVotes>();

    @OneToMany(targetEntity = Notification.class, orphanRemoval = true, mappedBy = "user", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Notification> notifications = new ArrayList<Notification>();

    @OneToMany(targetEntity = Reply.class, orphanRemoval = true, mappedBy = "postedBy", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Reply> replies = new ArrayList<Reply>();

    @OneToMany(targetEntity = Idea.class, orphanRemoval = true, mappedBy = "postedBy", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Idea> ideas = new ArrayList<Idea>();

    @OneToMany(targetEntity = Company.class, mappedBy = "createdBy", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Company> createdCompanies = new ArrayList<Company>();

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @FieldBridge(impl = TagsBridge.class)
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "user_skills")
    private List<Tag> skills = new ArrayList<>();

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    @FieldBridge(impl = TagsBridge.class)
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "user_interests")
    private List<Tag> interests = new ArrayList<>();

    @OneToMany(orphanRemoval = true, mappedBy = "createdBy", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Campaign> createdCampaigns = new ArrayList<Campaign>();

    @Transient
    private Employment currentEmployment;

    private String uploadCacheId = UUID.randomUUID().toString();

    @ManyToOne(cascade = CascadeType.ALL)
    private FileInfo profilePictureFile;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private boolean receiveNotifications = true;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private boolean receiveChallengeNotifications = true;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private boolean receiveIdeaNotifications = true;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private boolean receiveActionNotifications = true;

    @NotNull
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> invitedEmailContacts = new ArrayList<>();

    @Temporal(TemporalType.DATE)
    private Date birthDate;

    @Lob
    @Size(max = 2000)
    @Column(name = "the_condition", length = 2000)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String condition;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "challenges_followed")
    private List<Campaign> followedCampaigns = new ArrayList<Campaign>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "ideas_followed")
    private List<Idea> followedIdeas = new ArrayList<Idea>();

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinTable(name = "actions_followed")
    private List<Action> followedActions = new ArrayList<Action>();

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String city;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String country;

    private String website;

    private int noOfCampaignsLeads = 0;
    private int noOfIdeasLeads = 0;
    private int noOfActionsLeads = 0;
    private int noOfCommentsPosts = 0;
    private int noOfLikesGiven = 0;
    private int noOfLikesReceived = 0;

    private Boolean deleted = Boolean.FALSE;

    public User() {
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    /**
     * @param firstName the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param lastName the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the registeredThroughEFF
     */
    public boolean isRegisteredThroughEFF() {
        return registeredThroughEFF;
    }

    /**
     * @param registeredThroughEFF the registeredThroughEFF to set
     */
    public void setRegisteredThroughEFF(boolean registeredThroughEFF) {
        this.registeredThroughEFF = registeredThroughEFF;
    }

    /**
     * @return the ldapPrincipal
     */
    public String getLdapLogin() {
        return ldapLogin;
    }

    /**
     * @param ldapLogin the ldapPrincipal to set
     */
    public void setLdapLogin(String ldapLogin) {
        this.ldapLogin = ldapLogin;
    }

    /**
     * @return the password
     */
    @JsonIgnore
    @XmlTransient
    public byte[] getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }

    /**
     * @return the pwSalt
     */
    @JsonIgnore
    @XmlTransient
    public byte[] getPwSalt() {
        return pwSalt;
    }

    /**
     * @param pwSalt the pwSalt to set
     */
    public void setPwSalt(byte[] pwSalt) {
        this.pwSalt = pwSalt;
    }

    /**
     * @return the registrationDate
     */
    public Date getRegistrationDate() {
        return registrationDate;
    }

    /**
     * @param registrationDate the registrationDate to set
     */
    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * @return the registrationStatus
     */
    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }

    /**
     * @param registrationStatus the registrationStatus to set
     */
    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    /**
     * @return the registrationToken
     */
    @JsonIgnore
    @XmlTransient
    public String getRegistrationToken() {
        return registrationToken;
    }

    /**
     * @param registrationToken the registrationToken to set
     */
    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken;
    }

    /**
     * @return the resetPWRequestDate
     */
    public Date getResetPWRequestDate() {
        return resetPWRequestDate;
    }

    /**
     * @param resetPWRequestDate the resetPWRequestDate to set
     */
    public void setResetPWRequestDate(Date resetPWRequestDate) {
        this.resetPWRequestDate = resetPWRequestDate;
    }

    /**
     * @return the resetPWRequestToken
     */
    @JsonIgnore
    @XmlTransient
    public String getResetPWRequestToken() {
        return resetPWRequestToken;
    }

    /**
     * @param resetPWRequestToken the resetPWRequestToken to set
     */
    public void setResetPWRequestToken(String resetPWRequestToken) {
        this.resetPWRequestToken = resetPWRequestToken;
    }

    /**
     * @return the employments
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public Set<Employment> getEmployments() {
        return employments;
    }

    /**
     * @param employments the employments to set
     */
    public void setEmployments(Set<Employment> employments) {
        this.employments = employments;
    }

    /**
     * returns the user's companies.
     *
     * @return
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public Set<Company> getEmploymentCompanies() {
        Set<Company> employmentCompanies = new HashSet<Company>();
        for (Employment employment : getEmployments()) {
            employmentCompanies.add(employment.getCompany());
        }
        return employmentCompanies;
    }

    /**
     * @return the currentEmployment
     */
    @JsonIgnore
    // @XmlTransient annotation for some reason has to be applied on method
    // level to be working
    @XmlTransient
    public Employment getCurrentEmployment() {
        if (currentEmployment == null && this.employments.size() > 0) {
            currentEmployment = this.employments.iterator().next();
        }
        return currentEmployment;
    }

    /**
     * @param currentEmployment the currentEmployment to set
     * @throws IllegalArgumentException if the given employment is not in the list of employments
     *                                  returned by {@link #getEmployments()}.
     */
    public void setCurrentEmployment(Employment currentEmployment) {
        if (this.employments.contains(currentEmployment)) {
            this.currentEmployment = currentEmployment;
        } else {
            throw new IllegalArgumentException(
                    "Given employment not in list of employments!");
        }
    }

    @JsonIgnore
    @XmlTransient
    public Department getCurrentDepartment() {
        Employment emp = getCurrentEmployment();
        if (emp == null) {
            return null;
        }
        return emp.getDepartment();
    }

    @JsonIgnore
    @XmlTransient
    public Company getCurrentCompany() {
        Employment emp = getCurrentEmployment();
        if (emp == null) {
            return null;
        }
        return emp.getCompany();
    }

    @XmlTransient
    @JsonIgnore
    public UserRole getCurrentRole() {
        Employment emp = getCurrentEmployment();
        if (emp == null) {
            return UserRole.USER;
        }
        return emp.getRole();
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @XmlTransient
    @JsonIgnore
    public boolean hasAnyRoles(UserRole... role) {
        if (isSuperAdmin()) {
            return true;
        }
        UserRole userRole = getCurrentRole();
        return userRole.hasAnyRoles(role);
    }

    @XmlTransient
    @JsonIgnore
    public boolean isSuperAdmin() {
        for (Employment employment : getEmployments()) {
            if ((employment != null) && (employment.getRole() != null)
                    && (employment.getRole() == UserRole.SUPER_ADMIN)) {
                return true;
            }
        }
        return false;
    }

    @XmlTransient
    @JsonIgnore
    public boolean authenticatesThroughLDAP() {
        return StringUtils.isNotEmpty(ldapLogin);
    }

    @XmlTransient
    @JsonIgnore
    public boolean authenticatesThroughLinkedIn() {
        return StringUtils.isNotEmpty(linkedInId);
    }

    @XmlTransient
    @JsonIgnore
    public boolean authenticatesThroughFacebook() {
        return StringUtils.isNotEmpty(facebookId);
    }

    public static User fromActiveDirectoryUser(ActiveDirectoryUser adUser) {
        User user = new User();
        user.setFirstName(adUser.getFirstname());
        user.setLastName(adUser.getSurname());
        user.setNickName(adUser.getCommonName());
        user.setEmail(adUser.getEMail());
        user.setLdapLogin(adUser.getLogin());
        user.setRegistrationStatus(RegistrationStatus.CONFIRMED);
        user.setRegistrationDate(new Date());
        return user;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "User [firstName=" + firstName + ", lastName=" + lastName
                + ", email=" + email + ", birthDate=" + birthDate
                + ", registrationDate=" + registrationDate
                + ", skills=" + skills
                + ", interests=" + interests
                + ", registrationStatus=" + registrationStatus
                + ", registrationToken=" + registrationToken
                + ", resetPWRequestDate=" + resetPWRequestDate
                + ", resetPWRequestToken=" + resetPWRequestToken
                + ", getId()=" + getId() + "]";
    }

    @Transient
    @JsonIgnore
    public File getProfilePictureFile(PictureType type) {
        String path = profilePictureFile.getPath();
        for (PictureType oldType : PictureType.values()) {
            path = path.replace("." + oldType.name().toLowerCase() + ".", "." + type.name().toLowerCase()
                    + ".");
        }
        return new File(path);
    }

    public FileInfo getProfilePictureFile() {
        return profilePictureFile;
    }

    public void setProfilePictureFile(FileInfo profilePictureFile) {
        this.profilePictureFile = profilePictureFile;
    }

    public String getUploadCacheId() {
        return uploadCacheId;
    }

    public void setUploadCacheId(String uploadCacheId) {
        this.uploadCacheId = uploadCacheId;
    }

    public String getLinkedInId() {
        return linkedInId;
    }

    public void setLinkedInId(String linkedInId) {
        this.linkedInId = linkedInId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public Set<PrioritisationDotSplitVotes> getPrioDotVotes() {
        return prioDotVotes;
    }

    public void setPrioDotVotes(Set<PrioritisationDotSplitVotes> prioDotVotes) {
        this.prioDotVotes = prioDotVotes;
    }

    public boolean isReceivingNotifications() {
        return receiveNotifications;
    }

    public void setReceiveNotifications(boolean receiveNotifications) {
        this.receiveNotifications = receiveNotifications;
    }


    public boolean isReceivingChallengeNotifications() {
        return receiveChallengeNotifications;
    }

    public void setReceiveChallengeNotifications(boolean receiveChallengeNotifications) {
        this.receiveChallengeNotifications = receiveChallengeNotifications;
    }

    public boolean isReceivingIdeaNotifications() {
        return receiveIdeaNotifications;
    }

    public void setReceiveIdeaNotifications(boolean receiveIdeaNotifications) {
        this.receiveIdeaNotifications = receiveIdeaNotifications;
    }

    public boolean isReceiveActionNotifications() {
        return receiveActionNotifications;
    }

    public void setReceiveActionNotifications(boolean receiveActionNotifications) {
        this.receiveActionNotifications = receiveActionNotifications;
    }

    public void setCurrentEmployment(Long companyId) {
        for (Employment employment : getEmployments()) {
            if (employment.getCompany().getId().compareTo(companyId) == 0) {
                setCurrentEmployment(employment);
                return;
            }
        }
    }


    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

    public List<Idea> getIdeas() {
        return ideas;
    }

    public void setIdeas(List<Idea> ideas) {
        this.ideas = ideas;
    }

    public List<Company> getCreatedCompanies() {
        return createdCompanies;
    }

    public void setCreatedCompanies(List<Company> createdCompanies) {
        this.createdCompanies = createdCompanies;
    }

    public List<Campaign> getCreatedCampaigns() {
        return createdCampaigns;
    }

    public void setCreatedCampaigns(List<Campaign> createdCampaigns) {
        this.createdCampaigns = createdCampaigns;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return ((User) obj).getId().compareTo(getId()) == 0;
    }

    public List<Tag> getInterests() {
        return interests;
    }

    public void setInterests(List<Tag> interests) {
        this.interests = interests;
    }

    public List<Tag> getSkills() {
        return skills;
    }

    public void setSkills(List<Tag> skills) {
        this.skills = skills;
    }

    public List<String> getInvitedEmailContacts() {
        return invitedEmailContacts;
    }

    public void setInvitedEmailContacts(List<String> invitedEmailContacts) {
        this.invitedEmailContacts = invitedEmailContacts;
    }

    public void addInvitedEmailContacts(String invitedEmailContact) {
        invitedEmailContacts.add(invitedEmailContact);
    }

    public void removeInvitedEmailContacts(String invitedEmailContact) {
        invitedEmailContacts.remove(invitedEmailContact);
    }

    public List<Campaign> getFollowedCampaigns() {
        return followedCampaigns;
    }

    public void setFollowedCampaigns(List<Campaign> followedCampaigns) {
        this.followedCampaigns = followedCampaigns;
    }

    public List<Idea> getFollowedIdeas() {
        return followedIdeas;
    }

    public void setFollowedIdeas(List<Idea> followedIdeas) {
        this.followedIdeas = followedIdeas;
    }

    public List<Action> getFollowedActions() {
        return followedActions;
    }

    public void setFollowedActions(List<Action> followedActions) {
        this.followedActions = followedActions;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public void setFacebookUrl(String facebookUrl) {
        this.facebookUrl = facebookUrl;
    }

    public String getLinkedInUrl() {
        return linkedInUrl;
    }

    public void setLinkedInUrl(String linkedInUrl) {
        this.linkedInUrl = linkedInUrl;
    }

    public String getTwitterUrl() {
        return twitterUrl;
    }

    public void setTwitterUrl(String twitterUrl) {
        this.twitterUrl = twitterUrl;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getNoOfCampaignsLeads() {
        return noOfCampaignsLeads;
    }

    public void setNoOfCampaignsLeads(int noOfCampaignsLeads) {
        this.noOfCampaignsLeads = noOfCampaignsLeads;
    }

    public int getNoOfIdeasLeads() {
        return noOfIdeasLeads;
    }

    public void setNoOfIdeasLeads(int noOfIdeasLeads) {
        this.noOfIdeasLeads = noOfIdeasLeads;
    }

    public int getNoOfActionsLeads() {
        return noOfActionsLeads;
    }

    public void setNoOfActionsLeads(int noOfActionsLeads) {
        this.noOfActionsLeads = noOfActionsLeads;
    }

    public int getNoOfCommentsPosts() {
        return noOfCommentsPosts;
    }

    public void setNoOfCommentsPosts(int noOfCommentsPosts) {
        this.noOfCommentsPosts = noOfCommentsPosts;
    }

    public int getNoOfLikesGiven() {
        return noOfLikesGiven;
    }

    public void setNoOfLikesGiven(int noOfLikesGiven) {
        this.noOfLikesGiven = noOfLikesGiven;
    }

    public int getNoOfLikesReceived() {
        return noOfLikesReceived;
    }

    public void setNoOfLikesReceived(int noOfLikesReceived) {
        this.noOfLikesReceived = noOfLikesReceived;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}

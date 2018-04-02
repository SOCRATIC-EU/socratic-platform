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
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.web.components.resource.PictureType;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Company
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Entity
@XmlRootElement
@Table(name = "companies")
public class Company extends AbstractEntity implements Comparable<Company>, Deletable {

    private static final long serialVersionUID = 1211663122686350829L;

    @NotNull
    @Size(min = 2, max = 200)
    private String shortName;

    @NotNull
    @Size(min = 2, max = 200)
    private String fullName;

    private String ldapSuffix;

    @ManyToOne(targetEntity = Activity.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Size(min = 0, max = 1024)
    @Column(columnDefinition = "varchar(1024)")
    private String services;

    @OneToOne
    private CompanyInnovationAssessment companyInnovationAssessment;

    @OneToMany(orphanRemoval = true, mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<InnovationStrategy> innovationStrategy = new ArrayList<InnovationStrategy>();

    @OneToMany(orphanRemoval = true, mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Employment> employments = new ArrayList<Employment>();

    @OneToMany(orphanRemoval = true, mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Department> departments = new ArrayList<Department>();

    @OneToMany(targetEntity = Campaign.class, orphanRemoval = true, mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Campaign> campaigns = new ArrayList<Campaign>();

    @OneToMany(orphanRemoval = true, mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<InnovationObjective> objectives = new ArrayList<InnovationObjective>();

    @OneToMany(orphanRemoval = true, mappedBy = "company", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Network> networks = new ArrayList<Network>();

    @OneToMany(orphanRemoval = true, mappedBy = "companyNetworked", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Network> networksNetworked = new ArrayList<Network>();

    @OneToMany(mappedBy = "company", orphanRemoval = true, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Scope> scopes = new ArrayList<Scope>();

    private Boolean deleted = Boolean.FALSE;

    private String uploadCacheId = UUID.randomUUID().toString();

    private Boolean active;

    @ManyToOne(targetEntity = User.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private User createdBy;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private FileInfo companyLogoFile;

    //contact information fields..
    @Size(min = 1, max = 200)
    private String streetName;

    private String additionalStreetOption;

    @Size(min = 1, max = 200)
    private String city;

    @Size(min = 1, max = 200)
    private String zipCode;

    @Size(min = 1, max = 200)
    private String state;

    @Size(min = 1, max = 200)
    private String country;

    private String eMail;

    private String telephoneNumber;

    public List<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Network> networks) {
        this.networks = networks;
    }

    public List<Network> getNetworksNetworked() {
        return networksNetworked;
    }

    public void setNetworksNetworked(List<Network> networksNetworked) {
        this.networksNetworked = networksNetworked;
    }

    public CompanyInnovationAssessment getCompanyInnovationAssessment() {
        return companyInnovationAssessment;
    }

    public void setCompanyInnovationAssessment(
            CompanyInnovationAssessment companyInnovationAssessment) {
        this.companyInnovationAssessment = companyInnovationAssessment;
    }

    public List<InnovationStrategy> getInnovationStrategy() {
        return innovationStrategy;
    }

    public void setInnovationStrategy(
            List<InnovationStrategy> innovationStrategy) {
        this.innovationStrategy = innovationStrategy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param name the name to set
     */
    public void setFullName(String name) {
        this.fullName = name;
    }

    public String getUploadCacheId() {
        return uploadCacheId;
    }

    public void setUploadCacheId(String uploadCacheId) {
        this.uploadCacheId = uploadCacheId;
    }

    /**
     * @return the activity
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     * @param activity
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     * @return the services
     */
    public String getServices() {
        return services;
    }

    /**
     * @param services the services to set
     */
    public void setServices(String services) {
        this.services = services;
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
     * @return the createdBy
     */
    @XmlTransient
    @JsonIgnore
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
     * @param innovationStrategy
     */
    public void addInnovationStrategy(InnovationStrategy innovationStrategy) {
        this.innovationStrategy.add(innovationStrategy);
    }

    public String getLdapSuffix() {
        return ldapSuffix;
    }

    public void setLdapSuffix(String ldapSuffix) {
        this.ldapSuffix = ldapSuffix;
    }

    public FileInfo getCompanyLogoFile() {
        return companyLogoFile;
    }

    public void setCompanyLogoFile(FileInfo companyLogoFile) {
        this.companyLogoFile = companyLogoFile;
    }


    @Transient
    @JsonIgnore
    public File getCompanyLogoFile(PictureType type) {
        String path = companyLogoFile.getPath();
        for (PictureType oldType : PictureType.values()) {
            path = path.replace("." + oldType.name().toLowerCase() + ".", "." + type.name().toLowerCase()
                    + ".");
        }
        return new File(path);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Company [shortName=" + shortName + ", fullName=" + fullName
                + ", activity=" + activity + ", services=" + services
                + ", deleted=" + deleted + ", getId()=" + getId() + ", logo=" + companyLogoFile + "]";
    }

    @Override
    public int compareTo(Company o) {
        if (this.shortName != null && o.getShortName() == null)
            return 1;
        if (this.shortName == null && o.getShortName() != null)
            return -1;
        return this.shortName.compareTo(o.getShortName());
    }

    public String getShortServices(int n) {
        if (services == null) {
            return "";
        } else {
            if (services.length() > n) {
                return services.substring(0, n) + "...";
            } else {
                return services;
            }
        }
    }

    public List<Employment> getEmployments() {
        return employments;
    }

    public void setEmployments(List<Employment> employments) {
        this.employments = employments;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<InnovationObjective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<InnovationObjective> objectives) {
        this.objectives = objectives;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getAdditionalStreetOption() {
        return additionalStreetOption;
    }

    public void setAdditionalStreetOption(String additionalStreetOption) {
        this.additionalStreetOption = additionalStreetOption;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return ((Company) obj).getId().compareTo(getId()) == 0;
    }

}

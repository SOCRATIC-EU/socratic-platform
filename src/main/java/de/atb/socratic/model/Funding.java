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

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

@Entity
@XmlRootElement
@Table(name = "fundings")
public class Funding extends AbstractEntity {

    private static final long serialVersionUID = 4751535371499728451L;

    @NotNull
    private String title;

    @Lob
    @Size(max = 65535)
    @Column(length = 65535)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String description;

    @Lob
    @Size(max = 65535)
    @Column(length = 65535)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String requirements;

    @NotNull
    private String fBody;

    @NotNull
    private String fSource;

    @NotNull
    private String sector;

    @NotNull
    private String budget;

    @NotNull
    private String eligibility;

    @NotNull
    private String appType;

    @NotNull
    private Date deadline;

    @NotNull
    private String website;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private Company createdBy;

    private boolean publicFunding = false;

    public Company getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Company createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isPublicFunding() {
        return publicFunding;
    }

    public void setPublicFunding(boolean publicFunding) {
        this.publicFunding = publicFunding;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getfBody() {
        return fBody;
    }

    public void setfBody(String fBody) {
        this.fBody = fBody;
    }

    public String getfSource() {
        return fSource;
    }

    public void setfSource(String fSource) {
        this.fSource = fSource;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getEligibility() {
        return eligibility;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

}

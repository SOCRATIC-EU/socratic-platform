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


import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "company_projects")
public class CompanyProjects extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1700067315469184151L;

    /**
     *
     */


    @NotNull
    @Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String shortName;

    @NotNull
    @Size(min = 10, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String fullName;

    @NotNull
    // Check User.java @Pattern(regexp = "[1-9][0-9]{0,8}", message = "must contain only numbers")
    @Size(min = 4, max = 4)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String year;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String description;


    //@NotNull
    @ManyToOne
    private FundType fundType;

    //@NotNull
    @ManyToOne
    private FundOrigin fundOrigin;


    public FundOrigin getFundOrigin() {
        return fundOrigin;
    }

    public void setFundOrigin(FundOrigin fundOrigin) {
        this.fundOrigin = fundOrigin;
    }

    public FundType getFundType() {
        return fundType;
    }

    public void setFundType(FundType fundType) {
        this.fundType = fundType;
    }


    //@NotNull
    //@Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String prjCost;

    //@NotNull
    //@Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String costPercent;

    private Boolean deleted = Boolean.FALSE;


//	@NotNull
//	@ManyToOne
//	private User postedBy;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getPrjCost() {
        return prjCost;
    }

    public void setPrjCost(String prjCost) {
        this.prjCost = prjCost;
    }

    public String getCostPercent() {
        return costPercent;
    }

    public void setCostPercent(String costPercent) {
        this.costPercent = costPercent;
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
     *
     */
    public CompanyProjects() {
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CompanyProjects [shortName=" + shortName + ", fullName=" + fullName + ",year=" + year
                + ",description=" + description + ",fundType=" + fundType + ",fundOrigin=" + fundType + ",prjCost=" + prjCost + ",getId()=" + getId()
                + "]";
    }

}

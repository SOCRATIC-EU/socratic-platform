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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Employment
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@XmlRootElement
@Entity
@Table(name = "employments")
public class Employment extends AbstractEntity {

    private static final long serialVersionUID = -4120272334942636164L;

    @NotNull
    @ManyToOne(targetEntity = Company.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Company company;

    @ManyToOne(targetEntity = Department.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private Department department;

    @NotNull
    @ManyToOne(targetEntity = User.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private User user;

    @Column(nullable = false, columnDefinition = "tinyint(1) default 1")
    private Boolean active = Boolean.TRUE;

    private Boolean deleted = Boolean.FALSE;

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    //PGS
    private Boolean hasCompletedQuestionnaire = Boolean.FALSE;

    public Boolean getHasCompletedQuestionnaire() {
        return hasCompletedQuestionnaire;
    }

    public void setHasCompletedQuestionnaire(Boolean hasCompletedQuestionnaire) {
        this.hasCompletedQuestionnaire = hasCompletedQuestionnaire;
    }

    @OneToOne
    private EmploymentInnovationAssessment empAssessment;

    public EmploymentInnovationAssessment getEmploymentInnovationAssessment() {
        return empAssessment;
    }

    public void setEmploymentInnovationAssessment(
            EmploymentInnovationAssessment employmentInnovationAssessment) {
        this.empAssessment = employmentInnovationAssessment;
    }

    public Employment() {

    }

    public Employment(User user, Department department) {
        this.user = user;
        this.department = department;
        this.company = department.getCompany();
    }

    public Employment(User user, Company company) {
        this.user = user;
        this.company = company;
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
     * @return the departement
     */
    public Department getDepartment() {
        return department;
    }

    /**
     * @param departement the departement to set
     */
    public void setDepartment(Department departement) {
        this.department = departement;
    }

    /**
     * @return the user
     */
    @XmlTransient
    @JsonIgnore
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the role
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * @param role the role to set
     */
    public void setRole(UserRole role) {
        this.role = role;
    }


    @Override
    public String toString() {
        return "Employment [user=" + user + ", company=" + company + ", department=" + department + ", getId()=" + getId() + "]";
    }

    public String toMenuBarString() {
        String name = department != null ? company.getShortName() + " (" + department.getName() + ")" : company.getShortName();
        return name + " " + getRole().name().substring(0, 1);
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}

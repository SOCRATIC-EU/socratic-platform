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
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.Length;


/**
 * Company
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Entity
@XmlRootElement
@Table(name = "employment_kf")
public class EmploymentInnovationKeyFactors extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = -1593747872775784643L;


    @NotNull
    @Length(min = 2)
    private String name;


    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinTable(name = "eikf_ciaq", joinColumns = @JoinColumn(name = "EmploymentIKF_id"), inverseJoinColumns = @JoinColumn(name = "ciasq_id"))
    private List<CompanyInnovationAssessmentQuestions> innovationAssessmentQuestions = new ArrayList<CompanyInnovationAssessmentQuestions>();


    public List<CompanyInnovationAssessmentQuestions> getInnovationAssessmentQuestions() {
        return innovationAssessmentQuestions;
    }

    public void setInnovationAssessmentQuestions(
            List<CompanyInnovationAssessmentQuestions> innovationAssessmentQuestions) {
        this.innovationAssessmentQuestions = innovationAssessmentQuestions;
    }

    //*******************************************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * @param innovationAssessmentQuestion
     */
    public void addInnovationAssessmentQuestions(CompanyInnovationAssessmentQuestions innovationAssessmentQuestion) {
        this.innovationAssessmentQuestions.add(innovationAssessmentQuestion);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "InnovationKeyFactors [name=" + name
                + ", getId()=" + getId() + "]";
    }

}

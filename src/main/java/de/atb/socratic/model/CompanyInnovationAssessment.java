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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "company_assessment")
public class CompanyInnovationAssessment extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = 4861408143472530127L;

    /**
     *
     */


    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    private List<InnovationKeyFactors> keyFactors = new ArrayList<InnovationKeyFactors>();


    //********************************************************************************

    public List<InnovationKeyFactors> getKeyFactors() {
        return keyFactors;
    }

    public void setKeyFactors(List<InnovationKeyFactors> keyFactors) {
        this.keyFactors = keyFactors;
    }


    private Boolean deleted = Boolean.FALSE;


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
    public CompanyInnovationAssessment() {
    }

    /**
     * @param keyFactors
     */
    public void addKeyFactors(InnovationKeyFactors keyFactors) {
        this.keyFactors.add(keyFactors);
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "InnovationAssessment [getId()=" + getId()
                + "]";
    }

}

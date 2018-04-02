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
@Table(name = "questions")
public class InnovationAssessmentQuestions extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = -4897964620246438589L;

    @NotNull
    @Length(min = 2)
    private String question;

    private Boolean deleted = Boolean.FALSE;

    //****************************************************************

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "InnovationAssessmentQuestions [question=" + question
                + ", deleted=" + deleted + ", getId()=" + getId() + "]";
    }

}

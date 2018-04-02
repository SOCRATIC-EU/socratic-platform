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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;

/**
 * @author ATB
 */
@Entity
@XmlRootElement
@Table(name = "strategy")
public class InnovationStrategy extends AbstractEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1700067315469184151L;

    @ManyToOne(targetEntity = Company.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @IndexedEmbedded
    private Company company;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }


    @NotNull
    @Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @NotNull
    @Size(min = 3, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String scope;

    @NotNull
    @Size(min = 10, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String period;

    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String innovationObjectives;

    @OneToMany(orphanRemoval = true)
    private List<InnovationStrategyObjectives> objectives = new ArrayList<InnovationStrategyObjectives>();


    public List<InnovationStrategyObjectives> getInnovationStrategyObjectives() {
        return objectives;
    }

    public void setInnovationStrategyObjectives(
            List<InnovationStrategyObjectives> innovationStrategyObjectives) {
        this.objectives = innovationStrategyObjectives;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getInnovationObjectives() {
        return innovationObjectives;
    }

    public void setInnovationObjectives(String innovationObjectives) {
        this.innovationObjectives = innovationObjectives;
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
     * @param innovationStrategyObjective
     */
    public void addInnovationStrategy(InnovationStrategyObjectives innovationStrategyObjective) {
        this.objectives.add(innovationStrategyObjective);
    }

    /**
     *
     */
    public InnovationStrategy() {
    }

    /**
     * @param objective
     */
    public void removeObjective(InnovationStrategyObjectives objective) {
        this.objectives.remove(objective);
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "CompanyProjects [name=" + name + ", scope=" + scope + ", period=" + period + ",innovationObjectives=" + innovationObjectives
                + ",getId()=" + getId()
                + "]";
    }

}

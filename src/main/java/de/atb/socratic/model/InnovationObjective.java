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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "objectives")
public class InnovationObjective extends AbstractEntity {

    private static final long serialVersionUID = -3089711111446139113L;

    @NotNull
    @Size(min = 1, max = 200)
    @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
    private String name;

    private Boolean active = Boolean.TRUE;
    ;

    private Boolean deleted = Boolean.FALSE;

    @OneToMany(orphanRemoval = true, mappedBy = "innovationObjective", cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    private List<Campaign> campaigns = new ArrayList<Campaign>();

    @NotNull
    @ManyToOne(targetEntity = Company.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "company_id")
    private Company company;

    public InnovationObjective() {
    }

    public InnovationObjective(Company company) {
        this.company = company;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "InnovationObjective [name=" + name + ", active=" + active
                + ", deleted=" + deleted + "]";
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

}

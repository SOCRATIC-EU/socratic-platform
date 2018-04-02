package de.atb.socratic.model.scope;

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
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import de.atb.socratic.model.AbstractEntity;
import de.atb.socratic.model.Company;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;

/**
 * Scope
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Entity
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "@class")
public class Scope extends AbstractEntity {
    public Scope() {

    }

    public Scope(ScopeType scopeType) {
        super();
        this.scopeType = scopeType;
    }

    private static final long serialVersionUID = -4753141643123976325L;

    @ManyToOne(targetEntity = Company.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;

    @NotNull
    @Enumerated(EnumType.STRING)
    protected ScopeType scopeType;

    /**
     * @return
     */
    public Company getCompany() {
        return company;
    }

    /**
     * @param company
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @return the scopeType
     */
    public ScopeType getScopeType() {
        return scopeType;
    }

    /**
     * @param scopeType the scopeType to set
     */
    public void setScopeType(ScopeType scopeType) {
        this.scopeType = scopeType;
    }

}

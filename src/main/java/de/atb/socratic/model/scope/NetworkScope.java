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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.PostLoad;
import javax.xml.bind.annotation.XmlRootElement;

import de.atb.socratic.model.Company;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.jboss.seam.transaction.TransactionPropagation;
import org.jboss.seam.transaction.Transactional;

/**
 * CampaignNetworkScope
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Entity
@XmlRootElement
public class NetworkScope extends Scope {

    private static final long serialVersionUID = 5925293757003836539L;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.TRUE)
    @Cascade(value = {org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.DELETE})
    private Set<Company> networkedCompanies = new HashSet<Company>();

    public NetworkScope() {
        this.scopeType = ScopeType.NETWORK_ALL;
    }

    @PostLoad
    @Transactional(TransactionPropagation.REQUIRED)
    private void initCommentCount() {
        networkedCompanies.size();
    }

    public Set<Company> getNetworkedCompanies() {
        return networkedCompanies;
    }

    public void setNetworkedCompanies(Set<Company> networkedCompanies) {
        this.networkedCompanies = networkedCompanies;
    }

}

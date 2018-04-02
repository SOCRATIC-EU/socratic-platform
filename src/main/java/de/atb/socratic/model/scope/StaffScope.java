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

import de.atb.socratic.model.Department;
import de.atb.socratic.model.User;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.jboss.seam.transaction.TransactionPropagation;
import org.jboss.seam.transaction.Transactional;

/**
 * CampaignStaffScope
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Entity
@XmlRootElement
public class StaffScope extends Scope {

    private static final long serialVersionUID = 8741331245734871396L;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.TRUE)
    private Set<Department> departments = new HashSet<Department>();

    @ManyToMany(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.TRUE)
    protected Set<User> users = new HashSet<User>();

    public StaffScope() {
        this.scopeType = ScopeType.STAFF_ALL;
    }

    @PostLoad
    @Transactional(TransactionPropagation.REQUIRED)
    private void initCommentCount() {
        if (this.scopeType != ScopeType.STAFF_DEPARTMENTS) {
            users.size();
        } else {
            departments.size();
        }
    }


    /**
     * @return the users
     */
    @Transactional(TransactionPropagation.REQUIRED)
    public Set<User> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    /**
     * @return the departments
     */
    @Transactional(TransactionPropagation.REQUIRED)
    public Set<Department> getDepartments() {
        return departments;
    }

    /**
     * @param departments the departments to set
     */
    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }
}

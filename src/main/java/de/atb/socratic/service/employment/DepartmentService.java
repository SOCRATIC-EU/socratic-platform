package de.atb.socratic.service.employment;

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

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Department;
import de.atb.socratic.model.Department_;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.Employment_;
import de.atb.socratic.model.User;
import de.atb.socratic.service.AbstractService;

/**
 * DepartmentService
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Stateless
public class DepartmentService extends AbstractService<Department> {

    public DepartmentService() {
        super(Department.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = -1134119421569979486L;

    /**
     * @return
     */
    public List<Department> getAllSortedByName() {
        logger.info("loading all departments sorted by name ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Department> criteria = cb
                .createQuery(Department.class);
        Root<Department> root = criteria
                .from(Department.class);
        criteria.orderBy(cb.asc(root.get(Department_.name)));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * Returns all users that are associated with the given departments.
     *
     * @param departments the departments to get the users of.
     * @return a list of users belonging to the given departments sorted by
     * their names in ascending order.
     */
    public long getCountUsersForDepartment(Department departments) {

        logger.infof("loading all users for %s department ordered by ascending name ...",
                departments.getName());

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, User> join = root.join(Employment_.user);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(root.get(Employment_.department).in(departments));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.distinct(true);
        criteria.select(join);
        return em.createQuery(criteria).getResultList().size();
    }


    /**
     * @param department
     * @return
     */
    @Override
    public Department create(Department department) {
        department = createOrUpdate(department);
        logger.infof("persisted department %s ...", department);
        return department;
    }

    /**
     * @param department
     * @return
     */
    private Department createOrUpdate(Department department) {
        department = em.merge(department);
        return department;
    }

    /**
     * @param department
     */
    public void deleteDepartment(Department department) {
        department = em.merge(department);
        em.remove(department);
        logger.infof("deleted department %s ...", department);
    }

}

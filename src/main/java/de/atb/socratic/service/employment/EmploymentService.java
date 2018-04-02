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

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Company;
import de.atb.socratic.model.Department;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.Employment_;
import de.atb.socratic.model.User;
import de.atb.socratic.service.AbstractService;

/**
 * EmployeeService
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Stateless
public class EmploymentService extends AbstractService<Employment> {

    private static final long serialVersionUID = -182529754927561772L;

    public EmploymentService() {
        super(Employment.class);
    }

    /**
     * @param company
     * @param user
     * @return
     */
    public Employment getEmploymentByCompanyAndUser(Company company, User user) {
        logger.infof("loading employment for user with email %s and company %s ...", user.getEmail(), company.getShortName());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employment> criteria = cb.createQuery(Employment.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.where(cb.and(cb.equal(root.get(Employment_.company), company), cb.equal(root.get(Employment_.user), user)));
        return em.createQuery(criteria).setMaxResults(1).getSingleResult();
    }

    public List<Employment> getByUser(User user) {
        logger.infof("loading employments for user with email %s", user.getEmail());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employment> criteria = cb.createQuery(Employment.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.where(cb.equal(root.get(Employment_.user), user));
        return em.createQuery(criteria).getResultList();
    }

    public Long countByUser(User user) {
        logger.infof("loading employments for user with email %s", user.getEmail());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.where(cb.equal(root.get(Employment_.user), user));
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * Mark campaign as deleted but do not really remove from DB.
     *
     * @param employmentId
     */
    public void softDelete(Long employmentId) {
        Employment employment = getById(employmentId);
        softDelete(employment);
    }

    private List<Employment> getEmploymentsFromCompany(Company company) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employment> criteria = cb.createQuery(Employment.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.where(cb.equal(root.get(Employment_.company), company));
        return em.createQuery(criteria).getResultList();
    }

    public void softDelete(Company company) {
        List<Employment> employments = getEmploymentsFromCompany(company);
        for (Employment e : employments) {
            em.remove(e);
        }
    }

    /**
     * Mark campaign as deleted but do not really remove from DB.
     *
     * @param employment
     */
    public void softDelete(Employment employment) {
        employment.setDeleted(true);
        update(employment);
    }

    /**
     * @param department
     * @return
     */
    public long getCountEmployeesByDepartments(Department department) {
        logger.infof("loading employees with department %s ...", department);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.where(cb.and(cb.equal(root.get(Employment_.department), department)));
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param department
     * @return
     */
    public List<Employment> getEmploymentsByDepartments(Department department) {
        logger.infof("loading employees with department %s ...", department);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employment> criteria = cb.createQuery(Employment.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.where(cb.and(cb.equal(root.get(Employment_.department), department)));
        return em.createQuery(criteria).getResultList();
    }
}

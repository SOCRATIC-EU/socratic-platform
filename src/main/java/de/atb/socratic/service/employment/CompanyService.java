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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Company;
import de.atb.socratic.model.CompanyInnovationAssessment;
import de.atb.socratic.model.Company_;
import de.atb.socratic.model.Department;
import de.atb.socratic.model.Department_;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.Employment_;
import de.atb.socratic.model.InnovationStrategy;
import de.atb.socratic.model.InnovationStrategy_;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.User_;
import de.atb.socratic.service.AbstractService;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.WildcardContext;

/**
 * CompanyService
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@Stateless
public class CompanyService extends AbstractService<Company> {

    /**
     *
     */
    private static final long serialVersionUID = 224168038565079338L;

    /**
     *
     */
    public CompanyService() {
        super(Company.class);
    }

    /**
     * Returns all departments that are associated with the company specified
     * through its id.
     *
     * @param companyId the Id of the company to get the departments of.
     * @return a list of departments belonging to the given company sorted by
     * their names in ascending order.
     */
    public List<Department> getAllDepartmentsForCompanyByAscendingName(
            Long companyId) {
        return getAllDepartmentsForCompanyByAscendingName(getById(companyId));
    }

    public long countAllDepartmentsForCompanyByAscendingName(Long companyId) {
        return getAllDepartmentsForCompanyByAscendingName(getById(companyId))
                .size();
    }

    /**
     * Returns all departments that are associated with the given company.
     *
     * @param company the company to get the departments of.
     * @return a list of departments belonging to the given company sorted by
     * their names in ascending order.
     */
    public List<Department> getAllDepartmentsForCompanyByAscendingName(
            Company company) {
        logger.infof(
                "loading all departments for company %s (id = %d) ordered by ascending name ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Department> criteria = cb.createQuery(Department.class);
        Root<Department> root = criteria.from(Department.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Department_.company), company));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.orderBy(cb.asc(root.get(Department_.name)));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * Returns N departments that are associated with the given company.
     *
     * @param company the company to get the departments of.
     * @return a list of departments belonging to the given company sorted by
     * their names in ascending order.
     */
    public List<Department> getDepartmentsForCompanyByAscendingName(
            Company company, int first, int count) {
        logger.infof(
                "loading %d departments for company %s (id = %d) ordered by ascending name ...",
                count, company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Department> criteria = cb.createQuery(Department.class);
        Root<Department> root = criteria.from(Department.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Department_.company), company));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.orderBy(cb.asc(root.get(Department_.name)));
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * Returns all departments that are associated with the given company.
     *
     * @param company the company to get the departments of.
     * @return a list of departments belonging to the given company sorted by
     * their names in ascending order.
     */
    public List<Department> getAllDepartmentsWithEmployeesForCompanyByAscendingName(
            Company company) {
        if (null == company) {
            return new ArrayList<Department>();
        }
        logger.infof(
                "loading all departments for company %s (id = %d) ordered by ascending name ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Department> criteria = cb.createQuery(Department.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, Department> departments = root
                .join(Employment_.department);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(departments.get(Department_.company), company));
        predicates.add(cb.isNotNull(root.get(Employment_.user)));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.orderBy(cb.asc(departments.get(Department_.name)));
        criteria.select(departments).distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * Returns all users that are associated with the given departments.
     *
     * @param departments the departments to get the users of.
     * @return a list of users belonging to the given departments sorted by
     * their names in ascending order.
     */
    public List<User> getAllUsersForDepartmentsByAscendingName(
            Department... departments) {
        return getAllUsersForDepartmentsByAscendingName(Arrays
                .asList(departments));
    }

    /**
     * Returns all users that are associated with the given departments.
     *
     * @param departments the departments to get the users of.
     * @return a list of users belonging to the given departments sorted by
     * their names in ascending order.
     */
    public List<User> getAllUsersForDepartmentsByAscendingName(
            Collection<Department> departments) {
        if (departments.size() == 1) {
            logger.infof(
                    "loading all users for department %s (id = %d) ordered by ascending name ...",
                    departments.iterator().next().getName(), departments
                            .iterator().next().getId());
        } else {
            logger.infof(
                    "loading all users for %d departments ordered by ascending name ...",
                    departments.size());
        }
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
        criteria.orderBy(cb.asc(join.get(User_.lastName)));
        criteria.distinct(true);
        criteria.select(join);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * Returns all users that are associated with the company specified through
     * its id.
     *
     * @param companyId the Id of the company to get the users of.
     * @return a list of users belonging to the given company sorted by their
     * last name in ascending order.
     */
    public List<User> getAllUsersByAscendingLastName(Long companyId) {
        return getAllUsersByAscendingLastName(getById(companyId));
    }

    /**
     * Returns all users that are associated with the given company.
     *
     * @param company the company to get the users of.
     * @return a list of users belonging to the given company sorted by their
     * last name in ascending order.
     */
    public List<User> getAllUsersByAscendingLastName(Company company) {
        if (null == company) {
            return new ArrayList<User>();
        }
        logger.infof(
                "loading all users for company %s (id = %d) ordered by ascending last name ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, User> join = root.join(Employment_.user);
        criteria.select(join);
        criteria.where(cb.equal(root.get(Employment_.company), company),
                cb.equal(root.get(Employment_.deleted), false));
        criteria.orderBy(cb.asc(join.get(User_.lastName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * Returns N users that are associated with the given company.
     *
     * @param company the company to get the users of.
     * @param first   set the first result
     * @param count   set the max number of results
     * @return a list of users belonging to the given company sorted by their
     * last name in ascending order.
     */

    public List<User> getUsersByAscendingLastName(Company company, int first, int count) {
        if (null == company) {
            return new ArrayList<User>();
        }
        logger.infof(
                "loading %d users for company %s (id = %d) ordered by ascending last name ...",
                count, company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, User> join = root.join(Employment_.user);
        criteria.select(join);
        criteria.where(cb.equal(root.get(Employment_.company), company),
                cb.equal(root.get(Employment_.deleted), false));
        criteria.orderBy(cb.asc(join.get(User_.lastName)));
        criteria.distinct(true);
        return em.createQuery(criteria).setFirstResult(first).setMaxResults(count).getResultList();
    }

    /**
     * Returns all users that are associated with the given company.
     *
     * @param companies the companies to get the users of.
     * @return a list of users belonging to the given company sorted by their
     * last name in ascending order.
     */
    public List<User> getAllUsersByAscendingLastName(
            Collection<Company> companies) {
        if (companies.size() > 0) {
            logger.infof("loading all users ordered by ascending last name for company %s ...", companies);
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<User> criteria = cb.createQuery(User.class);
            Root<Employment> root = criteria.from(Employment.class);
            Join<Employment, User> join = root.join(Employment_.user);
            criteria.select(join);
            criteria.where(cb.isTrue(root.get(Employment_.company).in(companies)),
                    cb.equal(root.get(Employment_.deleted), false));
            criteria.orderBy(cb.asc(join.get(User_.lastName)));
            criteria.distinct(true);
            return em.createQuery(criteria).getResultList();
        } else {
            return Collections.emptyList();
        }
    }

    public List<User> getManagerUsersByAscendingLastName(Company company) {
        logger.infof(
                "loading all manager users for company %s (id = %d) ordered by ascending last name ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, User> join = root.join(Employment_.user);
        criteria.select(join);
        criteria.where(cb.equal(root.get(Employment_.company), company),
                cb.equal(root.get(Employment_.deleted), false),
                cb.equal(root.get(Employment_.role), UserRole.MANAGER));
        criteria.orderBy(cb.asc(join.get(User_.lastName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    public List<User> getAdminUsersByAscendingLastName() {
        logger.info("loading all admin users ordered by ascending last name ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, User> join = root.join(Employment_.user);
        criteria.select(join);
        criteria.where(cb.or(cb.equal(root.get(Employment_.role), UserRole.ADMIN), cb.equal(root.get(Employment_.role), UserRole.SUPER_ADMIN)));
        criteria.orderBy(cb.asc(join.get(User_.lastName)));
        criteria.distinct(true);
        return em.createQuery(criteria).getResultList();
    }

    public long getCountAllUsersByAscendingLastName(Company company) {
        logger.infof(
                "loading all users for company %s (id = %d) ordered by ascending last name ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.select(root.get(Employment_.user)).where(
                cb.equal(root.get(Employment_.company), company));
        return em.createQuery(criteria).getResultList().size();
    }


    /**
     * Returns the number of departments
     *
     * @param company
     * @return
     */
    public long getCountAllDepartmensByAscendingLastName(
            Company company) {
        logger.infof(
                "loading departments for company %s (id = %d) ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Department> criteria = cb.createQuery(Department.class);
        Root<Department> root = criteria.from(Department.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Department_.company), company));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        return em.createQuery(criteria).getResultList().size();
    }


    /**
     * Returns all users that are associated with the given company.
     *
     * @param company the company to get the users of.
     * @return a list of users belonging to the given company sorted by their
     * last name in ascending order.
     */

    public Long getCountofUsersFullfillQuestionnaire(Company company) {
        logger.infof(
                "loading all users for company %s (id = %d) ordered by ascending last name ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.select(root.get(Employment_.user))
                .where(cb.equal(root.get(Employment_.company), company),
                        cb.equal(
                                root.get(Employment_.hasCompletedQuestionnaire),
                                true));
        return (long) em.createQuery(criteria).getResultList().size();
    }

    public List<User> getUsersFullfillQuestionnaire(Company company) {
        logger.infof(
                "loading all users for company %s (id = %d) ordered by ascending last name ...",
                company.getShortName(), company.getId());
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<Employment> root = criteria.from(Employment.class);
        criteria.select(root.get(Employment_.user))
                .where(cb.equal(root.get(Employment_.company), company),
                        cb.equal(
                                root.get(Employment_.hasCompletedQuestionnaire),
                                true));
        List<User> collaborators = new ArrayList<User>();
        collaborators = em.createQuery(criteria).getResultList();
        System.out.println(collaborators.size() + "******collaborators.size()");
        for (User p : collaborators) {
            String mainString = p.getNickName().toString();
            System.out.println(mainString + "*******nombres de empleados");
        }
        ;
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @param companyId
     * @return
     */
    @Override
    public Company getById(Long companyId) {
        logger.infof("loading company with ID %d ...", companyId);
        Company company = em.find(Company.class, companyId);
        if ((company == null) || company.getDeleted()) {
            throw new EntityNotFoundException(String.format(
                    "company with id %d does not exist", companyId));
        }
        return company;
    }

    /**
     * Mark company as deleted but do not really remove from DB.
     *
     * @param companyId
     */
    public void softDelete(Long companyId) {
        Company company = getById(companyId);
        softDelete(company);
    }

    /**
     * Mark company as deleted but do not really remove from DB.
     *
     * @param company
     */
    public void softDelete(Company company) {
        // remove all Employment entities for this company
        company.setDeleted(true);
        update(company);
    }

    /**
     * @param companies
     */
    public void softDelete(Collection<Company> companies) {
        for (Company company : companies) {
            softDelete(company);
        }
    }

    @Override
    protected TermMatchingContext setFullTextSearchFields(WildcardContext wc) {
        return wc.onField(Company_.fullName.getName());
    }

    @Override
    protected List<Company> filterFullTextSearchResults(List<Company> results, final User loggedInUser) {
        filterDeleted(results.iterator());
        return results;
    }

    /**
     * @param first
     * @param count
     * @return
     */
    public List<Company> getAllFromToByAscendingName(int first, int count) {
        logger.infof(
                "loading %d companies starting from %d ordered by ascending due date ...",
                count, first);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Company_.deleted)));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.orderBy(cb.asc(root.get(Company_.shortName)));
        return em.createQuery(criteria).setFirstResult(first)
                .setMaxResults(count).getResultList();
    }

    /**
     * @param fullName
     * @return
     */
    public Company getByFullName(String fullName) {
        logger.infof("loading company by full name %s...", fullName);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Company_.deleted)));
        predicates.add(cb.equal(root.get(Company_.fullName), fullName));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param shortName
     * @return
     */
    public Company getByShortName(String shortName) {
        logger.infof("loading company by short name %s...", shortName);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Company_.deleted)));
        predicates.add(cb.equal(root.get(Company_.shortName), shortName));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        return em.createQuery(criteria).getSingleResult();
    }


    /**
     * @param companyId
     * @param questionId
     * @param keyFactorId
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<CompanyInnovationAssessment> getScoresByQuestionAndKeyFactors(
            Long companyId, Long questionId, Long keyFactorId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CompanyInnovationAssessment> criteria = cb
                .createQuery(CompanyInnovationAssessment.class);
        Root<Company> root = criteria.from(Company.class);
        Join<Company, CompanyInnovationAssessment> join = root
                .join(Company_.companyInnovationAssessment);
        criteria.select(join).where(
                cb.and(cb.equal(root.get(Company_.id), companyId)));
        // why is this a list with a single result?!
        return (List<CompanyInnovationAssessment>) em.createQuery(criteria)
                .getSingleResult();
    }

    /**
     * @param companyId
     * @param count
     * @return
     */

    public List<InnovationStrategy> getInnovationStrategyAllForCompanyByDescendingYear(
            Long companyId, int count) {
        logger.infof(
                "loading %d All InnovationStrategy ordered by descending year for company with ID %d ...",
                count, companyId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InnovationStrategy> criteria = cb
                .createQuery(InnovationStrategy.class);
        Root<Company> root = criteria.from(Company.class);
        Join<Company, InnovationStrategy> join = root
                .join(Company_.innovationStrategy);
        criteria.select(join)
                .where(cb.and(cb.equal(root.get(Company_.id), companyId),
                        cb.not(join.get(InnovationStrategy_.deleted))))
                .orderBy(cb.desc(join.get(InnovationStrategy_.period)));
        return em.createQuery(criteria).setMaxResults(count).getResultList();
    }

    /**
     * @param companyId
     * @return
     */
    public long countInnovationStrategyForCompany(Long companyId) {
        logger.infof(
                "counting all InnovationStrategy for company with ID %d ...",
                companyId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Company> root = criteria.from(Company.class);
        Join<Company, InnovationStrategy> join = root
                .join(Company_.innovationStrategy);
        criteria.select(cb.countDistinct(join)).where(
                cb.and(cb.equal(root.get(Company_.id), companyId),
                        cb.not(join.get(InnovationStrategy_.deleted))));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @return
     */
    public long countAllFromToByAscendingName() {
        logger.infof("counting companys ordered by ascending name ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Company_.deleted)));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param fullName
     * @return
     */
    public long countByFullName(String fullName) {
        logger.infof("counting companies by full name %s...", fullName);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Company_.deleted)));
        predicates.add(cb.equal(root.get(Company_.fullName), fullName));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param searchQuery
     * @return
     */
    public Long countFullTextResults(String searchQuery, final User loggedInUser) {
        return Long.valueOf(fullTextSearch(0, Integer.MAX_VALUE, searchQuery, loggedInUser).size());
    }

    @Override
    public long countAll() {
        logger.info("counting all companys ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Company> from = criteria.from(Company.class);
        criteria.select(cb.countDistinct(from));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param companyId
     * @return
     */
    public boolean companyExists(Long companyId) {
        logger.info(String.format("checking if company with ID %d exists ...",
                companyId));
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Company> from = criteria.from(Company.class);
        criteria.where(cb.equal(from.get(Company_.id), companyId));
        criteria.select(cb.countDistinct(from));
        return (em.createQuery(criteria).getSingleResult().longValue() == 1);
    }

    /**
     * @param company
     * @return
     */
    @Override
    public Company create(Company company) {
        company = createOrUpdate(company);
        logger.infof("persisted company %s ...", company);
        return company;
    }

    /**
     * @param company
     * @return
     */
    @Override
    public Company update(Company company) {
        company = createOrUpdate(company);
        logger.infof("updated company %s ...", company);
        return company;
    }

    /**
     * @param company
     * @return
     */
    private Company createOrUpdate(Company company) {
        company = em.merge(company);
        return company;
    }

    /**
     * @return
     */
    public Company getFreeFormCompany() {
        logger.infof("loading free form project ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Company> root = criteria.from(Company.class);
        criteria.where(cb.and(cb.not(root.get(Company_.deleted))));
        return em.createQuery(criteria).getResultList().get(0);
    }

    /**
     * @param companyId
     * @param innovationStrategy
     * @return
     */
    public InnovationStrategy addInnovationStrategy(Long companyId,
                                                    InnovationStrategy innovationStrategy) {
        Company company = getById(companyId);
        innovationStrategy = addInnovationStrategyToCompany(company,
                innovationStrategy);
        logger.infof("added companyProject %s to company with ID %d ...",
                innovationStrategy, companyId);
        return innovationStrategy;
    }

    /**
     * @param company
     * @param innovationStrategy
     * @return
     */
    public InnovationStrategy addInnovationStrategy(Company company,
                                                    InnovationStrategy innovationStrategy) {
        company = em.merge(company);
        innovationStrategy = addInnovationStrategyToCompany(company,
                innovationStrategy);
        logger.infof("added innovationStrategy %s to company %s ...",
                innovationStrategy, company);
        return innovationStrategy;
    }

    /**
     * @param company
     * @param innovationStrategy
     * @return
     */
    private InnovationStrategy addInnovationStrategyToCompany(Company company,
                                                              InnovationStrategy innovationStrategy) {
        innovationStrategy = em.merge(innovationStrategy);
        company.addInnovationStrategy(innovationStrategy);
        em.merge(company);
        return innovationStrategy;
    }

    public Company getEmploymentCompany(Long employmentId) {
        logger.infof("loading company for employee with ID %d ...",
                employmentId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, Company> join = root.join(Employment_.company);
        criteria.select(join).where(
                cb.and(cb.equal(root.get(Employment_.id), employmentId),
                        cb.not(join.get(Company_.deleted))));
        return em.createQuery(criteria).getSingleResult();// .getResultList();
    }

    public List<Employment> getCompanyEmployments(Company company) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Employment> criteria = cb.createQuery(Employment.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, Company> join = root.join(Employment_.company);
        criteria.where(
                cb.and(cb.equal(root.get(Employment_.company), company.getId()),
                        cb.not(join.get(Company_.deleted))));
        return em.createQuery(criteria).getResultList();
    }

    public long getEmploymentCompanyCount(Long employmentId) {
        logger.infof("loading company for employee with ID %d ...",
                employmentId);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Employment> root = criteria.from(Employment.class);
        Join<Employment, Company> join = root.join(Employment_.company);
        criteria.select(join).where(
                cb.and(cb.equal(root.get(Employment_.id), employmentId),
                        cb.not(join.get(Company_.deleted))));
        return em.createQuery(criteria).getResultList().size();// .getResultList();
    }

    public Company findByLDAPDomain(String suffix) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Company> root = criteria.from(Company.class);
        criteria.where(cb.and(
                cb.not(root.get(Company_.deleted)),
                cb.equal(cb.lower(root.get(Company_.ldapSuffix)),
                        suffix.toLowerCase())));
        List<Company> resultList = em.createQuery(criteria).getResultList();
        if (resultList.size() == 0)
            return null;
        return resultList.get(0);
    }

    public List<Company> getAllPendingsExcept(Company company) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.notEqual(root, company));
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        return em.createQuery(criteria).getResultList();
    }

    public List<User> getEmployeesFromCompanyByRoles(Company company, UserRole... roles) {
        List<Employment> employments = getCompanyEmployments(company);
        List<User> ret = new ArrayList<User>();
        List<UserRole> listOfRoles = Arrays.asList(roles);
        for (Employment e : employments) {
            if (listOfRoles.contains(e.getRole())) {
                ret.add(e.getUser());
            }
        }
        return ret;
    }

    public List<Company> getFiltered(Collection<Company> searchCompany, int offset, int count) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Company> criteria = cb.createQuery(Company.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Company_.deleted)));
        if (searchCompany != null && !searchCompany.isEmpty()) {
            predicates.add(root.in(searchCompany));
        }
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.orderBy(cb.asc(root.get(Company_.shortName)));
        return em.createQuery(criteria).setFirstResult(offset)
                .setMaxResults(count).getResultList();
    }

    public long countFiltered(Collection<Company> searchCompany) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Company> root = criteria.from(Company.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.not(root.get(Company_.deleted)));
        if (searchCompany != null && !searchCompany.isEmpty()) {
            predicates.add(root.in(searchCompany));
        }
        if (!predicates.isEmpty()) {
            criteria.where(cb.and(predicates.toArray(new Predicate[predicates
                    .size()])));
        }
        criteria.select(cb.countDistinct(root));
        criteria.orderBy(cb.asc(root.get(Company_.shortName)));
        return em.createQuery(criteria).getSingleResult();
    }
}

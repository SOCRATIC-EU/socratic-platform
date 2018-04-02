/**
 *
 */
package de.atb.socratic.service.inception;

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

import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Company;
import de.atb.socratic.model.InnovationObjective;
import de.atb.socratic.model.InnovationObjective_;
import de.atb.socratic.service.AbstractService;

/**
 * @author ATB
 */
@Stateless
public class InnovationObjectiveService extends
        AbstractService<InnovationObjective> {

    /**
     *
     */
    private static final long serialVersionUID = 657634175557473533L;

    /**
     *
     */
    public InnovationObjectiveService() {
        super(InnovationObjective.class);
    }

    /**
     * @return
     */
    public List<InnovationObjective> getAllSortedByName(Company company) {
        logger.info("loading all innovation objectives sorted by name ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InnovationObjective> criteria = cb
                .createQuery(InnovationObjective.class);
        Root<InnovationObjective> root = criteria
                .from(InnovationObjective.class);
        criteria.where(cb.and(cb.not(root.get(InnovationObjective_.deleted))), cb.equal(root.get(InnovationObjective_.company), company));
        criteria.orderBy(cb.asc(root.get(InnovationObjective_.name)));
        return em.createQuery(criteria).getResultList();
    }

    public List<InnovationObjective> getActiveSortedByName(Company company) {
        logger.info("loading all innovation objectives sorted by name ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InnovationObjective> criteria = cb
                .createQuery(InnovationObjective.class);
        Root<InnovationObjective> root = criteria
                .from(InnovationObjective.class);
        criteria.where(cb.and(cb.and(cb.not(root.get(InnovationObjective_.deleted))), cb.equal(root.get(InnovationObjective_.company), company)), cb.isTrue(root.get(InnovationObjective_.active)));
        criteria.orderBy(cb.asc(root.get(InnovationObjective_.name)));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    public List<InnovationObjective> getObjectivesSortedByName(int first,
                                                               int count, Company company) {
        logger.info("loading all innovation objectives sorted by name ...");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InnovationObjective> criteria = cb
                .createQuery(InnovationObjective.class);
        Root<InnovationObjective> root = criteria
                .from(InnovationObjective.class);
        criteria.where(cb.and(cb.not(root.get(InnovationObjective_.deleted))), cb.equal(root.get(InnovationObjective_.company), company));
        criteria.orderBy(cb.asc(root.get(InnovationObjective_.name)));

        return em.createQuery(criteria).setFirstResult(first)
                .setMaxResults(count).getResultList();
    }

    /**
     * @param name
     * @return
     */
    public long countByName(String name, Company company) {
        logger.infof("counting innovation objective for name %s...", name);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<InnovationObjective> root = criteria
                .from(InnovationObjective.class);
        criteria.where(
                cb.and(cb.and(cb.equal(root.get(InnovationObjective_.name), name)),
                        cb.not(root.get(InnovationObjective_.deleted))),
                cb.equal(root.get(InnovationObjective_.company), company));

        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param name
     * @return
     */
    public InnovationObjective getByName(String name, Company company) {
        logger.infof("loading innovation objective for name %s...", name);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InnovationObjective> criteria = cb
                .createQuery(InnovationObjective.class);
        Root<InnovationObjective> root = criteria
                .from(InnovationObjective.class);
        criteria.where(
                cb.and(cb.and(cb.equal(root.get(InnovationObjective_.name), name)),
                        cb.not(root.get(InnovationObjective_.deleted))),
                cb.equal(root.get(InnovationObjective_.company), company));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param objective
     */
    public void add(InnovationObjective objective) {
        this.create(objective);
    }

    /**
     * @param objective
     */
    public void remove(InnovationObjective objective) {
        this.delete(objective);
    }

    /**
     * @param objective
     */
    public void remove(Collection<InnovationObjective> objective) {
        this.delete(objective);
    }

    /**
     * @param objective
     */
    public void softDelete(InnovationObjective objective) {
        objective.setDeleted(true);
    }

    /**
     * @param objective
     */
    public void softDelete(Collection<InnovationObjective> objective) {
        for (InnovationObjective innovationObjective : objective) {
            innovationObjective.setDeleted(true);
        }
    }

    public long countObjectivesSortedByName(Company company) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<InnovationObjective> root = criteria
                .from(InnovationObjective.class);
        criteria.where(cb.and(cb.not(root.get(InnovationObjective_.deleted))), cb.equal(root.get(InnovationObjective_.company), company));
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    public long countActiveSortedByName(Company company) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<InnovationObjective> root = criteria
                .from(InnovationObjective.class);
        criteria.where(cb.and(cb.and(cb.not(root.get(InnovationObjective_.deleted))), cb.equal(root.get(InnovationObjective_.company), company)), cb.isTrue(root.get(InnovationObjective_.active)));
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

}

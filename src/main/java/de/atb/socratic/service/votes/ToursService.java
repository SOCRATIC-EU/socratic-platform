package de.atb.socratic.service.votes;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.tour.Tour;
import de.atb.socratic.model.tour.Tour_;
import de.atb.socratic.qualifier.Conversational;
import de.atb.socratic.service.AbstractService;

@Conversational
@Singleton
@Startup
@ApplicationScoped
public class ToursService extends AbstractService<Tour> implements Serializable {

    private static final long serialVersionUID = 952226135488371553L;

    public void updateTour(long user, String name, String step, boolean end) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tour> query = cb.createQuery(Tour.class);
        Root<Tour> root = query.from(Tour.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Tour_.user_id), user));
        predicates.add(cb.equal(root.get(Tour_.name), name));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        List<Tour> tours = em.createQuery(query).getResultList();
        if (tours == null || tours.isEmpty()) {
            Tour t = new Tour(name, Long.valueOf(user), Integer.parseInt(step), end);
            t = em.merge(t);
        } else {
            Tour t = tours.get(0);
            t.setCurrent_step(Integer.parseInt(step));
            t.setEnded(end);
            t = em.merge(t);
        }
    }

    public boolean isEnded(long user, String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tour> query = cb.createQuery(Tour.class);
        Root<Tour> root = query.from(Tour.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Tour_.user_id), user));
        predicates.add(cb.equal(root.get(Tour_.name), name));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        List<Tour> tours = em.createQuery(query).getResultList();
        if (tours == null || tours.isEmpty()) {
            return false;
        } else {
            return tours.get(0).isEnded();
        }
    }

    public int getLastStep(long user, String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tour> query = cb.createQuery(Tour.class);
        Root<Tour> root = query.from(Tour.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get(Tour_.user_id), user));
        predicates.add(cb.equal(root.get(Tour_.name), name));
        query.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        List<Tour> tours = em.createQuery(query).getResultList();
        if (tours == null || tours.isEmpty()) {
            return 0;
        } else {
            return tours.get(0).getCurrent_step();
        }
    }

}

/**
 *
 */
package de.atb.socratic.service;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.AbstractEntity_;
import de.atb.socratic.model.Deletable;
import de.atb.socratic.model.User;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.PhraseContext;
import org.hibernate.search.query.dsl.PhraseMatchingContext;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.dsl.WildcardContext;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
public abstract class AbstractService<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4171879462026403888L;

    protected Class<T> clazz;
    protected String readableClassName;

    @SuppressWarnings("unchecked")
    protected AbstractService() {
        this((Class<T>) Void.class);
    }

    /**
     * @param clazz
     */
    protected AbstractService(Class<T> clazz) {
        this.clazz = clazz;
        this.readableClassName = clazz.getSimpleName();
    }

    /**
     * @param clazz
     * @param readableClassName
     */
    protected AbstractService(Class<T> clazz, String readableClassName) {
        this.clazz = clazz;
        this.readableClassName = readableClassName;
    }

    // inject a logger
    @Inject
    protected Logger logger;

    @Inject
    protected EntityManager em;

    /**
     * Detaches entity from persistence context.
     *
     * @param t
     * @return
     */
    public T detach(T t) {
        em.detach(t);
        return t;
    }

    public T getByRandom() {
        Long count = countAll();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(clazz);
        criteria.from(clazz);
        if ((count != null) && (count > 0)) {
            int index = new Random().nextInt(count.intValue());
            List<T> resultList = em.createQuery(criteria).setFirstResult(index).setMaxResults(1).getResultList();
            if (resultList.size() > 0) {
                return resultList.get(0);
            }
        }
        return null;
    }

    /**
     * @return
     */
    @SuppressWarnings("unused")
    public List<T> getAll() {
        logger.infof("loading all %ss ...", readableClassName);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(clazz);
        Root<T> root = criteria.from(clazz);
        return em.createQuery(criteria).getResultList();
    }

    /**
     * @return
     */
    @SuppressWarnings("unused")
    public List<T> getAll(int offset, int count) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteria = cb.createQuery(clazz);
        Root<T> root = criteria.from(clazz);
        return em.createQuery(criteria).setFirstResult(offset).setMaxResults(count).getResultList();
    }

    /**
     * @return
     */
    public long countAll() {
        logger.infof("counting all %ss ...", readableClassName);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<T> root = criteria.from(clazz);
        criteria.select(cb.countDistinct(root));
        return em.createQuery(criteria).getSingleResult();
    }

    /**
     * @param id
     * @return
     */
    public T getById(Long id) {
        logger.infof("loading %s with ID %d ...", readableClassName, id);
        T entity = em.find(clazz, id);
        if (entity == null) {
            throw new EntityNotFoundException(String.format(
                    "%s with id %d does not exist", readableClassName, id));
        }
        return entity;
    }

    /**
     * @param entity
     * @return
     */
    public T create(T entity) {
        entity = update(entity);
        logger.infof("persisted %s %s ...", readableClassName, entity);
        return entity;
    }

    /**
     * @param entity
     * @return
     */
    public T update(T entity) {
        entity = em.merge(entity);
        logger.infof("updated %s %s ...", readableClassName, entity);
        return entity;
    }

    /**
     * @param entityId
     */
    public void delete(Long entityId) {
        T user = em.find(clazz, entityId);
        em.remove(user);
        logger.infof("deleted %s with ID %d ...", readableClassName, entityId);
    }

    /**
     * @param entity
     */
    public void delete(T entity) {
        entity = em.merge(entity);
        em.remove(entity);
        logger.infof("deleted %s %s ...", readableClassName, entity);
    }

    /**
     * @param entities
     */
    public void delete(Collection<T> entities) {
        for (T entity : entities) {
            this.delete(entity);
        }
    }

    /**
     * @param first
     * @param count
     * @param searchQuery
     * @return
     */
    public List<T> fullTextSearch(int first, int count, String searchQuery, final User loggedInUser) {
        FullTextEntityManager ftem = Search.getFullTextEntityManager(em);
        QueryBuilder qb = ftem.getSearchFactory().buildQueryBuilder().forEntity(clazz).get();
        Query luceneQuery = null;
        try {
            WildcardContext wc = qb.keyword().wildcard();
            String[] searchTerms = searchQuery.split("\\s+");
            if (searchTerms.length > 1) {
                PhraseContext pcm = qb.phrase();
                luceneQuery = setFullTextSearchFields(pcm)
                        .sentence(searchQuery.toLowerCase())
                        .createQuery();
            } else {
                TermMatchingContext tmc = setFullTextSearchFields(wc);
                luceneQuery = tmc
                        .ignoreAnalyzer()
                        .ignoreFieldBridge()
                        .matching("*" + searchQuery.toLowerCase() + "*")
                        .createQuery();
            }
        } catch (Exception e) {
            // because of lucene stop words
            logger.error("search exception", e);
            return new ArrayList<T>(0);
        }
        FullTextQuery query = ftem.createFullTextQuery(luceneQuery, clazz);
        List<T> results = query.setFirstResult(first).setMaxResults(count).getResultList();
        results = filterFullTextSearchResults(results, loggedInUser);
        return results;
    }

    protected PhraseMatchingContext setFullTextSearchFields(PhraseContext pc) {
        return pc.onField(AbstractEntity_.id.getName());
    }

    /**
     * Override this to set the fields to search on.
     *
     * @param wc
     * @return
     */
    protected TermMatchingContext setFullTextSearchFields(WildcardContext wc) {
        return wc.onField(AbstractEntity_.id.getName());
    }

    /**
     * Override this to filter full text search stuff.
     *
     * @param results
     * @return
     */
    protected List<T> filterFullTextSearchResults(List<T> results, final User loggedInUser) {
        return results;
    }

    /**
     * @param searchQuery
     * @return
     */
    public Long countFullTextResults(String searchQuery, final User loggedInUser) {
        return Long.valueOf(fullTextSearch(0, Integer.MAX_VALUE, searchQuery, loggedInUser).size());
    }

    /**
     * @param vetterItt
     * @return
     */
    public static Iterator<? extends Deletable> filterDeleted(Iterator<? extends Deletable> vetterItt) {
        while (vetterItt.hasNext()) {
            Deletable d = vetterItt.next();
            if (d.getDeleted()) {
                vetterItt.remove();
            }
        }
        return vetterItt;
    }
}

package de.atb.socratic.service.other;

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
import java.util.LinkedHashSet;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import de.atb.socratic.model.Tag;
import de.atb.socratic.model.Tag_;
import de.atb.socratic.service.AbstractService;

@Stateless
public class TagService extends AbstractService<Tag> {

    public TagService() {
        super(Tag.class);
    }

    /**
     *
     */
    private static final long serialVersionUID = 632625335365363627L;

    public Tag getByTagOrCreate(final String tag) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tag> criteria = cb.createQuery(clazz);
        Root<Tag> root = criteria.from(clazz);
        criteria.where(cb.equal(root.get(Tag_.tag), tag));
        List<Tag> list = em.createQuery(criteria).getResultList();
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return this.create(new Tag(tag));
        }
    }

    public Tag getByTagId(final Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tag> criteria = cb.createQuery(clazz);
        Root<Tag> root = criteria.from(clazz);
        criteria.where(cb.equal(root.get(Tag_.id), id));
        List<Tag> list = em.createQuery(criteria).getResultList();
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public Collection<Tag> getExistingTagsAndCreateMissingTagsFromChoicesString(
            final Collection<String> choiceStrings) {
        // use a set to avoid duplicate tags
        Collection<Tag> finalTags = new LinkedHashSet<>(choiceStrings.size());
        for (String potentialId : choiceStrings) {
            try {
                Long id = Long.parseLong(potentialId);
                // skip tag if the inserted digits are not stored yet
                if (getByTagId(id) == null) {
                    continue;
                }
                // try to find existing tag
                Tag existingTag = getById(id);
                if (existingTag == null) {
                    // not existent yet, create a new tag
                    existingTag = getByTagOrCreate(potentialId);
                }
                finalTags.add(existingTag);
            } catch (NumberFormatException e) {
                // potential id is not a number, create a tag out of it!
                finalTags.add(getByTagOrCreate(potentialId));
            }
        }
        return finalTags;
    }

}

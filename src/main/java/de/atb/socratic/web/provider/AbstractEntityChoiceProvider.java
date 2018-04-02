package de.atb.socratic.web.provider;

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
import java.util.List;

import com.vaynberg.wicket.select2.Response;
import com.vaynberg.wicket.select2.TextChoiceProvider;
import de.atb.socratic.model.AbstractEntity;

/**
 * AbstractChoiceProvider
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public abstract class AbstractEntityChoiceProvider<Type extends AbstractEntity>
        extends TextChoiceProvider<Type> {

    /**
     *
     */
    private static final long serialVersionUID = -9195572998582222759L;

    private List<Type> allEntities = new ArrayList<Type>();

    public AbstractEntityChoiceProvider() {

    }

    public AbstractEntityChoiceProvider(List<Type> allEntities,
                                        Type... entitiesToExclude) {
        this.allEntities = allEntities;

        if ((entitiesToExclude != null) && (entitiesToExclude.length > 0)) {
            // remove the provided entities from the suggestion list
            this.allEntities.removeAll(Arrays.asList(entitiesToExclude));
        }
    }

    public AbstractEntityChoiceProvider(List<Type> entities) {
        this(entities, (Type[]) null);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaynberg.wicket.select2.TextChoiceProvider#getId(java.lang.Object)
     */
    @Override
    protected Object getId(Type entity) {
        return entity.getId();
    }

    @Override
    public void query(String term, int page, Response<Type> response) {
        response.addAll(queryMatches(term, page, 10));
        response.setHasMore(response.size() == 10);
    }

    @Override
    public Collection<Type> toChoices(Collection<String> ids) {
        List<Type> entities = new ArrayList<Type>();
        for (String id : ids) {
            entities.add(findEntityById(Long.valueOf(id)));
        }
        return entities;
    }

    protected void setAllEntities(List<Type> entities) {
        this.allEntities = entities;
    }

    /**
     * Queries {@code pageSize} the list of entities, starting with
     * {@code page * pageSize} offset. Entities should be matched on a String
     * attribute containing {@code term}
     *
     * @param term     search term
     * @param page     starting page
     * @param pageSize items per page
     * @return list of matches
     */
    protected List<Type> queryMatches(String term, final int page,
                                      final int pageSize) {
        List<Type> result = new ArrayList<Type>();
        final int offset = page * pageSize;
        int matched = 0;
        for (Type entity : allEntities) {
            if (result.size() == pageSize) {
                break;
            }
            if (queryMatches(entity, term)) {
                matched++;
                if (matched > offset) {
                    result.add(entity);
                }
            }
        }
        return result;
    }

    /**
     * Determines whether the given entity is matched by the given term.
     *
     * @param entity
     * @param term
     * @return <code>true</code> if the given entity can be found by the given
     * termn, <code>false</code> otherwise.
     */
    protected abstract boolean queryMatches(Type entity, String term);

    /**
     * @param id
     * @return
     */
    private Type findEntityById(final Long id) {
        for (Type type : allEntities) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        return null;
    }

}

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

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.atb.socratic.model.Tag;
import de.atb.socratic.service.other.TagService;
import org.apache.wicket.cdi.CdiContainer;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class TagProvider extends AbstractEntityChoiceProvider<Tag> {

    @Inject
    TagService tagService;

    @Inject
    LoggedInUserProvider userProvider;

    /**
     *
     */
    private static final long serialVersionUID = -340800766625962172L;

    public TagProvider(List<Tag> allEntities) {
        super(allEntities, (Tag[]) null);
        CdiContainer.get().getNonContextualManager().inject(this);
    }

    public TagProvider(List<Tag> allEntities, Tag[] entitiesToExclude) {
        super(allEntities, entitiesToExclude);
        CdiContainer.get().getNonContextualManager().inject(this);
    }

    @Override
    public Collection<Tag> toChoices(Collection<String> ids) {
        return tagService.getExistingTagsAndCreateMissingTagsFromChoicesString(
                ids);
    }

    @Override
    protected boolean queryMatches(Tag entity, String term) {
        return entity.getTag().toUpperCase().contains(term.toUpperCase());
    }

    @Override
    protected String getDisplayText(Tag choice) {
        return choice.getTag();
    }


}

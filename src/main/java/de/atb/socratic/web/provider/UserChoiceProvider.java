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

import java.util.List;

import de.atb.socratic.model.User;

/**
 * UserChoiceProvider
 * <p>
 * {@link User} based choice provider for Select2 user selection component.
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public final class UserChoiceProvider extends
        AbstractEntityChoiceProvider<User> {

    /**
     *
     */
    private static final long serialVersionUID = 4565514108785418223L;

    public UserChoiceProvider(List<User> users) {
        super(users);
    }

    public UserChoiceProvider(List<User> users, User... usersToExclude) {
        super(users, usersToExclude);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.socratic.web.provider.AbstractEntityChoiceProvider#queryMatches(de.
     * atb.socratic.model.AbstractEntity, java.lang.String)
     */
    @Override
    protected boolean queryMatches(User user, String term) {
        return user.getNickName().toUpperCase().contains(term.toUpperCase());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaynberg.wicket.select2.TextChoiceProvider#getDisplayText(java.lang
     * .Object)
     */
    @Override
    protected String getDisplayText(User user) {
        return user.getNickName();
    }

}

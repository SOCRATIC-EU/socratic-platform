package de.atb.socratic.model.event;

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

import de.atb.socratic.model.Tag;
import de.atb.socratic.model.User;

/**
 * This class (event type) will represent new skill or interest added to user's profile. Skills and interests are represented by
 * Tag.
 *
 * @author ATB
 */
public class UserTagAdded {

    private User user;
    private List<Tag> addedSkillsOrInterest = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Tag> getAddedSkillsOrInterest() {
        return addedSkillsOrInterest;
    }

    public void setAddedSkillsOrInterest(List<Tag> addedSkillsOrInterest) {
        this.addedSkillsOrInterest = addedSkillsOrInterest;
    }
}

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

import de.atb.socratic.model.Action;
import de.atb.socratic.model.Tag;

/**
 * This class (event type) will represent new skill or interest added to action. Skills and interests are represented by Tag.
 *
 * @author ATB
 */
public class ActionTagAdded {

    private Action action;
    private List<Tag> addedSkillsOrInterest = new ArrayList<>();

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<Tag> getAddedSkillsOrInterest() {
        return addedSkillsOrInterest;
    }

    public void setAddedSkillsOrInterest(List<Tag> addedSkillsOrInterest) {
        this.addedSkillsOrInterest = addedSkillsOrInterest;
    }
}

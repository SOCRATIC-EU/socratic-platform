package de.atb.socratic.model;

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

import java.util.Arrays;
import java.util.List;

/**
 * @author ATB
 */
public enum UNGoalType {

    // FIXME: use localised message keys here
    GOAL3("Goal 3: Ensuring healthy lives and promote well-being for all at all ages."),
    GOAL4("Goal 4: Ensuring inclusive and equitable quality education and promote lifelong learning opportunities for all."),
    GOAL8("Goal 8: Promoting sustained, inclusive and sustainable economic growth, full and productive employment and decent work for all.");

    private String goal;

    UNGoalType(String goal) {
        this.goal = goal;
    }

    public String getGoal() {
        return goal;
    }

    public static List<UNGoalType> getAll() {
        return Arrays.asList(GOAL3, GOAL4, GOAL8);
    }

    public static List<String> getAllAsString() {
        return Arrays.asList(GOAL3.getGoal(), GOAL4.getGoal(), GOAL8.getGoal());
    }

}

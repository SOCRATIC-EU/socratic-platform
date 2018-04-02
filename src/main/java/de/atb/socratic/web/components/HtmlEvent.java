/**
 *
 */
package de.atb.socratic.web.components;

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

/**
 * @author ATB
 */
public enum HtmlEvent {

    ONSUBMIT("submit"),

    ONCHANGE("change"),

    ONBLUR("blur"),

    // these next two are specifically for capturing events on a bootstrap
    // date picker
    ONCHANGEDATE("changeDate"),

    ONHIDE("hide");

    private String event;

    private HtmlEvent(String event) {
        this.event = event;
    }

    /**
     * @return the event
     */
    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return event;
    }

}

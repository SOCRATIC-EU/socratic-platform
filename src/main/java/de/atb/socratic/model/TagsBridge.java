/**
 *
 */
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

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.search.bridge.StringBridge;

/**
 * @author ATB
 */
public class TagsBridge implements StringBridge {

    /* (non-Javadoc)
     * @see org.hibernate.search.bridge.StringBridge#objectToString(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String objectToString(Object object) {
        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof Collection) {
            List<String> tags = (List<String>) object;
            if (!tags.isEmpty()) {
                return StringUtils.join(tags, ',');
            }
        }
        return null;
    }

}

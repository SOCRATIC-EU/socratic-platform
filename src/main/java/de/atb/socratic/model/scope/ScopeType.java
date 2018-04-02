package de.atb.socratic.model.scope;

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
 * ScopeType
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public enum ScopeType {

    STAFF,

    STAFF_ALL,

    STAFF_DEPARTMENTS,

    STAFF_USERS,

    NETWORK,

    NETWORK_ALL,

    NETWORK_TYPES,

    NETWORK_COMPANIES,

    OPEN;

    public static ScopeType determineGenericType(ScopeType scopeType) {
        if (scopeType != null) {
            switch (scopeType) {
                case OPEN:
                    return OPEN;
                case NETWORK:
                case NETWORK_ALL:
                case NETWORK_COMPANIES:
                case NETWORK_TYPES:
                    return NETWORK;
                default:
                    return STAFF;
            }
        }
        return STAFF;
    }

}

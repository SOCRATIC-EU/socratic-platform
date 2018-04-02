package de.atb.socratic.authorization.authentication;

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
import java.util.EnumSet;


/**
 * AuthenticationType
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public enum AuthenticationType {

    SESSION,

    BASIC,

    OAUTH1;

    public static EnumSet<AuthenticationType> getFromAnnotation(RequiresAuthentication annotation) {
        if ((annotation != null) && (annotation.allowedTypes().length > 0)) {
            return EnumSet.copyOf(Arrays.asList(annotation.allowedTypes()));
        } else {
            return EnumSet.noneOf(AuthenticationType.class);
        }

    }

}

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
import java.util.EnumSet;
import java.util.List;

import de.atb.socratic.authorization.authentication.RequiresAuthentication;

/**
 * UserRole
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public enum UserRole {

    NO_ROLE,

    USER,

    MANAGER(UserRole.USER),

    ADMIN(UserRole.USER, UserRole.MANAGER),

    SUPER_ADMIN(UserRole.USER, UserRole.MANAGER, UserRole.ADMIN);

    private UserRole[] containedRoles;

    UserRole() {
        this.containedRoles = new UserRole[]{this};
    }

    UserRole(UserRole... containedRoles) {
        this.containedRoles = containedRoles;
    }

    public boolean hasAnyRoles(UserRole role) {
        return hasAnyRoles(EnumSet.of(role));
    }

    public boolean hasAnyRoles(UserRole... roles) {
        return hasAnyRoles(EnumSet.copyOf(Arrays.asList(roles)));
    }

    public boolean hasAnyRoles(EnumSet<UserRole> allowedRoles) {
        if (allowedRoles.contains(this)) {
            return true;
        }
        for (UserRole containedRole : this.getContainedRoles()) {
            for (UserRole allowedRole : allowedRoles) {
                if (containedRole.equals(allowedRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAnyRoles(UserRole role, EnumSet<UserRole> allowedRoles) {
        return role.hasAnyRoles(allowedRoles);
    }

    public EnumSet<UserRole> getContainedRoles() {
        if (containedRoles.length > 0) {
            return EnumSet.copyOf(Arrays.asList(containedRoles));
        } else {
            return EnumSet.of(this);
        }
    }

    public static EnumSet<UserRole> getRequiredRolesFromAnnotation(RequiresAuthentication annotation) {
        if (annotation != null) {
            return EnumSet.copyOf(Arrays.asList(annotation.requiredRoles()));
        } else {
            return EnumSet.noneOf(UserRole.class);
        }
    }

    public static List<UserRole> getUserRoles(Company company) {
        if (company.getFullName().equals("SOCRATIC")) {
            return Arrays.asList(NO_ROLE, USER, MANAGER, ADMIN, SUPER_ADMIN);
        } else {
            return Arrays.asList(NO_ROLE, USER, MANAGER, ADMIN);
        }
    }

}

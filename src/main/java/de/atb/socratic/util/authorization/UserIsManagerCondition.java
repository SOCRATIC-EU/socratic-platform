package de.atb.socratic.util.authorization;

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

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.UserRole;

/**
 * UserIsManagerCondition
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class UserIsManagerCondition extends UserBasedAuthorizationCondition<Company> {

    /**
     *
     */
    private static final long serialVersionUID = -8788299536027966190L;

    /**
     * @param conditionalObject
     */
    public UserIsManagerCondition(Company conditionalObject) {
        super(conditionalObject);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.socratic.authorization.strategies.metadata.IAuthorizationCondition#
     * isAuthorized()
     */
    @Override
    public boolean isAuthorized() {
        if (user == null) {
            return false;
        }
        if (user.isSuperAdmin()) {
            return true;
        }
        Employment curEmp = user.getCurrentEmployment();
        return (curEmp != null) && (curEmp.getCompany() != null) && curEmp.getCompany().equals(conditionalObject)
                && user.hasAnyRoles(UserRole.MANAGER);
    }

    public static UserBasedAuthorizationCondition<Company> get(Campaign campaign) {
        return UserBasedAuthorizationCondition.get(UserIsManagerCondition.class, Company.class, campaign.getCompany());
    }

    public static UserBasedAuthorizationCondition<Company> get(Company company) {
        return UserBasedAuthorizationCondition.get(UserIsManagerCondition.class, Company.class, company);
    }

}

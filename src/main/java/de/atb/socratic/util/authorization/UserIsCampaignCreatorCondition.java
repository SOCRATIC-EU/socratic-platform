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

public class UserIsCampaignCreatorCondition extends UserBasedAuthorizationCondition<Campaign> {

    /**
     *
     */
    private static final long serialVersionUID = 5476859556093667682L;

    public UserIsCampaignCreatorCondition(Campaign conditionalObject) {
        super(conditionalObject);
    }

    @Override
    public boolean isAuthorized() {
        if (user.isSuperAdmin()) return true;
        return conditionalObject != null && user != null && conditionalObject.getCreatedBy().equals(user);
    }

    public static UserBasedAuthorizationCondition<Campaign> get(Campaign campaign) {
        return UserBasedAuthorizationCondition.get(UserIsCampaignCreatorCondition.class, Campaign.class, campaign);
    }
}

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

import javax.inject.Inject;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.model.scope.ScopeType;
import de.atb.socratic.service.inception.ScopeService;
import org.apache.wicket.cdi.CdiContainer;

/**
 * UserInScopeCondition
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class UserInScopeCondition extends UserBasedAuthorizationCondition<Scope> {

    private static final long serialVersionUID = 7687151080745629104L;

    @Inject
    ScopeService scopeService;

    public UserInScopeCondition(Scope scope) {
        super(scope);
        CdiContainer.get().getNonContextualManager().inject(this);
    }

    @Override
    public boolean isAuthorized() {
        // if scope is set to be open, everybody is allowed to interact
        if ((getConditionalObject() != null) && (getConditionalObject().getScopeType() == ScopeType.OPEN)) {
            return true;
        }

        // if user is part of the scope, interaction is allowed
        return (getUser() != null) && (getConditionalObject() != null)
                && scopeService.isUserInScope(getConditionalObject(), getUser());
    }

    public static UserBasedAuthorizationCondition<Scope> get(Scope scope) {
        return UserBasedAuthorizationCondition.get(UserInScopeCondition.class, Scope.class, scope);
    }

    public static UserBasedAuthorizationCondition<Scope> get(Campaign campaign) {
        return UserBasedAuthorizationCondition.get(UserInScopeCondition.class, Scope.class, campaign.getScope());
    }

}

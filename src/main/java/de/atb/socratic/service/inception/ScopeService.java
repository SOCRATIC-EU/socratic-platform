/**
 *
 */
package de.atb.socratic.service.inception;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import de.atb.socratic.exception.NotificationException;
import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Employment;
import de.atb.socratic.model.User;
import de.atb.socratic.model.UserRole;
import de.atb.socratic.model.notification.NotificationType;
import de.atb.socratic.model.scope.NetworkScope;
import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.model.scope.ScopeType;
import de.atb.socratic.model.scope.StaffScope;
import de.atb.socratic.service.AbstractService;
import de.atb.socratic.service.employment.CompanyService;
import de.atb.socratic.service.notification.ParticipateNotificationService;
import de.atb.socratic.service.user.UserService;
import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.inception.idea.IdeasPage;
import de.atb.socratic.web.provider.UrlProvider;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.jboss.seam.transaction.TransactionPropagation;
import org.jboss.seam.transaction.Transactional;

/**
 * @author ATB
 */
@Stateless
public class ScopeService extends AbstractService<Scope> {

    private static final long serialVersionUID = 7530745128013694771L;

    @Inject
    UrlProvider urlProvider;

    @Inject
    UserService userService;

    @Inject
    CompanyService companyService;

    @Inject
    ParticipateNotificationService participateNotifier;

    public ScopeService() {
        super(Scope.class);
    }

    public void notifyAboutParticipation(Campaign campaign) {
        notifyAboutParticipation(campaign.getScope(), campaign);
    }

    public boolean isUserInScope(Scope scope, User user) {
        if (scope.getScopeType() == ScopeType.OPEN || user.getCurrentRole() == UserRole.SUPER_ADMIN) {
            // Scope is open or is admin, allow!
            return true;
        } else if ((user.getCurrentRole() == UserRole.MANAGER || user.getCurrentRole() == UserRole.ADMIN) && user.getCurrentCompany() != null && user.getCurrentCompany().equals(scope.getCompany())) {
            // user is admin or manager of the company the scope belongs to, allow!
            return true;
        } else if (scope instanceof StaffScope) {
            // get users from company
            return getStaffScopedUsers(scope).contains(user);
        } else if (scope instanceof NetworkScope) {
            // get users from network
            return getNetworkScopedUsers(scope).contains(user);
        }
        return false;
    }

    public List<User> getAllUsersInScope(Scope scope) {
        if (scope.getScopeType() == ScopeType.OPEN) {
            return userService.getAllRegisteredUsers();
        }
        Set<User> users = new HashSet<>();
        users.addAll(getStaffScopedUsers(scope));
        users.addAll(getNetworkScopedUsers(scope));
        // add all managers of the company to the scoped users
        if (scope.getCompany() != null) {
            users.addAll(companyService.getManagerUsersByAscendingLastName(scope.getCompany()));
        }
        // add all administrators to the scoped users as well
        users.addAll(companyService.getAdminUsersByAscendingLastName());

        return new ArrayList<>(users);
    }

    public Long countAllUsersInScope(Scope scope) {
        if (scope.getScopeType() == ScopeType.OPEN) {
            return userService.countAllRegisteredUsers();
        }
        Set<User> users = new HashSet<>();
        users.addAll(getStaffScopedUsers(scope));
        users.addAll(getNetworkScopedUsers(scope));
        // add all managers of the company to the scoped users
        if (scope.getCompany() != null) {
            users.addAll(companyService.getManagerUsersByAscendingLastName(scope.getCompany()));
        }
        // add all administrators to the scoped users as well
        users.addAll(companyService.getAdminUsersByAscendingLastName());

        return (long) users.size();
    }

    @Transactional(TransactionPropagation.REQUIRED)
    public Long countAllExplicitUsersInScope(Scope scope) {
        if (scope.getScopeType() == ScopeType.OPEN) {
            return userService.countAllRegisteredUsers();
        }
        Set<User> users = new HashSet<>();
        users.addAll(getStaffScopedUsers(scope));
        users.addAll(getNetworkScopedUsers(scope));
        return (long) users.size();
    }

    public Set<User> getNetworkScopedUsers(Scope scope) {
        Set<User> users = new HashSet<>();
        if (!(scope instanceof NetworkScope)) return users;
        NetworkScope networkScope = (NetworkScope) scope;
        if (networkScope.getScopeType() == ScopeType.NETWORK_COMPANIES) {
            users.addAll(companyService.getAllUsersByAscendingLastName(networkScope.getNetworkedCompanies()));
        } else if (networkScope.getScopeType() == ScopeType.NETWORK_TYPES) {
            // implement!
        } else {
            // implement!
        }
        return users;
    }

    public Set<User> getStaffScopedUsers(Scope scope) {
        Set<User> users = new HashSet<>();
        if (!(scope instanceof StaffScope)) return users;
        StaffScope staffScope = (StaffScope) scope;
        if (staffScope.getScopeType() == ScopeType.STAFF_ALL) {
            users.addAll(companyService.getAllUsersByAscendingLastName(staffScope.getCompany()));
        } else if (staffScope.getScopeType() == ScopeType.STAFF_DEPARTMENTS) {
            users.addAll(companyService.getAllUsersForDepartmentsByAscendingName(staffScope.getDepartments()));
        } else if (staffScope.getScopeType() == ScopeType.STAFF_USERS) {
            users.addAll(staffScope.getUsers());
        }
        return users;
    }

    private void notifyAboutParticipation(Scope scope, Campaign campaign) {
        try {
            String campaignLink = urlProvider.urlFor(IdeasPage.class, "id", String.valueOf(campaign.getId()));
            String logoLink = urlProvider.urlFor(new PackageResourceReference(BasePage.class, "img/soc_logo_124x32px_horizontal.png"), null);
            List<User> users = getAllUsersInScope(scope);
            for (User user : users) {
                boolean send = true;
                for (Employment e : user.getEmployments()) {
                    if (e.getCompany().equals(campaign.getCompany()) && !e.getActive()) {
                        send = false;
                        break;
                    }
                }
                if (send) {
                    // check preferences! Only sent mail if wanted!
                    participateNotifier.sendParticipationMail(campaign, user, campaignLink, logoLink, NotificationType.CAMPAIGN_PARTICIPATE);
                    // check preferences! Only save notification if wanted!
                    participateNotifier.addParticipationNotification(campaign, user, NotificationType.CAMPAIGN_PARTICIPATE);
                }
            }
        } catch (NotificationException e) {
            logger.error(e.getMessage(), e);
        }
    }


}

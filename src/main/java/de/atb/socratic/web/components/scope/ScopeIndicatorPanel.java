package de.atb.socratic.web.components.scope;

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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.Department;
import de.atb.socratic.model.User;
import de.atb.socratic.model.scope.NetworkScope;
import de.atb.socratic.model.scope.Scope;
import de.atb.socratic.model.scope.StaffScope;
import de.atb.socratic.web.components.StringResourceModelPlaceholderDelegate;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.CssReferenceHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class ScopeIndicatorPanel extends Panel {

    @Inject
    @LoggedInUser
    User loggedInUser;


    private static final long serialVersionUID = 4973200812213414970L;

    private WebMarkupContainer container;
    private final boolean isTooltipped;

    public ScopeIndicatorPanel(String id, IModel<Scope> model) {
        super(id, model);
        container = new WebMarkupContainer("container");
        container.add(createNewAjaxLink("open"));
        container.add(createNewAjaxLink("network"));
        container.add(createNewAjaxLink("staff"));

        if (model != null && model.getObject() != null) {
            Scope scope = model.getObject();
            switch (scope.getScopeType()) {
                case NETWORK:
                case NETWORK_ALL:
                case NETWORK_COMPANIES:
                case NETWORK_TYPES:
                    container.add(new CssClassNameAppender("network"));
                    break;
                case STAFF:
                case STAFF_ALL:
                case STAFF_DEPARTMENTS:
                case STAFF_USERS:
                    container.add(new CssClassNameAppender("staff"));
                    break;
                case OPEN:
                    container.add(new CssClassNameAppender("open"));
                    break;
            }
        } else {
            container.add(new CssClassNameAppender("none"));
        }

        String tooltip = getTooltip(model);
        if (StringUtils.isNotBlank(tooltip)) {
            isTooltipped = true;
            container.add(new AttributeModifier("data-toggle", "tooltip"));
            container.add(new AttributeModifier("data-placement", "left"));
            container.add(new AttributeModifier("title", getTooltip(model)));
        } else {
            isTooltipped = false;
        }
        add(container);
    }


    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssReferenceHeaderItem.forReference(new PackageResourceReference(ScopeIndicatorPanel.class, "ScopeIndicatorPanel.css")));
        if (isTooltipped) {
            response.render(OnDomReadyHeaderItem.forScript("jQuery('#" + container.getMarkupId() + "').tooltip()"));
        }
    }

    @SuppressWarnings("unchecked")
    protected AjaxLink<Scope> createNewAjaxLink(final String id) {
        return (AjaxLink<Scope>) new AjaxLink<Scope>(id) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                ScopeIndicatorPanel.this.onClick(target);
            }
        }.setOutputMarkupId(true);
    }

    /**
     * Nothing to do here, only overwrite if you are interested in doing something after clicking.
     *
     * @param target
     */
    public abstract void onClick(AjaxRequestTarget target);

    public String getTooltip(final IModel<Scope> model) {
        Scope scope = model != null && model.getObject() != null ? model.getObject() : null;
        if (scope != null) {
            switch (scope.getScopeType()) {
                case STAFF_ALL:
                    return getStaffAllTooltip(scope);
                case STAFF_USERS:
                    return getStaffUsersTooltip((StaffScope) scope);
                case STAFF_DEPARTMENTS:
                    return getStaffDepartmentsTooltip((StaffScope) scope);
                case NETWORK_ALL:
                    return getNetworkAllTooltip((NetworkScope) scope);
                case NETWORK_COMPANIES:
                    return getNetworkCompaniesTooltip((NetworkScope) scope);
                case NETWORK_TYPES:
                    // implement types!
                    return "";
                default:
                    return new StringResourceModelPlaceholderDelegate("scope.type." + scope.getScopeType().toString(), this, null).getString();
            }
        } else {
            return this.getStringForScopeTypeNone();
        }
    }

    protected String getStringForScopeTypeNone() {
        return new StringResourceModelPlaceholderDelegate("scope.type.NONE", this, null).getString();
    }

    protected String getNetworkAllTooltip(NetworkScope scope) {
        return new StringResourceModelPlaceholderDelegate("scope.type." + scope.getScopeType().toString(), this, null,
                new Object[]{scope.getCompany().getShortName()}).getString();
    }

    protected String getNetworkCompaniesTooltip(NetworkScope scope) {
        Set<Company> companies = scope.getNetworkedCompanies();
        List<String> companiesNames = new ArrayList<String>(companies.size());
        for (Company company : companies) {
            companiesNames.add(company.getShortName());
        }
        Collections.sort(companiesNames);
        return new StringResourceModelPlaceholderDelegate("scope.type." + scope.getScopeType().toString(), this, null,
                new Object[]{companies.size(), StringUtils.join(companiesNames, ", ")}).getString();
    }

    protected String getStaffAllTooltip(Scope scope) {
        if (scope.getCompany() != null) {
            return new StringResourceModelPlaceholderDelegate("scope.type." + scope.getScopeType().toString(), this, null,
                    new Object[]{scope.getCompany().getShortName()}).getString();
        } else {
            return new StringResourceModelPlaceholderDelegate("scope.type." + scope.getScopeType().toString(), this, null,
                    new Object[]{loggedInUser.getCurrentCompany().getShortName()}).getString();
        }
    }

    protected String getStaffDepartmentsTooltip(StaffScope scope) {
        Set<Department> departments = scope.getDepartments();
        List<String> departmentNames = new ArrayList<String>(departments.size());
        for (Department department : departments) {
            departmentNames.add(department.getName());
        }
        Collections.sort(departmentNames);
        return new StringResourceModelPlaceholderDelegate("scope.type." + scope.getScopeType().toString(), this, null,
                new Object[]{departments.size(), StringUtils.join(departmentNames, ", ")}).getString();
    }

    protected String getStaffUsersTooltip(StaffScope scope) {
        Set<User> users = scope.getUsers();
        List<String> userNames = new ArrayList<String>(users.size());
        for (User user : users) {
            userNames.add(user.getNickName());
        }
        Collections.sort(userNames);
        return new StringResourceModelPlaceholderDelegate("scope.type." + scope.getScopeType().toString(), this, null,
                new Object[]{users.size(), StringUtils.join(userNames, ", ")}).getString();
    }

}

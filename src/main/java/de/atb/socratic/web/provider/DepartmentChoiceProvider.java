package de.atb.socratic.web.provider;

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

import java.util.List;

import de.atb.socratic.model.Department;

/**
 * DepartmentChoiceProvider
 *
 * @author ATB
 * @version $LastChangedRevision: $
 */
public class DepartmentChoiceProvider extends
        AbstractEntityChoiceProvider<Department> {

    /**
     *
     */
    private static final long serialVersionUID = -4449412068948131756L;

    public DepartmentChoiceProvider(List<Department> departments) {
        this(departments, (Department[]) null);
    }

    /**
     * @param departments
     * @param departmentsToExclude
     */
    public DepartmentChoiceProvider(List<Department> departments,
                                    Department... departmentsToExclude) {
        super(departments, departmentsToExclude);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.atb.socratic.web.provider.AbstractEntityChoiceProvider#queryMatches(de.
     * atb.socratic.model.AbstractEntity, java.lang.String)
     */
    @Override
    protected boolean queryMatches(Department entity, String term) {
        return entity.getName().toUpperCase().contains(term.toUpperCase());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vaynberg.wicket.select2.TextChoiceProvider#getDisplayText(java.lang
     * .Object)
     */
    @Override
    protected String getDisplayText(Department department) {
        return department.getName();
    }

}

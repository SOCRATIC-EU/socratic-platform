package de.atb.socratic.web.dashboard;

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

import de.atb.socratic.web.BasePage;
import de.atb.socratic.web.components.FileUploadBehavior;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class DashboardManager extends BasePage {

    private static final long serialVersionUID = -9092430357787574242L;

    /**
     *
     */
    public DashboardManager(final PageParameters parameters) {
        super(parameters);
        // add js and css for tags input, table sorter, etc.
        // add(new EFFBehaviour());

        // add js and css for file upload
        add(new FileUploadBehavior());
    }

    /*
     * (non-Javadoc)
     *
     *
     */
    @Override
    protected IModel<String> getPageTitleModel() {
        return new StringResourceModel("page.title", this, null);
    }

}


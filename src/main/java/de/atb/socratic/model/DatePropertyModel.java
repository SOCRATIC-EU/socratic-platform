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

import java.util.Date;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.wicket.model.PropertyModel;

public class DatePropertyModel extends PropertyModel<Object> {

    private static final long serialVersionUID = -1100525207499613474L;

    public DatePropertyModel(Object modelObject, String expression) {
        super(modelObject, expression);
    }

    @Override
    public Object getObject() {
        return DateFormatUtils.format((Date) super.getObject(), "dd.MM.yyyy");
    }

}

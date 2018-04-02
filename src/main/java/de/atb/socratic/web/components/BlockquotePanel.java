/**
 *
 */
package de.atb.socratic.web.components;

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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * @author ATB
 */
public class BlockquotePanel<T> extends GenericPanel<T> {

    /**
     *
     */
    private static final long serialVersionUID = -3601702319458336436L;

    private static final String LABEL_ID = "property";

    public BlockquotePanel(final String id, final IModel<T> model,
                           final String property) {
        this(id, model, property, false);
    }

    public BlockquotePanel(final String id, final IModel<T> model,
                           final String property, final boolean escapeHTML) {
        super(id, model);
        add(new Label(LABEL_ID,
                new PropertyModel<T>(getModelObject(), property)).setEscapeModelStrings(escapeHTML));
    }

}

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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Temporarily (until end of request) appends the css class in the model to a
 * component's class attribute.
 *
 * @author ATB
 */
public class CSSAppender extends AttributeAppender {

    /**
     *
     */
    private static final long serialVersionUID = 875094567205322546L;

    private static final String attribute = "class";

    private static final String separator = " ";

    private IModel<String> model;

    public CSSAppender(IModel<String> model) {
        super(attribute, model, separator);
        this.setModel(model);
    }

    public CSSAppender(String css) {
        this(new Model<>(css));
    }

    public IModel<String> getModel() {
        return model;
    }

    public void setModel(IModel<String> model) {
        this.model = model;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.behavior.Behavior#isTemporary(org.apache.wicket.Component
     * )
     */
    @Override
    public boolean isTemporary(Component component) {
        return true;
    }
}

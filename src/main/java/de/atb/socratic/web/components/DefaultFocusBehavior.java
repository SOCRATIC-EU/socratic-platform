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
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.FormComponent;

/**
 * @author ATB
 */
@SuppressWarnings("rawtypes")
public class DefaultFocusBehavior extends Behavior {

    /**
     *
     */
    private static final long serialVersionUID = -561941706017776414L;

    private FormComponent formComponent;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.behavior.Behavior#bind(org.apache.wicket.Component)
     */
    @Override
    public void bind(Component component) {
        if (component instanceof FormComponent) {
            this.formComponent = (FormComponent) component;
            this.formComponent.setOutputMarkupId(true);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.behavior.Behavior#renderHead(org.apache.wicket.Component
     * , org.apache.wicket.markup.html.IHeaderResponse)
     */
    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        if (this.formComponent != null) {
            final String js = String.format("$('#%s').focus()",
                    this.formComponent.getMarkupId());
            response.render(OnDomReadyHeaderItem.forScript(js));
        }
    }
}

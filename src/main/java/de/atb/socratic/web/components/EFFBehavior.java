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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;

/**
 * @author ATB
 */
public class EFFBehavior extends Behavior {

    /**
     *
     */
    private static final long serialVersionUID = 3712548616429988065L;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.behavior.Behavior#renderHead(org.apache.wicket.Component
     * , org.apache.wicket.markup.head.IHeaderResponse)
     */
    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);

        // add eff java script stuff
        response.render(JavaScriptHeaderItem.forUrl(JSTemplates.EFF_JS, JSTemplates.EFF_JS_REF_ID));
        // add javascript and css files for using jquery tags input for the tags
        // field
        response.render(JavaScriptHeaderItem.forUrl(JSTemplates.JQUERY_TAGS_INPUT_JS,
                JSTemplates.JQUERY_TAGS_INPUT_JS_REF_ID));
        response.render(CssHeaderItem.forUrl(JSTemplates.JQUERY_TAGS_INPUT_CSS));
        // add javascript files for using jquery table sorter and select all
        response.render(JavaScriptHeaderItem.forUrl(JSTemplates.JQUERY_TABLESORT_JS,
                JSTemplates.JQUERY_TABLESORT_JS_REF_ID));
        // // add javascript files for using jquery ui (sortable, draggable
        // etc.)
        response.render(JavaScriptHeaderItem.forUrl(JSTemplates.JQUERY_UI_JS, JSTemplates.JQUERY_UI_JS_REF_ID));

        // initially hide all ".collapse" thingies
        response.render(OnDomReadyHeaderItem.forScript(JSTemplates.INIT_COLLAPSE));

    }
}

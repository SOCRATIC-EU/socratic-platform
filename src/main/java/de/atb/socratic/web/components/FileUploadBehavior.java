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

/**
 * @author ATB
 */
public class FileUploadBehavior extends Behavior {

    /**
     *
     */
    private static final long serialVersionUID = -2493523853622247416L;

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

        response.render(JavaScriptHeaderItem.forUrl(
                JSTemplates.EFF_FILE_UPLOAD_JS,
                JSTemplates.EFF_FILE_UPLOAD_JS_REF_ID));
        response.render(JavaScriptHeaderItem.forUrl(
                JSTemplates.FILE_UPLOAD_TMPL_JS,
                JSTemplates.FILE_UPLOAD_TMPL_JS_REF_ID));
        response.render(JavaScriptHeaderItem.forUrl(
                JSTemplates.FILE_UPLOAD_IMG_JS,
                JSTemplates.FILE_UPLOAD_IMG_JS_REF_ID));
        response.render(JavaScriptHeaderItem.forUrl(
                JSTemplates.FILE_UPLOAD_CANVAS_JS,
                JSTemplates.FILE_UPLOAD_CANVAS_JS_REF_ID));
        response.render(JavaScriptHeaderItem.forUrl(
                JSTemplates.FILE_UPLOAD_IFRAME_JS,
                JSTemplates.FILE_UPLOAD_IFRAME_JS_REF_ID));
        response.render(JavaScriptHeaderItem.forUrl(JSTemplates.FILE_UPLOAD_JS,
                JSTemplates.FILE_UPLOAD_JS_REF_ID));
        response.render(JavaScriptHeaderItem.forUrl(
                JSTemplates.FILE_UPLOAD_FP_JS,
                JSTemplates.FILE_UPLOAD_FP_JS_REF_ID));
        response.render(JavaScriptHeaderItem.forUrl(
                JSTemplates.FILE_UPLOAD_UI_JS,
                JSTemplates.FILE_UPLOAD_UI_JS_REF_ID));

        response.render(CssHeaderItem
                .forUrl(JSTemplates.FILE_UPLOAD_GALLERY_CSS));
        response.render(CssHeaderItem.forUrl(JSTemplates.FILE_UPLOAD_UI_CSS));
    }
}

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

import java.io.Serializable;

import org.apache.wicket.protocol.http.WebApplication;

/**
 * @author ATB
 */
public class JSTemplates implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6666017216728726827L;

    /**
     * JS and CSS file paths
     */
    // some general eff stuff
    public static final String EFF_JS = "assets/js/eff.js";
    // jquery table sorter
    public static final String JQUERY_TABLESORT_JS = "assets/js/jquery.tablesorter.min.js";
    // jquery tags input
    public static final String JQUERY_TAGS_INPUT_JS = "assets/js/jquery.tagsinput.js";
    public static final String JQUERY_TAGS_INPUT_CSS = "assets/css/jquery.tagsinput.css";
    // jquery ui with ALL plugins
    public static final String JQUERY_UI_JS = "assets/js/jquery-ui-1.9.1.custom.js";
    // bootstrap tour
    public static final String BOOTSTRAP_TOUR = "assets/js/bootstrap-tour.js";
    // jquery file upload
    public static final String EFF_FILE_UPLOAD_JS = "assets/js/fileupload/eff-fileupload.js";
    public static final String FILE_UPLOAD_JQUERY_UI_JS = "assets/js/fileupload/vendor/jquery.ui.widget.js";
    public static final String FILE_UPLOAD_TMPL_JS = "assets/js/fileupload/tmpl.min.js";
    public static final String FILE_UPLOAD_IMG_JS = "assets/js/fileupload/load-image.min.js";
    public static final String FILE_UPLOAD_CANVAS_JS = "assets/js/fileupload/canvas-to-blob.min.js";
    public static final String FILE_UPLOAD_IFRAME_JS = "assets/js/fileupload/jquery.iframe-transport.js";
    public static final String FILE_UPLOAD_JS = "assets/js/fileupload/jquery.fileupload.js";
    public static final String FILE_UPLOAD_FP_JS = "assets/js/fileupload/jquery.fileupload-fp.js";
    public static final String FILE_UPLOAD_UI_JS = "assets/js/fileupload/jquery.fileupload-ui.js";
    public static final String FILE_UPLOAD_GALLERY_CSS = "assets/css/fileupload/bootstrap-image-gallery.min.css";
    public static final String FILE_UPLOAD_UI_CSS = "assets/css/fileupload/jquery.fileupload-ui.css";

    // scoping css stuff
    public static final String SCOPE_CSS = "assets/css/scoping.css";

    /**
     * JS IDs
     */
    // some general eff stuff
    public static final String EFF_JS_REF_ID = "js_eff";
    // jquery table sorter
    public static final String JQUERY_TABLESORT_JS_REF_ID = "js_tablesort";
    // jquery tags input
    public static final String JQUERY_TAGS_INPUT_JS_REF_ID = "js_tagsinput";
    // jquery ui
    public static final String JQUERY_UI_JS_REF_ID = "js_ui";
    // bootstrap tour
    public static final String BOOTSTRAP_TOUR_REF_ID = "b_tour";
    // jquery file upload
    public static final String EFF_FILE_UPLOAD_JS_REF_ID = "assets/js/fileupload/eff-fileupload.js";
    public static final String FILE_UPLOAD_JQUERY_UI_JS_REF_ID = "assets/js/fileupload/vendor/jquery.ui.widget.js";
    public static final String FILE_UPLOAD_TMPL_JS_REF_ID = "assets/js/fileupload/tmpl.min.js";
    public static final String FILE_UPLOAD_IMG_JS_REF_ID = "assets/js/fileupload/load-image.min.js";
    public static final String FILE_UPLOAD_CANVAS_JS_REF_ID = "assets/js/fileupload/canvas-to-blob.min.js";
    public static final String FILE_UPLOAD_IFRAME_JS_REF_ID = "assets/js/fileupload/jquery.iframe-transport.js";
    public static final String FILE_UPLOAD_JS_REF_ID = "assets/js/fileupload/jquery.fileupload.js";
    public static final String FILE_UPLOAD_FP_JS_REF_ID = "assets/js/fileupload/jquery.fileupload-fp.js";
    public static final String FILE_UPLOAD_UI_JS_REF_ID = "assets/js/fileupload/jquery.fileupload-ui.js";

    /**
     * JS templates
     */
    public static final String ADJUST_BODY_PADDING_TOP = "adjustBodyPaddingTop('%s');";
    public static final String HIDE_SUBNAV = "hideSubNav();";
    public static final String SHOW_TAGS = "showTags('%s');";
    public static final String LOAD_TAGS_INPUT = "loadTagsInput('%s', '%s');";
    public static final String LOAD_FILE_UPLOAD = "loadFileUpload('%s', '%s', '%s', '%s');";
    public static final String LOAD_TABLE_SORTER = "activateTableSort('%s');";
    public static final String LOAD_VOTING_TABLE_SORTER = "activateVotingTableSort('%s');";
    public static final String LOAD_PRIORITISED_IDEAS_TABLE_SORTER = "activatePrioritisedTableSort('%s');";
    public static final String LOAD_SELECT_ALL = "activateSelectAll('%s');";
    public static final String INIT_COLLAPSE = "initCollapse();";
    public static final String TOGGLE_COLLAPSE = "toggleCollapse('%s');";
    public static final String CLOSE_COLLAPSE = "closeCollapse('%s');";
    public static final String SHOW_COLLAPSE = "showCollapse('%s');";
    public static final String PREPEND_ELEM_TEMPLATE = "prependElemToContainer('%s', '%s', '%s');";
    public static final String APPEND_ELEM_TEMPLATE = "appendElemToContainer('%s', '%s', '%s');";
    public static final String FADE_IN_ELEM_TEMPLATE = "fadeInElem('%s');";
    public static final String FADE_OUT_ELEM_TEMPLATE = "notify|fadeOutElem('%s', notify);";
    public static final String FADE_OUT_AND_REMOVE_ELEM_TEMPLATE = "notify|fadeOutAndRemoveElem('%s', notify);";
    public static final String SLIDE_UP_ELEM_TEMPLATE = "notify|slideUp('%s', notify);";
    public static final String SLIDE_DOWN_ELEM_TEMPLATE = "notify|slideDown('%s', notify);";

    /**
     * @return the current web apps context path (e.g. "eff-jboss-wicket")
     */
    public static String getContextPath() {
        return WebApplication.get().getServletContext().getContextPath();
    }

}

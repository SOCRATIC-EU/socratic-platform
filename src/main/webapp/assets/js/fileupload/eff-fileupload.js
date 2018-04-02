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
/*
 * jQuery File Upload Plugin JS Example 6.7
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/*jslint nomen: true, unparam: true, regexp: true */
/*global $, window, document */

function loadFileUpload(contextPath, formId, hiddenFieldId, filesContainerId) {
    'use strict';

    // Initialize the jQuery File Upload widget:
    $('#' + formId).fileupload({
        'url': contextPath + '/fileupload',
        'filesContainer': '#' + filesContainerId,
        'autoUpload': true,
        'previewMaxWidth': 32,
        'previewMaxHeight': 32
    });

    // Enable iframe cross-domain access via redirect option:
    $('#' + formId).fileupload('option', 'redirect', window.location.href.replace(/\/[^\/]*$/, '/cors/result.html?%s'));

    // Load existing files:
    $('#' + formId).each(
        function () {
            var that = this;
            $.getJSON(contextPath + '/fileupload?uploadCacheId=' + $('#' + hiddenFieldId).val(), function (result) {
                if (result && result.length) {
                    $(that)
                        .fileupload('option', 'done')
                        .call(that, null, {
                            result: result
                        });
                }
            });
        }
    );

}

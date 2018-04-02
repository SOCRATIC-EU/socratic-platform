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
/**
 * dynamically adjust body padding to accommodate for variable navigation bar height.
 *
 * @param navbarId
 */
function adjustBodyPaddingTop(navbarId) {
    "use strict";
    var navBar = $('#' + navbarId);
    if (navBar) {
        var navBarHeightWithUnit = navBar.css('height');
        var body = $('#body');
        if (body) {
            body.css('padding-top', navBarHeightWithUnit);
        }
    }
}

/**
 * hides the sub navigation bar.
 */
function hideSubNav() {
    "use strict";

    var subNav = $('#subnav');
    if (subNav) {
        subNav.css('display', 'none');
    }
}

/**
 * add jquery tags input stuff to input text field with the given id
 * and load any already existing tags.
 */
function loadTagsInput(id, existingTags) {
	$('#' + id).tagsInput({
		'width' : 'auto',
		'minHeight' : '37px',
		'minChars' : 2,
		'maxChars' : 75,
		'interactive' : true,
		'defaultText' : 'add a tag',
		'onRemoveTag': function(value) {
			var delim = ',';
			var old = $('#' + id).val().split(delim);
			var str = '';
			for (var i = 0; i < old.length; i++) {
				if (old[i] != value) {
					str = str + delim +old[i];
				}
			}
			$('#' + id).val(str);
		},
		'typeahead' : {
			source : function(query, process) {
				var orig = window.location.protocol+'//'+window.location.host;
				return $.get(orig + '/eff-jboss-wicket/rest/tags/mostused', function(data) {
					return process(data);
				}, 'json');
			}
		}
	});
	$('#' + id).importTags(existingTags);
};

/**
 * add jquery tags input stuff to input text field with the given id interactive
 * is set to false so tags are displayed but you cannot add or remove tags.
 */
function showTags(id) {
	$('#' + id).tagsInput({
		'width' : 'auto',
		'minHeight' : '37px',
		'interactive' : false
	});
};

/**
 *
 */
function initCollapse() {
	$('.collapse').collapse({
		toggle : false
	});
}

/**
 * use bootstrap collapse to toggle elements which match given selector.
 */
function toggleCollapse(selector) {
	$(selector).collapse('toggle');
}


/**
 * use bootstrap collapse to hide elements which match given selector.
 */
function closeCollapse(selector) {
	if ($(selector).hasClass('in')) {
		$(selector).collapse('hide');
	}
}

/**
 * use bootstrap collapse to show elements which match given selector.
 */
function showCollapse(selector) {
	if (!$(selector).hasClass('in')) {
		$(selector).collapse('show');
	}
}

/**
 * appends an item at the end of the given container.
 */
function appendElemToContainer(itemType, itemId, containerId) {
	var item = document.createElement(itemType);
	item.id = itemId;
	Wicket.$(containerId).appendChild(item);
}

/**
 * prepends an item at the beginning of the given container.
 */
function prependElemToContainer(itemType, itemId, containerId) {
	var item = document.createElement(itemType);
	item.id = itemId;
	var container = Wicket.$(containerId);
	var firstElem = container.firstChild;
	if (firstElem) {
		container.insertBefore(item, firstElem);
	} else {
		container.appendChild(item);
	}
};

/**
 * fades in the elem with the given id.
 * @param id
 */
function fadeInElem(id) {
	$('#' + id).fadeIn(1500);
};

/**
 * fades out the elem with the given id
 * @param id
 * @returns {Boolean}
 */
function fadeOutElem(id, notifyCallback) {
	$('#' + id).fadeOut(1000, notifyCallback);
};

/**
 * fades out the elem with the given id and removes it from DOM
 * @param id
 * @returns {Boolean}
 */
function fadeOutAndRemoveElem(id, notifyCallback) {
	$('#' + id).fadeOut(1000, function() {
		$('#' + id).remove();
		notifyCallback();
	});
};

/**
 * slides up the elem with the given id
 * @param id
 * @returns {Boolean}
 */
function slideUp(id, notifyCallback) {
	$('#' + id).slideUp(1000, notifyCallback);
}

/**
 * slides down out the elem with the given id
 * @param id
 * @returns {Boolean}
 */
function slideDown(id, notifyCallback) {
	$('#' + id).slideDown(1000, notifyCallback);
}

/**
 *
 */
function activateTableSort(id) {
	jQuery('#' + id).tablesorter({
        headers: {
            0: { sorter: false },
            4: { sorter: false },
            5: { sorter: false },
            6: { sorter: false },
            7: { sorter: false },
            8: { sorter: false }
        }
    });
};
/**
 *
 */
function activateVotingTableSort(id) {
	jQuery('#' + id).tablesorter({
        headers: {
            0: { sorter: false },
            4: { sorter: false },
            5: { sorter: false },
            7: { sorter: 'numeric' }
        } ,
        textExtraction: {
            7: function(node, table, cellIndex){ return $(node+".wicketRatingText").text(); }
          }
    });
};


/**
 *
 */
function activatePrioritisedTableSort(id) {
	jQuery('#' + id).tablesorter({
        headers: {
            0: { sorter: false },
            7: { sorter: 'numeric' }
        } ,
        textExtraction: {
            7: function(node, table, cellIndex){ return $(node+".wicketRatingText").text(); }
          }
    });
};
/**
 *
 */
function activateSelectAll(id) {
	jQuery('#' + id).click(function(e){
		var boxes = document.getElementsByTagName('input');
		for (var index = 0; index < boxes.length; index++) {
			box = boxes[index];
			if (box.type == 'checkbox' && box.className == 'batch-select')
				box.checked = this.checked;
		}
		return true;
	});
};

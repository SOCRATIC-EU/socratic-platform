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

import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wicket.contrib.tinymce4.TinyMceBehavior;
import wicket.contrib.tinymce4.settings.TinyMCESettings;
import wicket.contrib.tinymce4.settings.Toolbar;

/**
 * @author ATB
 */
public class TinyMCETextArea extends TextArea<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TinyMCETextArea.class);

    public TinyMCETextArea(final String id, final IModel<String> model) {
        this(id, model, false);
    }

    public TinyMCETextArea(final String id, final IModel<String> model, final boolean readOnly) {
        super(id, model);
        add(new TinyMceBehavior(new TinyMCESettings(TinyMCESettings.Theme.modern)
                .setReadOnly(readOnly)
                .setMenuBar(false)
                .addPlugins("table")
                .addPlugins("link")
                .addPlugins("textcolor")
                .addToolbar(new Toolbar(
                        "toolbar",
                        "formatselect fontselect fontsizeselect | forecolor | backcolor | bold italic underline | alignleft aligncenter alignright alignjustify | bullist numlist | blockquote link table"))));
    }

}

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


import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonList;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;

public class MenuDivider extends AbstractLink {

    private static final long serialVersionUID = 3564171653376282143L;


    public MenuDivider() {
        super(ButtonList.getButtonMarkupId());

        setBody(Model.of("&nbsp;"));
        setEscapeModelStrings(false);
    }


    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        getParent().add(new CssClassNameAppender("divider"));
    }

}

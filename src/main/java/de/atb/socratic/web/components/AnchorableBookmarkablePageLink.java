/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @param <T>
 * @author ATB
 */
public class AnchorableBookmarkablePageLink<T> extends BookmarkablePageLink<T> {

    private static final long serialVersionUID = -5537852083626580417L;
    private final String anchor;

    public <C extends Page> AnchorableBookmarkablePageLink(final String id,
                                                           final Class<C> pageClass,
                                                           final String anchor) {
        super(id, pageClass);
        this.anchor = anchor;
    }

    public <C extends Page> AnchorableBookmarkablePageLink(final String id,
                                                           final Class<C> pageClass,
                                                           final PageParameters parameters,
                                                           final String anchor) {
        super(id, pageClass, parameters);
        this.anchor = anchor;
    }

    @Override
    protected CharSequence getURL() {
        return super.getURL() + "#" + anchor;
    }

}

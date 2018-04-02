package de.atb.socratic.web.provider;

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

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlRenderer;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbsoluteUrlRenderer extends UrlRenderer {
    private static final Logger log = LoggerFactory.getLogger(AbsoluteUrlRenderer.class);

    private final Url contextUrl, filterUrl;
    private String domain;

    public AbsoluteUrlRenderer(Request request, String prefix) {
        super(request);

        this.contextUrl = buildContextUrl(request, prefix);
        this.filterUrl = buildFilterUrl(request, prefix);
        this.domain = prefix;
        log.debug("Prefix for absolute urls: {}", filterUrl);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String renderRelativeUrl(Url url) {
        Args.notNull(url, "url");

        if (url.isAbsolute()) {
            return url.toString();
        } else {
            Url absolute = fromFilterRelativeToAbsolute(url);
            log.debug("renderRelativeUrl: {} => {}", url, absolute);

            String renderedUrl = absolute.toString();
            return Strings.isEmpty(renderedUrl) ? "." : domain + renderedUrl;
        }
    }


    @Override
    public String renderContextRelativeUrl(String url) {
        Args.notNull(url, "url");

        // Prevent prefixing a url twice
        if (url.startsWith(contextUrl.toString())) {
            return url;
        }

        if (url.startsWith("./")) {
            url = url.substring(2);
        }

        Url relativeUrl = Url.parse(url);
        Url absoluteUrl = fromContextRelativeToAbsolute(relativeUrl);
        return absoluteUrl.toString();
    }

    @SuppressWarnings("deprecation")
    private Url buildContextUrl(Request request, String prefix) {
        Url url = new Url();

        if (prefix != null && prefix.length() > 0) {
            url.getSegments().addAll(Url.parse(prefix).getSegments());
        }

        String contextPath = request.getContextPath();
        if (contextPath.length() > 0) {
            url.getSegments().addAll(Url.parse(contextPath.substring(1)).getSegments());
        }

        if (!url.isAbsolute()) {
            url.getSegments().add(0, "");
        }

        return url;
    }

    private Url buildFilterUrl(Request request, String prefix) {
        Url url = buildContextUrl(request, prefix);

        String filterPath = request.getFilterPath();
        if (filterPath.length() > 0) {
            url.getSegments().addAll(Url.parse(filterPath.substring(1)).getSegments());
        }

        return url;
    }

    private Url fromContextRelativeToAbsolute(Url url) {
        Url absolute = new Url(url);
        absolute.prependLeadingSegments(contextUrl.getSegments());

        return absolute;
    }

    private Url fromFilterRelativeToAbsolute(Url url) {
        Url absolute = new Url(url);
        absolute.prependLeadingSegments(filterUrl.getSegments());

        return absolute;
    }

}

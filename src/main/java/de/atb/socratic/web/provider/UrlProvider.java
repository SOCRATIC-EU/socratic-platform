/**
 *
 */
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

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Page;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * @author ATB
 */
@ApplicationScoped
public class UrlProvider {

    /**
     * @param clazz
     * @return
     */
    public String urlFor(final Class<? extends Page> clazz) {
        return urlFor(clazz, null);
    }

    /**
     * @param clazz
     * @param key
     * @param value
     * @return
     */
    public String urlFor(final Class<? extends Page> clazz, final String key,
                         final String value) {
        PageParameters parameters = new PageParameters();
        parameters.add(key, value);
        return urlFor(clazz, parameters);
    }

    /**
     * @param clazz
     * @param parameters
     * @return
     */
    public String urlFor(final Class<? extends Page> clazz,
                         final PageParameters parameters) {
        HttpServletRequest request = (HttpServletRequest) RequestCycle.get().getRequest().getContainerRequest();
        String forwarded = request.getHeader("x-forwarded-for");
        if (forwarded != null) {
            String header = request.getHeader("referer");
            int eff = header.indexOf("/eff-jboss-wicket");
            if (eff > 0) {
                String domain = header.substring(0, eff);
                String url = domain + new AbsoluteUrlRenderer(RequestCycle.get().getRequest(), "").renderContextRelativeUrl(Url.parse(RequestCycle.get().urlFor(clazz, parameters)).toString());
                return url;
            } else {
                return RequestCycle.get().getUrlRenderer().renderFullUrl(
                        Url.parse(RequestCycle.get().urlFor(clazz, parameters).toString()));
            }
        } else {
            String url = RequestCycle.get().getUrlRenderer().renderFullUrl(
                    Url.parse(RequestCycle.get().urlFor(clazz, parameters).toString()));
            return url;
        }

    }

    public String urlFor(final ResourceReference reference, final PageParameters parameters) {
        return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(RequestCycle.get().urlFor(reference, parameters).toString()));
    }

}

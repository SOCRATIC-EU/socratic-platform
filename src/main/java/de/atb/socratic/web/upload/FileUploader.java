/**
 *
 */
package de.atb.socratic.web.upload;

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

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import de.atb.socratic.web.EFFSession;
import org.apache.wicket.Session;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.flow.AbortWithHttpErrorCodeException;
import org.apache.wicket.request.mapper.AbstractMapper;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class FileUploader extends AbstractMapper {

    // inject a logger
    @Inject
    Logger logger;

    private final String[] mountSegments;

    /**
     *
     */
    public FileUploader(final String path) {
        mountSegments = getMountSegments(path);
        // make this thing available in CDI contexts.
        CdiContainer.get().getNonContextualManager().inject(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.request.IRequestMapper#getCompatibilityScore(org.apache
     * .wicket.request.Request)
     */
    @Override
    public int getCompatibilityScore(Request request) {
        if (urlStartsWith(request.getUrl(), mountSegments)) {
            return mountSegments.length;
        } else {
            return 0;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.wicket
     * .request.IRequestHandler)
     */
    @Override
    public Url mapHandler(IRequestHandler requestHandler) {
        if (requestHandler instanceof FileUploadRequestHandler) {
            Url url = new Url();
            for (String s : mountSegments) {
                url.getSegments().add(s);
            }
            return url;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket
     * .request.Request)
     */
    @Override
    public IRequestHandler mapRequest(Request request) {
        Url url = request.getUrl();

        if (urlStartsWith(url, mountSegments)) {
            if (!isAuthorized()) {
                logger.debug("Caller is unauthorized to upload files");
                throw new AbortWithHttpErrorCodeException(
                        HttpServletResponse.SC_FORBIDDEN);
            }
            return new FileUploadRequestHandler();
        } else {
            return null;
        }
    }

    /**
     * Called to verify if the caller is authorized to upload files
     *
     * @return
     */
    protected boolean isAuthorized() {
        return ((EFFSession) Session.get()).isAuthenticated();
    }

}

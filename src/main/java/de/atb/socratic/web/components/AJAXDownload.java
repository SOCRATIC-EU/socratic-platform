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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;

/**
 * @author ATB
 * @author Ernesto Reinaldo Barreiro (reiern70@gmail.com)
 * @author Jordi Deu-Pons (jordi@jordeu.net)
 */
public abstract class AJAXDownload extends AbstractAjaxBehavior {

    /**
     *
     */
    private static final long serialVersionUID = 5339459622422933741L;

    private boolean addAntiCache;

    /**
     *
     */
    public AJAXDownload() {
        this(true);
    }

    /**
     * @param addAntiCache
     */
    public AJAXDownload(boolean addAntiCache) {
        super();
        this.addAntiCache = addAntiCache;
    }

    /**
     * Call this method to initiate the download.
     *
     * @param target
     */
    public void initiate(AjaxRequestTarget target) {
        String url = getCallbackUrl().toString();

        if (addAntiCache) {
            url = url + (url.contains("?") ? "&" : "?");
            url = url + "antiCache=" + System.currentTimeMillis();
        }

        String command = null;
        if (isImageFile()) {
            command = "window.open('" + url + "', '_blank')";
        } else {
            command = "window.location.href='" + url + "'";
        }

        // the timeout is needed to let Wicket release the channel
        target.appendJavaScript("setTimeout(\"" + command + "\", 100);");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.behavior.IBehaviorListener#onRequest()
     */
    @Override
    public void onRequest() {
        ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(getResourceStream(), getFileName());
        if (isImageFile()) {
            handler.setContentDisposition(ContentDisposition.INLINE);
        } else {
            handler.setContentDisposition(ContentDisposition.ATTACHMENT);
        }
        getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
    }

    /**
     * Override this method for a file name which will let the browser prompt
     * with a save/open dialog.
     */
    protected String getFileName() {
        return null;
    }

    protected abstract boolean isImageFile();

    /**
     * Hook method providing the actual resource stream.
     */
    protected abstract IResourceStream getResourceStream();
}

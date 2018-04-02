/**
 *
 */
package de.atb.socratic.util;

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

import java.io.File;
import java.io.Serializable;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;

/**
 * @author ATB
 */
public class MimeTypeDetector implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5334741423012819077L;

    /**
     * @param filePath
     */
    public static final String getMimeType(String filePath) {
        return getMimeType(new File(filePath));
    }

    /**
     * @param file
     * @return
     */
    public static final String getMimeType(File file) {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        // always returns at least the UNKNOWN_MIME_TYPE
        // "application/octet-stream"
        return ((MimeType) MimeUtil.getMimeTypes(file).iterator().next())
                .toString();
    }

}

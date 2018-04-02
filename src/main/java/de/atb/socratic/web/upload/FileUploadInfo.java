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

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;

import de.atb.socratic.model.FileInfo;
import de.atb.socratic.web.qualifier.FileUploadCache;

/**
 * @author ATB
 */
@SessionScoped
@FileUploadCache
public class FileUploadInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 8507896722379788124L;

    private Map<String, Set<FileInfo>> filesMap = new HashMap<String, Set<FileInfo>>();

    /**
     *
     */
    public FileUploadInfo() {
    }

    /**
     * @param cacheId
     * @return
     */
    public synchronized Set<FileInfo> getFileInfos(String cacheId) {
        if (filesMap.containsKey(cacheId)) {
            return filesMap.get(cacheId);
        }
        return new LinkedHashSet<FileInfo>();
    }

    /**
     * Returns FileInfo with given path if it exists, null otherwise
     *
     * @param cacheId
     * @param filePath
     * @return
     */
    public synchronized FileInfo getFileInfo(String cacheId, String filePath) {
        Set<FileInfo> files = getFileInfos(cacheId);
        for (FileInfo fileInfo : files) {
            if (fileInfo.getPath().equals(filePath)) {
                return fileInfo;
            }
        }
        return null;
    }

    /**
     * @param cacheId
     * @param fileInfo
     */
    public synchronized void addFileInfo(String cacheId, FileInfo... fileInfo) {
        Set<FileInfo> files = getFileInfos(cacheId);
        for (FileInfo fi : fileInfo) {
            files.add(fi);
        }
        filesMap.put(cacheId, files);
    }

    /**
     * @param cacheId
     * @param fileInfo
     */
    public synchronized void removeFileInfo(String cacheId, FileInfo fileInfo) {
        getFileInfos(cacheId).remove(fileInfo);
    }

    /**
     * @param cacheId
     */
    public synchronized void removeFileInfos(String cacheId) {
        filesMap.remove(cacheId);
    }

    /**
     * @param cacheId
     */
    public synchronized void cleanup(String cacheId) {
        removeFileInfos(cacheId);
    }

    /**
     *
     */
    private synchronized void cleanupAll() {
        for (String cacheId : filesMap.keySet()) {
            cleanup(cacheId);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        cleanupAll();
    }

}

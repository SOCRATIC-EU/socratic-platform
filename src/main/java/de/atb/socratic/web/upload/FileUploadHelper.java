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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.Company;
import de.atb.socratic.model.User;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
@SessionScoped
public class FileUploadHelper implements Serializable {

    private static final long serialVersionUID = -8665792211751481118L;

    public static final PackageResourceReference iconRef
            = new PackageResourceReference(FileUploadRequestHandler.class, "img/file-icon.png");

    public static enum UploadType {
        USER,
        COMPANY,
        CHALLENGE;
    }

    @Inject
    Logger logger;

    // inject the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    private String uploadFolderBasePath;

    private UploadType type;

    private static final String EMPTY_MAIL = "unknown@example.com";
    private static final String EMPTY_COMPANY = "Unknown company";
    private static final String EMPTY_CHALLENGE = "Unknown challenge";

    public FileUploadHelper(UploadType type) {
        CdiContainer.get().getNonContextualManager().inject(this);
        this.type = type;
    }

    /**
     * @param uploadCacheId
     * @return
     */
    private String getUploadFolderPath(String dir, String uploadCacheId) {
        return createPersonalUploadFolder(dir).getAbsolutePath()
                + File.separatorChar + uploadCacheId;
    }

    /**
     * @return
     */
    private File createPersonalUploadFolder(String dir) {
        File folder = new File(getPersonalFolderPath(dir));
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * @return
     */
    private String getPersonalFolderPath(String dir) {
        return getUploadFolderBasePath() + File.separatorChar + dir;
    }

    /**
     * @param uploadCacheId
     * @return
     */
    public File createUploadFolder(String uploadCacheId, Company company, Campaign challenge) {
        switch (type) {
            case USER:
                return createUploadFolderWithDir(loggedInUser == null ? EMPTY_MAIL : loggedInUser.getEmail(), uploadCacheId);
            case COMPANY:
                return createUploadFolderWithDir(loggedInUser == null ? EMPTY_COMPANY : company.getShortName(), uploadCacheId);
            case CHALLENGE:
                return createUploadFolderWithDir(loggedInUser == null ? EMPTY_CHALLENGE : challenge.getName(), uploadCacheId);
        }
        return null;

    }

    public File createUploadFolderWithDir(String dir, String uploadCacheId) {
        File uploadFolder = new File(getUploadFolderPath(dir, uploadCacheId));
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }
        return uploadFolder;
    }

    /**
     * @param uploadCacheId
     * @param fileName
     * @return
     */
    public String getFilePath(String uploadCacheId, String fileName) {
        switch (type) {
            case USER:
                return getUploadFolderPath(loggedInUser == null ? EMPTY_MAIL : loggedInUser.getEmail(), uploadCacheId)
                        + File.separatorChar
                        + fileName;
            case COMPANY:
                return getUploadFolderPath(loggedInUser == null ? EMPTY_MAIL : loggedInUser.getCurrentCompany().getShortName(), uploadCacheId)
                        + File.separatorChar
                        + fileName;
        }
        return null;
    }

    public String getFilePath(String dir, String uploadCacheId, String fileName) {
        return getUploadFolderPath(dir, uploadCacheId) + File.separatorChar
                + fileName;
    }

    private String getUploadFolderBasePath() {
        if (uploadFolderBasePath == null) {
            String home = System.getProperty("user.home");
            if (StringUtils.isBlank(home)) {
                logger.info("Cannot determine user.home for upload folder!");
                home = "";
            }
            File uploadFolder = new File(home + File.separatorChar + ".socratic-platform" + File.separatorChar + "uploads");
            try {
                uploadFolder.mkdirs();
            } catch (Exception e) {
                logger.info("Could not create upload folder!", e);
            }
            uploadFolderBasePath = uploadFolder.getAbsolutePath();
        }
        return uploadFolderBasePath;
    }

    public static MediaType getMimeType(File file) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                file));
        AutoDetectParser parser = new AutoDetectParser();
        Detector detector = parser.getDetector();
        Metadata md = new Metadata();
        md.add(Metadata.RESOURCE_NAME_KEY, file.getAbsolutePath());
        MediaType mediaType = detector.detect(bis, md);
        bis.close();
        return mediaType;
    }

}

package de.atb.socratic.web.components.resource;

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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import de.atb.socratic.model.FileInfo;
import de.atb.socratic.model.User;
import de.atb.socratic.web.provider.LoggedInUserProvider;
import de.atb.socratic.web.upload.FileUploadHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.cdi.CdiContainer;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.time.Time;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.jboss.solder.logging.Logger;

@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class ProfilePictureResource extends DynamicImageResource {

    /**
     *
     */
    private static final long serialVersionUID = 5025233979459337586L;

    private static final PackageResourceReference defaultRef = new PackageResourceReference(
            FileUploadHelper.class, "img/default-user-profile.png");

    @Inject
    LoggedInUserProvider provider;

    @Inject
    Logger logger;

    private User user;

    private PictureType type;

    private boolean scaleImageUponRequest = false;

    public static IResource get(PictureType type) {
        return get(type, null);
    }

    public static IResource get(PictureType type, User user) {
        ProfilePictureResource profilePictureResource = null;
        if (user != null) {
            profilePictureResource = new ProfilePictureResource(type, user);
            User actualUser = profilePictureResource.user;
            if (actualUser == null || actualUser.getProfilePictureFile() == null || actualUser.getProfilePictureFile(type) == null)
                return defaultRef.getResource();
            return profilePictureResource;
        } else {
            return defaultRef.getResource();
        }
    }

    private ProfilePictureResource(PictureType type) {
        this(type, null);
    }

    private ProfilePictureResource(PictureType type, User user) {
        CdiContainer.get().getNonContextualManager().inject(this);
        this.user = user;
        this.type = type;
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        if (user != null && user.getProfilePictureFile() != null
                && user.getProfilePictureFile().getFile() != null
                && user.getProfilePictureFile().getFile().exists()) {
            FileInfo pictureFile = user.getProfilePictureFile();
            File image = pictureFile.getFile();
            setLastModifiedTime(Time.millis(image.lastModified()));
            try {
                File file = user.getProfilePictureFile(type);
                FileInputStream fis = new FileInputStream(file);
                byte[] bytes = scaleImage(fis);
                fis.close();
                return bytes;
            } catch (IOException e) {
                logger.error(e);
            }
        }
        return getNonProfilePicture();
    }

    private byte[] scaleImage(InputStream stream) throws IOException {
        return scaleImage(stream, scaleImageUponRequest);
    }

    private byte[] scaleImage(InputStream stream, boolean scale)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage image = ImageIO.read(stream);
        if (scale) {
            BufferedImage thumb = Scalr.resize(image, this.type.getSize());
            ImageIO.write(thumb, "png", baos);
        } else {
            ImageIO.write(image, "png", baos);
        }
        return baos.toByteArray();
    }

    private byte[] getNonProfilePicture() {
        return new byte[0];
    }

    public int getMaxSize() {
        return type.getSize();
    }

    public boolean getScaleImageUponRequest() {
        return scaleImageUponRequest;
    }

    public ProfilePictureResource setScaleImageUponRequest(
            boolean scaleImageUponRequest) {
        this.scaleImageUponRequest = scaleImageUponRequest;
        return this;
    }

    public static FileInfo createProfilePictureFromUpload(FileUpload image, File uploadFolder, User user, PictureType type) throws IOException {
        final String extension = FilenameUtils.getExtension(image.getClientFileName());
        final File file = new File(uploadFolder.getAbsolutePath() + File.separatorChar + user.getId() + "_profile_picture." + type.name().toLowerCase() + "." + extension);

        BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
        BufferedImage thumb = Scalr.resize(bufferedImage, Method.QUALITY, type.getSize());
        ImageIO.write(thumb, "png", file);
        bufferedImage.flush();
        FileInfo fileInfo = new FileInfo(file.getAbsolutePath(),
                file.getName(),
                image.getClientFileName(),
                image.getSize(),
                image.getContentType());
        image.closeStreams();
        return fileInfo;
    }

    public static FileInfo createProfilePictureFromLinkedIn(String pictureUrl, File uploadFolder, User user, PictureType type) throws IOException {
        final String displayName = "LinkedIn";
        final String extension = "png";
        final File file = new File(uploadFolder.getAbsolutePath() + File.separatorChar + user.getId() + "_profile_picture." + type.name().toLowerCase() + "." + extension);

        URL url = new URL(pictureUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        BufferedImage thumb = Scalr.resize(bufferedImage, Method.QUALITY, type.getSize());
        ImageIO.write(thumb, "png", file);
        bufferedImage.flush();
        FileInfo fileInfo = new FileInfo(file.getAbsolutePath(),
                file.getName(),
                displayName,
                file.length(),
                "image/png");
        return fileInfo;
    }

    public static FileInfo createProfilePictureFromFacebook(String pictureUrl, File uploadFolder, User user, PictureType type) throws IOException {
        final String displayName = "Facebook";
        final String extension = "png";
        final File file = new File(uploadFolder.getAbsolutePath() + File.separatorChar + user.getId() + "_profile_picture." + type.name().toLowerCase() + "." + extension);

        URL url = new URL(pictureUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        BufferedImage thumb = Scalr.resize(bufferedImage, Method.QUALITY, type.getSize());
        ImageIO.write(thumb, "png", file);
        bufferedImage.flush();
        FileInfo fileInfo = new FileInfo(file.getAbsolutePath(),
                file.getName(),
                displayName,
                file.length(),
                "image/png");
        return fileInfo;
    }
}

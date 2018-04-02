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

import javax.imageio.ImageIO;
import javax.inject.Inject;

import de.atb.socratic.model.ActionIteration;
import de.atb.socratic.model.FileInfo;
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
public class ActionIterationPictureResource extends DynamicImageResource {

    /**
     *
     */
    private static final long serialVersionUID = 5025233979459337586L;

    private static final PackageResourceReference defaultRef = new PackageResourceReference(FileUploadHelper.class,
            "img/dummy_img_medium.png");

    @Inject
    Logger logger;

    private ActionIteration iteration;

    private PictureType type;

    private boolean scaleImageUponRequest = false;

    public static IResource get(PictureType type) {
        return get(type, null);
    }

    public static IResource get(PictureType type, ActionIteration iteration) {
        ActionIterationPictureResource iterationPictureResource = null;
        if (iteration != null) {
            iterationPictureResource = new ActionIterationPictureResource(type, iteration);
            ActionIteration actualIteration = iterationPictureResource.iteration;
            if (actualIteration == null || actualIteration.getIterationImage() == null
                    || actualIteration.getIterationImage(type) == null)
                return defaultRef.getResource();

        }
        return iterationPictureResource;
    }

    private ActionIterationPictureResource(PictureType type) {
        this(type, null);
    }

    private ActionIterationPictureResource(PictureType type, ActionIteration iteration) {
        CdiContainer.get().getNonContextualManager().inject(this);
        this.iteration = iteration;
        this.type = type;
    }

    @Override
    protected byte[] getImageData(Attributes attributes) {
        if (iteration != null && iteration.getIterationImage() != null && iteration.getIterationImage().getFile() != null
                && iteration.getIterationImage().getFile().exists()) {

            FileInfo pictureFile = iteration.getIterationImage();
            File image = pictureFile.getFile();
            setLastModifiedTime(Time.millis(image.lastModified()));
            try {
                File file = iteration.getIterationImage(type);
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

    private byte[] scaleImage(InputStream stream, boolean scale) throws IOException {
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

    public ActionIterationPictureResource setScaleImageUponRequest(boolean scaleImageUponRequest) {
        this.scaleImageUponRequest = scaleImageUponRequest;
        return this;
    }

    public static FileInfo createProfilePictureFromUpload(FileUpload image, File uploadFolder, ActionIteration iteration,
                                                          PictureType type) throws IOException {
        final String extension = FilenameUtils.getExtension(image.getClientFileName());
        final File file = new File(uploadFolder.getAbsolutePath() + File.separatorChar + iteration.getId()
                + "_profile_picture." + type.name().toLowerCase() + "." + extension);

        BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
        BufferedImage thumb = Scalr.resize(bufferedImage, Method.QUALITY, type.getSize());
        ImageIO.write(thumb, "png", file);
        bufferedImage.flush();
        FileInfo fileInfo = new FileInfo(file.getAbsolutePath(), file.getName(), image.getClientFileName(), image.getSize(),
                image.getContentType());
        image.closeStreams();
        return fileInfo;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.atb.socratic.model.FileInfo;
import org.apache.wicket.extensions.markup.html.image.resource.ThumbnailImageResource;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.time.Time;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ATB
 */
public class ThumbnailResource extends ThumbnailImageResource {

    private static final long serialVersionUID = -2543079102382661022L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailResource.class);

    public ThumbnailResource(final FileInfo fileInfo, final int maxSize) {
        super(new DynamicImageResource() {

            private static final long serialVersionUID = -5857265559042391844L;

            @Override
            protected byte[] getImageData(Attributes attributes) {
                byte[] bytes = new byte[0];
                File file = fileInfo.getFile();
                if (file.exists()) {
                    setLastModifiedTime(Time.millis(file.lastModified()));
                    ByteArrayOutputStream baos = null;
                    try {
                        BufferedImage im = ImageIO.read(file);
                        BufferedImage thumb;
                        if (im != null) {
                            thumb = Scalr.resize(im, maxSize);
                            baos = new ByteArrayOutputStream();
                            ImageIO.write(thumb, "PNG", baos);
                            bytes = baos.toByteArray();
                        } else {
                            im = ImageIO.read(
                                    FileUploadHelper.iconRef.getResource().getResourceStream().getInputStream());
                            thumb = Scalr.resize(im, 32);
                            baos = new ByteArrayOutputStream();
                            ImageIO.write(thumb, "PNG", baos);
                            bytes = baos.toByteArray();
                        }

                    } catch (FileNotFoundException e) {
                        LOGGER.debug(String.format("File %s cannot be found", fileInfo.getInternalName()));
                    } catch (IOException e) {
                        LOGGER.debug(String.format("File %s cannot be read", fileInfo.getInternalName()));
                    } catch (ResourceStreamNotFoundException ex) {
                        LOGGER.debug(String.format("File %s cannot be read", fileInfo.getInternalName()));
                    } finally {
                        try {
                            if (baos != null) {
                                baos.flush();
                                baos.close();
                            }
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                            // Swallow, if we can't write anything, we give up
                        }
                    }
                }
                return bytes;
            }
        }, maxSize);
    }

}

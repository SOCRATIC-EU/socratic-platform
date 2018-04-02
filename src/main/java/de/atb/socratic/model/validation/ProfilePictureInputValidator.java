package de.atb.socratic.model.validation;

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

import org.apache.tika.mime.MediaType;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;

public class ProfilePictureInputValidator extends AbstractFormValidator {
    private static final long serialVersionUID = 3610885216640184204L;

    final FileUploadField picture;

    public ProfilePictureInputValidator(final FileUploadField picture) {
        super();
        if (picture == null) {
            throw new IllegalArgumentException("argument picture cannot be null");
        }

        this.picture = picture;
    }

    @Override
    public FormComponent<?>[] getDependentFormComponents() {
        return new FormComponent<?>[]{picture};
    }

    @Override
    public void validate(Form<?> form) {
        if (picture.getFileUpload() == null) {
            return;
        }

        if (!isUploadedFileAnImage(picture.getFileUpload())) {
            // if file is not an image error
            error(picture, "picture.isnotImage.error");
        }
    }

    private boolean isUploadedFileAnImage(FileUpload image) {
        if (image == null) {
            return false;
        }

        final MediaType type = MediaType.parse(image.getContentType());
        if (!"image".equalsIgnoreCase(type.getType())) {
            return false;
        }
        boolean gif = "gif".equalsIgnoreCase(type.getSubtype());
        boolean png = "png".equalsIgnoreCase(type.getSubtype());
        boolean jpg = "jpeg".equalsIgnoreCase(type.getSubtype());
        return gif || png || jpg;
    }
}

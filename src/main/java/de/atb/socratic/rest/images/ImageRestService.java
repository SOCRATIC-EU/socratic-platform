package de.atb.socratic.rest.images;

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
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.atb.socratic.model.FileInfo;
import de.atb.socratic.qualifier.Conversational;
import org.jboss.solder.logging.Logger;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/images")
@Conversational
public class ImageRestService implements Serializable {

    @Inject
    private Logger logger;

    @Inject
    private EntityManager em;

    @GET
    @Path("/{id:[1-9][0-9]*}")
    @Produces("image/png")
    public Response getCampaignById(@PathParam("id") Long id) {
        logger.debugf("loading image with file info id: %d", id);

        final FileInfo fileInfo = em.find(FileInfo.class, id);
        if (fileInfo != null) {
            try {
                final BufferedInputStream inputStream =
                        new BufferedInputStream(Files.newInputStream(Paths.get(fileInfo.getPath())));
                return Response.ok().entity(inputStream).build();
            } catch (IOException e) {
                logger.errorf("Error reading image file: %s", fileInfo.getPath());
            }
        }
        return Response.status(NOT_FOUND).build();
    }

}

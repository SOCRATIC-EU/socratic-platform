/**
 *
 */
package de.atb.socratic.rest.inception;

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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.atb.socratic.qualifier.Conversational;
import de.atb.socratic.util.ModelInitializer;

/**
 * @author ATB
 */
@Path("/tags")
@Conversational
public class TagsRestService implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -7781371114253779920L;

    @GET
    @Path("/mostused")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getMostUsedTags() {
        return Response.ok()
                .entity(new GenericEntity<List<String>>(ModelInitializer.tags) {
                }).build();
    }

}

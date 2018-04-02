/**
 *
 */
package de.atb.socratic.rest.user;

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

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.atb.socratic.model.Employment;
import de.atb.socratic.model.User;
import de.atb.socratic.qualifier.Conversational;
import de.atb.socratic.service.employment.EmploymentService;
import de.atb.socratic.service.user.UserService;
import org.hibernate.validator.constraints.Email;
import org.jboss.resteasy.spi.validation.ValidateRequest;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Path("/users")
@Conversational
public class UserRestService implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5689160616648831544L;

    // inject a logger
    @Inject
    Logger logger;

    @Inject
    UserService userService;

    @Inject
    EmploymentService employmentService;

    /**
     * @param email
     * @return
     */
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public User getByEmail(@QueryParam("email") @NotNull @Email String email) {
        return userService.getByEmail(email);
    }

    @GET
    @Path("/employments")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public Response getEmploymentsByEmail(@QueryParam("email") @NotNull @Email String email) {
        User user = userService.getByEmail(email);
        List<Employment> list = employmentService.getByUser(user);
        return Response.ok().entity(new GenericEntity<List<Employment>>(list) {
        }).build();
    }

}

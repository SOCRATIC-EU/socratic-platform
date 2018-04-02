/**
 *
 */
package de.atb.socratic.rest.provider;

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

import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {

    // inject a logger
    @Inject
    Logger logger;

    @Override
    public Response toResponse(EJBException ex) {
        logger.error(ex.getMessage(), ex);

        if (ex.getCause() instanceof PersistenceException) {
            // if we get a persistence related exception from one of our rest
            // services
            if ((ex.getCause() instanceof NoResultException)
                    || (ex.getCause() instanceof EntityNotFoundException)) {
                // if the entity was not found or query returned no result -->
                // send 404
                return Response.status(Status.NOT_FOUND)
                        .entity(ex.getCause().getMessage()).build();
            }
        } else if (ex.getCause() instanceof UnauthorizedException) {
            throw (UnauthorizedException) ex.getCause();
            // return
            // Response.status(Status.UNAUTHORIZED).entity(ex.getCause().getMessage()).build();
        } else if (ex instanceof EJBTransactionRolledbackException) {
            if ((ex.getCause() != null)
                    && (ex.getCause() instanceof RollbackException)) {
                Throwable cause = ex.getCause();
                if ((cause.getCause() != null)
                        && (cause.getCause() instanceof PersistenceException)) {
                    cause = cause.getCause();
                    // if cause of rollback was a constraint violation return
                    // status code 400 --> bad request
                    if ((cause.getCause() != null)
                            && (cause.getCause() instanceof ConstraintViolationException)) {
                        return Response.status(Status.BAD_REQUEST)
                                .entity(cause.getCause().getMessage()).build();
                    }
                }
            }
        }
        return Response
                .serverError()
                .entity("An unexpected error has occurred - we apologize for the inconvenience.")
                .build();
    }

}

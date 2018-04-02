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
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import de.atb.socratic.model.Campaign;
import de.atb.socratic.model.EntityCount;
import de.atb.socratic.model.Idea;
import de.atb.socratic.model.User;
import de.atb.socratic.qualifier.Conversational;
import de.atb.socratic.service.inception.CampaignService;
import de.atb.socratic.service.inception.IdeaService;
import de.atb.socratic.service.other.TagService;
import de.atb.socratic.web.qualifier.LoggedInUser;
import org.jboss.resteasy.spi.validation.ValidateRequest;
import org.jboss.solder.logging.Logger;

/**
 * @author ATB
 */
@Path("/campaigns")
@Conversational
public class CampaignRestService implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4634952642942688535L;

    /**
     * Custom messages for HTTP error codes.
     */
    private static final String _400_ID_NULL_MSG = "Cannot update resource without ID using PUT. To create a new resource, use POST instead.";
    private static final String _400_NO_CONTENT_MSG = "Cannot update non-existing resource with ID using PUT. To create a new resources, use POST instead.";
    private static final String _400_WRONG_ID_MSG = "The given entity's ID is different from the ID in the URL for the requested resource.";
    private static final String _400_ID_NOT_NULL_MSG = "Cannot create resource with given ID using POST. To update an existing resource, use PUT instead.";

    // inject a logger
    @Inject
    Logger logger;

    @Inject
    CampaignService campaignService;

    @Inject
    IdeaService ideaService;

    @Inject
    TagService tagService;

    // inject a provider to get the currently logged in user
    @Inject
    @LoggedInUser
    User loggedInUser;

    /**
     * @param first
     * @param count
     * @return
     */
    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public Response getAllCampaignsFromToByDescendingDueDate(
            @QueryParam("first") @DefaultValue("0") @NotNull @Min(value = 0) @Max(value = Integer.MAX_VALUE) int first,
            @QueryParam("count") @DefaultValue("10") @NotNull @Min(value = 0) @Max(value = Integer.MAX_VALUE) int count) {
        List<Campaign> campaigns = campaignService
                .getAllFromToByAscendingDueDate(first, count, null, loggedInUser);
        return Response.ok()
                .entity(new GenericEntity<List<Campaign>>(campaigns) {
                }).build();
    }

    /**
     * @return
     */
    @GET
    @Path("/count")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response countAllCampaigns() {
        long count = campaignService.countAll();
        return Response.ok(new EntityCount(count)).build();
    }

    /**
     * @param campaignId
     * @return
     */
    @GET
    @Path("/{campaignId:[1-9][0-9]*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getCampaignById(@PathParam("campaignId") Long campaignId) {
        Campaign campaign = campaignService.getById(campaignId);
        return Response.ok().entity(campaign).build();
    }

    /**
     * @param campaign
     * @return
     */
    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public Response createCampaign(@Valid Campaign campaign,
                                   @Context UriInfo uriInfo) {
        if (campaign.getId() != null) {
            // throw exception, cannot create campaign with given ID via POST.
            // Use PUT instead
            return Response.status(Status.BAD_REQUEST)
                    .entity(_400_ID_NOT_NULL_MSG).build();
        }
        campaign.setSocialChallenge("socialChallenge");
        campaign.setOpenForDiscussion(false);
        campaign.setIsPublic(false);    //by default private
        campaign.setIdeasProposed("ideasProposed");
        campaign.setLevelOfSupport("levelOfSupport");
        campaign.setPotentialImpact("potentialImpact");
        campaign.setBeneficiaries("beneficiaries");
        campaign.setTags(tagService.getAll());
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        campaign.setIdeationStartDate(date.getTime());
        date.add(Calendar.DATE, 15);
        campaign.setIdeationEndDate(date.getTime());
        campaign.setSelectionStartDate(date.getTime());
        date.add(Calendar.DATE, 20);
        campaign.setSelectionEndDate(date.getTime());

        campaign = campaignService.createOrUpdate(campaign);
        return Response.created(location(uriInfo, campaign.getId()))
                .entity(campaign).build();
    }

    /**
     * @param campaign
     * @return
     */
    @PUT
    @Path("/{campaignId:[1-9][0-9]*}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public Response updateCampaign(@PathParam("campaignId") Long campaignId,
                                   @Valid Campaign campaign) {
        if (campaign.getId() == null) {
            // throw exception, cannot update campaign without ID via
            // PUT. Use POST instead.
            return Response.status(Status.BAD_REQUEST).entity(_400_ID_NULL_MSG)
                    .build();
        }
        if (!campaign.getId().equals(campaignId)) {
            // throw exception, updating campaign at wrong path
            return Response.status(Status.BAD_REQUEST)
                    .entity(_400_WRONG_ID_MSG).build();
        }
        if (!campaignService.campaignExists(campaignId)) {
            // throw exception, cannot update non-existing campaign via
            // PUT. Use POST instead.
            return Response.status(Status.BAD_REQUEST)
                    .entity(_400_NO_CONTENT_MSG).build();
        }

        // update existing campaign and sent corresponding response
        campaign = campaignService.update(campaign);
        return Response.ok().entity(campaign).build();
    }

    /**
     * @param campaignId
     * @return
     */
    @DELETE
    @Path("/{campaignId:[1-9][0-9]*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteCampaign(@PathParam("campaignId") Long campaignId) {
        campaignService.softDelete(campaignId);
        return Response.noContent().build();
    }

    /**
     * @param campaignId
     * @param before
     * @param count
     * @return
     */
    @GET
    @Path("/{campaignId:[1-9][0-9]*}/ideas")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public Response getIdeasForCampaignFromToByDescendingPostDate(
            @PathParam("campaignId") Long campaignId,
            @QueryParam("before") @DefaultValue("" + Long.MAX_VALUE) @NotNull @Min(value = 0) @Max(value = Long.MAX_VALUE) long before,
            @QueryParam("count") @DefaultValue("" + Integer.MAX_VALUE) @NotNull @Min(value = 0) @Max(value = Integer.MAX_VALUE) int count) {
        Date postedAt = new Date(before);
        List<Idea> ideas = campaignService
                .getIdeasForCampaignBeforeDateByDescendingPostDate(campaignId,
                        postedAt, count);
        return Response.ok().entity(new GenericEntity<List<Idea>>(ideas) {
        }).build();
    }

    /**
     * @param campaignId
     * @return
     */
    @GET
    @Path("/{campaignId:[1-9][0-9]*}/ideas/count")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response countIdeasForCampaign(
            @PathParam("campaignId") Long campaignId) {
        long count = campaignService.countIdeasForCampaign(campaignId);
        return Response.ok(new EntityCount(count)).build();
    }

    /**
     * @param campaignId
     * @param ideaId
     * @return
     */
    @GET
    @Path("/{campaignId:[1-9][0-9]*}/ideas/{ideaId:[1-9][0-9]*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getIdeaById(@PathParam("campaignId") Long campaignId,
                                @PathParam("ideaId") Long ideaId) {
        Idea idea = ideaService.getById(ideaId);
        return Response.ok().entity(idea).build();
    }

    /**
     * @param campaignId
     * @param idea
     * @return
     */
    @POST
    @Path("/{campaignId:[1-9][0-9]*}/ideas")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public Response addIdea(@PathParam("campaignId") Long campaignId,
                            @Valid Idea idea, @Context UriInfo uriInfo) {
        if (idea.getId() != null) {
            // throw exception, cannot create idea with given ID via POST.
            // Use PUT instead
            return Response.status(Status.BAD_REQUEST)
                    .entity(_400_ID_NOT_NULL_MSG).build();
        }
        idea = campaignService.addIdea(campaignId, idea);
        return Response.created(location(uriInfo, idea.getId())).entity(idea)
                .build();
    }

    /**
     * @param campaignId
     * @param ideaId
     * @param idea
     * @return
     */
    @PUT
    @Path("/{campaignId:[1-9][0-9]*}/ideas/{ideaId:[1-9][0-9]*}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @ValidateRequest
    public Response updateIdea(@PathParam("campaignId") Long campaignId,
                               @PathParam("ideaId") Long ideaId, @Valid Idea idea) {
        if (idea.getId() == null) {
            // throw exception, cannot update campaign without ID via
            // PUT. Use POST instead.
            return Response.status(Status.BAD_REQUEST).entity(_400_ID_NULL_MSG)
                    .build();
        }
        if (!idea.getId().equals(ideaId)) {
            // throw exception, updating campaign at wrong path
            return Response.status(Status.BAD_REQUEST)
                    .entity(_400_WRONG_ID_MSG).build();
        }
        if (!campaignService.ideaExists(ideaId)) {
            // throw exception, cannot update non-existing idea via PUT. Use
            // POST instead.
            return Response.status(Status.BAD_REQUEST)
                    .entity(_400_NO_CONTENT_MSG).build();
        }

        // update existing idea and sent corresponding response
        idea = campaignService.updateIdea(idea);
        return Response.ok().entity(idea).build();
    }

    /**
     * @param campaignId
     * @param ideaId
     * @return
     */
    @DELETE
    @Path("/{campaignId:[1-9][0-9]*}/ideas/{ideaId:[1-9][0-9]*}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response removeIdea(@PathParam("campaignId") Long campaignId,
                               @PathParam("ideaId") Long ideaId) {
        campaignService.removeIdea(campaignId, ideaId);
        return Response.noContent().build();
    }

    /**
     * @param uriInfo
     * @param id
     * @return
     */
    private URI location(UriInfo uriInfo, Long id) {
        return URI.create(uriInfo.getPath() + "/" + id);
    }


}

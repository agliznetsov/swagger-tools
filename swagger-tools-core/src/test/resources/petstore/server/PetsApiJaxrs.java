package com.example.web;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

@Path("/v1")
@Consumes("application/json")
@Produces("application/json")
public interface PetsApi {
    @GET
    @Path("/pets")
    List<Pet> listPets(@QueryParam("limit") Integer limit);

    @POST
    @Path("/pets")
    Pet createPet(Pet pet);

    @GET
    @Path("/pets/{petId}")
    Pet getPetById(@PathParam("petId") Long petId, @QueryParam("details") Boolean details);

    @PUT
    @Path("/pets/{petId}")
    void updatePet(@PathParam("petId") Long petId, Pet requestBody);

    @DELETE
    @Path("/pets/{petId}")
    void deletePetById(@PathParam("petId") Long petId);

    @PUT
    @Path("/pets-ref/{petId}")
    Pet updatePetRefById(@PathParam("petId") Long petId, Pet requestBody);

    @GET
    @Path("/pets/{petId}/body")
    @Produces("text/plain")
    String getPetBody(@PathParam("petId") Long petId);

    @GET
    @Path("/pets/{petId}/thumbnail")
    @Produces("image/jpeg")
    byte[] getPetThumbnail(@PathParam("petId") Long petId);

    @GET
    @Path("/pets/{petId}/events")
    @Produces("text/event-stream")
    void getPetEvents(@PathParam("petId") Long petId, @Context SseEventSink sseEventSink,
            @Context Sse sse);
}
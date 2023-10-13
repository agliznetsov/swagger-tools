package com.example.web;

import com.example.model.Pet;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;

@Path("/v1")
@Produces("application/json")
public interface PetsApi {
    @POST
    @Path("/pets")
    @Consumes("application/json")
    Pet createPet(Pet pet);

    @DELETE
    @Path("/pets/{petId}")
    void deletePetById(@PathParam("petId") Long petId);

    @GET
    @Path("/pets/{petId}/body")
    @Produces("text/plain")
    String getPetBody(@PathParam("petId") Long petId);

    @GET
    @Path("/pets/{petId}")
    Pet getPetById(@PathParam("petId") Long petId, @QueryParam("details") Boolean details);

    @GET
    @Path("/pets/{petId}/details")
    Pet getPetDetails(@PathParam("petId") Long petId);

    @GET
    @Path("/pets/{petId}/events")
    @Produces("text/event-stream")
    void getPetEvents(@PathParam("petId") Long petId,
            @HeaderParam("Last-Event-Id") String lastEventId, @Context SseEventSink sseEventSink,
            @Context Sse sse);

    @GET
    @Path("/pets/{petId}/thumbnail")
    @Produces("image/jpeg")
    byte[] getPetThumbnail(@PathParam("petId") Long petId);

    @GET
    @Path("/hidden-client")
    void hiddenClientOp();

    @GET
    @Path("/pets")
    List<Pet> listPets(@QueryParam("limit") Integer limit,
            @QueryParam("Offset-Value") Integer offsetValue);

    @PUT
    @Path("/pets/{petId}")
    @Consumes("application/json")
    void updatePet(@PathParam("petId") Long petId, Pet requestBody);

    @PUT
    @Path("/pets-ref/{petId}")
    @Consumes("application/json")
    Pet updatePetRefById(@PathParam("petId") Long petId, Pet requestBody);

    @POST
    @Path("/xmlTest")
    @Produces("application/xml")
    @Consumes("application/xml")
    String xmlOperation(String pet);
}

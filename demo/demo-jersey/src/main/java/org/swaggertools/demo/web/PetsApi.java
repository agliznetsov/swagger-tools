package org.swaggertools.demo.web;

import org.swaggertools.demo.model.Pet;

import javax.ws.rs.*;
import java.util.List;

@Path("/pets")
@Consumes("application/json")
@Produces("application/json")
public interface PetsApi {
    @GET
    @Path("")
    List<Pet> listPets(@QueryParam("limit") Integer limit);

    @POST
    @Path("")
    Pet createPet(Pet requestBody);

    @GET
    @Path("/{petId}")
    Pet getPetById(@PathParam("petId") Long petId, @QueryParam("details") Boolean details);

    @PUT
    @Path("/{petId}")
    void updatePet(@PathParam("petId") Long petId, Pet requestBody);

    @DELETE
    @Path("/{petId}")
    void deletePetById(@PathParam("petId") Long petId);
}

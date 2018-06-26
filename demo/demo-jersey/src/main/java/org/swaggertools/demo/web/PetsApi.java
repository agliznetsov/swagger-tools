package org.swaggertools.demo.web;

import org.swaggertools.demo.model.Pet;

import javax.ws.rs.*;
import java.util.List;

@Path("/pets")
@Consumes("application/json")
@Produces("application/json")
public interface PetsApi {
    @GET
    @Path("/pets")
    List<Pet> listPets(@QueryParam("limit") Integer limit);

    @POST
    @Path("/pets")
//    @ResponseStatus(HttpStatus.CREATED)
    Pet createPet(Pet requestBody);

    @GET
    @Path("/pets/{petId}")
    Pet getPetById(@PathParam("petId") Long petId, @QueryParam("details") Boolean details);

    @PUT
    @Path("/pets/{petId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updatePet(@PathParam("petId") Long petId, Pet requestBody);

    @DELETE
    @Path("/pets/{petId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePetById(@PathParam("petId") Long petId);
}

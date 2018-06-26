package org.swaggertools.demo.client;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mashape.unirest.http.HttpMethod;
import org.swaggertools.demo.model.Pet;


public class PetsClient extends UnirestClient {
    public PetsClient(String basePath) {
        super(basePath);
    }

    public List<Pet> listPets(Integer limit) {
        Map urlVariables = createUrlVariables();
        Map parameters = createQueryParameters("limit", limit);
        TypeReference<List<Pet>> typeRef = new TypeReference<List<Pet>>(){};
        return invokeAPI("/pets", HttpMethod.GET, urlVariables, parameters, null, typeRef);
    }

    public Pet createPet(Pet requestBody) {
        Map urlVariables = createUrlVariables();
        Map parameters = createQueryParameters();
        TypeReference<Pet> typeRef = new TypeReference<Pet>(){};
        return invokeAPI("/pets", HttpMethod.POST, urlVariables, parameters, requestBody, typeRef);
    }

    public Pet getPetById(Long petId, Boolean details) {
        Map urlVariables = createUrlVariables("petId", petId);
        Map parameters = createQueryParameters("details", details);
        TypeReference<Pet> typeRef = new TypeReference<Pet>(){};
        return invokeAPI("/pets/{petId}", HttpMethod.GET, urlVariables, parameters, null, typeRef);
    }

    public void updatePet(Long petId, Pet requestBody) {
        Map urlVariables = createUrlVariables("petId", petId);
        Map parameters = createQueryParameters();
        TypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", HttpMethod.PUT, urlVariables, parameters, requestBody, typeRef);
    }

    public void deletePetById(Long petId) {
        Map urlVariables = createUrlVariables("petId", petId);
        Map parameters = createQueryParameters();
        TypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", HttpMethod.DELETE, urlVariables, parameters, null, typeRef);
    }
}

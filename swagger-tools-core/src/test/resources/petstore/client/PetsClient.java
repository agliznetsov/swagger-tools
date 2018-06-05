package com.example.client;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class PetsClient extends RestTemplateClient {
    public PetsClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public List<Pet> listPets(Integer limit) {
        MultiValueMap parameters = createQueryParameters("limit", limit);
        ParameterizedTypeReference<List<Pet>> typeRef = new ParameterizedTypeReference<List<Pet>>(){};
        ResponseEntity<List<Pet>> response = invokeAPI("/pets", HttpMethod.GET, parameters, null, typeRef);
        return response.getBody();
    }

    public Pet createPet(Pet requestBody) {
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets", HttpMethod.POST, parameters, requestBody, typeRef);
        return response.getBody();
    }

    public Pet getPetById(Long petId, Boolean details) {
        MultiValueMap parameters = createQueryParameters("petId", petId, "details", details);
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets/{petId}", HttpMethod.GET, parameters, null, typeRef);
        return response.getBody();
    }

    public void updatePet(Long petId, Pet requestBody) {
        MultiValueMap parameters = createQueryParameters("petId", petId);
        ParameterizedTypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", HttpMethod.PUT, parameters, requestBody, typeRef);
    }

    public void deletePetById(Long petId) {
        MultiValueMap parameters = createQueryParameters("petId", petId);
        ParameterizedTypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", HttpMethod.DELETE, parameters, null, typeRef);
    }
}
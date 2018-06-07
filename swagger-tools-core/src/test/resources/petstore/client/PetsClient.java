package com.example.client;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class PetsClient extends RestTemplateClient {
    public PetsClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public PetsClient(RestTemplate restTemplate, String basePath) {
        super(restTemplate, basePath);
    }

    public List<Pet> listPets(Integer limit) {
        Map urlVariables = createUrlVariables();
        MultiValueMap parameters = createQueryParameters("limit", limit);
        ParameterizedTypeReference<List<Pet>> typeRef = new ParameterizedTypeReference<List<Pet>>(){};
        ResponseEntity<List<Pet>> response = invokeAPI("/pets", HttpMethod.GET, urlVariables, parameters, null, typeRef);
        return response.getBody();
    }

    public Pet createPet(Pet requestBody) {
        Map urlVariables = createUrlVariables();
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets", HttpMethod.POST, urlVariables, parameters, requestBody, typeRef);
        return response.getBody();
    }

    public Pet getPetById(Long petId, Boolean details) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters("details", details);
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets/{petId}", HttpMethod.GET, urlVariables, parameters, null, typeRef);
        return response.getBody();
    }

    public void updatePet(Long petId, Pet requestBody) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", HttpMethod.PUT, urlVariables, parameters, requestBody, typeRef);
    }

    public void deletePetById(Long petId) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", HttpMethod.DELETE, urlVariables, parameters, null, typeRef);
    }
}
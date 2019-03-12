package com.example.client;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PetsClient extends BaseClient {
    public PetsClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public PetsClient(RestTemplate restTemplate, String basePath) {
        super(restTemplate, basePath);
    }

    public PetsClient(RestTemplate restTemplate, String basePath,
            Map<String, List<String>> headers) {
        super(restTemplate, basePath, headers);
    }

    public List<Pet> listPets(Integer limit) {
        ParameterizedTypeReference<List<Pet>> typeRef = new ParameterizedTypeReference<List<Pet>>(){};
        ResponseEntity<List<Pet>> response = invokeAPI("/pets", "GET", createUrlVariables(), createQueryParameters("limit", limit), null, typeRef);
        return response.getBody();
    }

    public Pet createPet(Pet pet) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets", "POST", createUrlVariables(), createQueryParameters(), pet, typeRef);
        return response.getBody();
    }

    public Pet getPetById(Long petId, Boolean details) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets/{petId}", "GET", createUrlVariables("petId", petId), createQueryParameters("details", details), null, typeRef);
        return response.getBody();
    }

    public void updatePet(Long petId, Pet requestBody) {
        ParameterizedTypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), requestBody, typeRef);
    }

    public void deletePetById(Long petId) {
        ParameterizedTypeReference typeRef = VOID;
        invokeAPI("/pets/{petId}", "DELETE", createUrlVariables("petId", petId), createQueryParameters(), null, typeRef);
    }

    public Pet updatePetRefById(Long petId, Pet requestBody) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets-ref/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), requestBody, typeRef);
        return response.getBody();
    }

    public String getPetBody(Long petId) {
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>(){};
        ResponseEntity<String> response = invokeAPI("/pets/{petId}/body", "GET", createUrlVariables("petId", petId), createQueryParameters(), null, typeRef);
        return response.getBody();
    }

    public byte[] getPetThumbnail(Long petId) {
        ParameterizedTypeReference<byte[]> typeRef = new ParameterizedTypeReference<byte[]>(){};
        ResponseEntity<byte[]> response = invokeAPI("/pets/{petId}/thumbnail", "GET", createUrlVariables("petId", petId), createQueryParameters(), null, typeRef);
        return response.getBody();
    }
}
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
import org.springframework.web.client.ResponseExtractor;
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

    public void hiddenServerOp() {
        ParameterizedTypeReference responseType = VOID;
        invokeAPI("/hidden-server", "GET", createUrlVariables(), createQueryParameters(), createQueryParameters(), null, null, responseType);
    }

    public List<Pet> listPets(Integer limit, Integer offsetValue) {
        ParameterizedTypeReference<List<Pet>> responseType = new ParameterizedTypeReference<List<Pet>>(){};
        ResponseEntity<List<Pet>> response = invokeAPI("/pets", "GET", createUrlVariables(), createQueryParameters("limit", limit, "Offset-Value", offsetValue), createQueryParameters(), null, null, responseType);
        return response.getBody();
    }

    public Pet createPet(Pet pet) {
        ParameterizedTypeReference<Pet> requestType = new ParameterizedTypeReference<Pet>(){};
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets", "POST", createUrlVariables(), createQueryParameters(), createQueryParameters(), pet, requestType, responseType);
        return response.getBody();
    }

    public Pet getPetById(Long petId, Boolean details) {
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets/{petId}", "GET", createUrlVariables("petId", petId), createQueryParameters("details", details), createQueryParameters(), null, null, responseType);
        return response.getBody();
    }

    public void updatePet(Long petId, Pet requestBody) {
        ParameterizedTypeReference<Pet> requestType = new ParameterizedTypeReference<Pet>(){};
        ParameterizedTypeReference responseType = VOID;
        invokeAPI("/pets/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), requestBody, requestType, responseType);
    }

    public void deletePetById(Long petId) {
        ParameterizedTypeReference responseType = VOID;
        invokeAPI("/pets/{petId}", "DELETE", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null, responseType);
    }

    public Pet updatePetRefById(Long petId, Pet requestBody) {
        ParameterizedTypeReference<Pet> requestType = new ParameterizedTypeReference<Pet>(){};
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets-ref/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), requestBody, requestType, responseType);
        return response.getBody();
    }

    public String getPetBody(Long petId) {
        ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<String>(){};
        ResponseEntity<String> response = invokeAPI("/pets/{petId}/body", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null, responseType);
        return response.getBody();
    }

    public byte[] getPetThumbnail(Long petId) {
        ParameterizedTypeReference<byte[]> responseType = new ParameterizedTypeReference<byte[]>(){};
        ResponseEntity<byte[]> response = invokeAPI("/pets/{petId}/thumbnail", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null, responseType);
        return response.getBody();
    }

    public void getPetEvents(Long petId, String lastEventId, ResponseExtractor responseExtractor) {
        executeAPI("/pets/{petId}/events", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters("Last-Event-Id", lastEventId), null, responseExtractor);
    }

    public ResponseEntity<Pet> getPetDetails(Long petId) {
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        ResponseEntity<Pet> response = invokeAPI("/pets/{petId}/details", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null, responseType);
        return response;
    }
}

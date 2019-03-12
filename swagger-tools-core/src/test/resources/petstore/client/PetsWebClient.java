package com.example.client;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.lang.Void;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class PetsWebClient extends BaseClient {
    public PetsWebClient(WebClient webClient) {
        super(webClient);
    }

    public PetsWebClient(WebClient webClient, String basePath) {
        super(webClient, basePath);
    }

    public PetsWebClient(WebClient webClient, String basePath, Map<String, List<String>> headers) {
        super(webClient, basePath, headers);
    }

    public Mono<List<Pet>> listPets(Integer limit) {
        ParameterizedTypeReference<List<Pet>> typeRef = new ParameterizedTypeReference<List<Pet>>(){};
        return invokeAPI("/pets", "GET", createUrlVariables(), createQueryParameters("limit", limit), null).bodyToMono(typeRef);
    }

    public Mono<Pet> createPet(Pet pet) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets", "POST", createUrlVariables(), createQueryParameters(), pet).bodyToMono(typeRef);
    }

    public Mono<Pet> getPetById(Long petId, Boolean details) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets/{petId}", "GET", createUrlVariables("petId", petId), createQueryParameters("details", details), null).bodyToMono(typeRef);
    }

    public Mono<Void> updatePet(Long petId, Pet requestBody) {
        ParameterizedTypeReference typeRef = VOID;
        return invokeAPI("/pets/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), requestBody).bodyToMono(typeRef);
    }

    public Mono<Void> deletePetById(Long petId) {
        ParameterizedTypeReference typeRef = VOID;
        return invokeAPI("/pets/{petId}", "DELETE", createUrlVariables("petId", petId), createQueryParameters(), null).bodyToMono(typeRef);
    }

    public Mono<Pet> updatePetRefById(Long petId, Pet requestBody) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets-ref/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), requestBody).bodyToMono(typeRef);
    }

    public Mono<String> getPetBody(Long petId) {
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>(){};
        return invokeAPI("/pets/{petId}/body", "GET", createUrlVariables("petId", petId), createQueryParameters(), null).bodyToMono(typeRef);
    }

    public Mono<byte[]> getPetThumbnail(Long petId) {
        ParameterizedTypeReference<byte[]> typeRef = new ParameterizedTypeReference<byte[]>(){};
        return invokeAPI("/pets/{petId}/thumbnail", "GET", createUrlVariables("petId", petId), createQueryParameters(), null).bodyToMono(typeRef);
    }
}
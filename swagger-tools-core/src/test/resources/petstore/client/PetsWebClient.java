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
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
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

    public Mono<List<Pet>> listPets(Integer limit, Integer offsetValue) {
        ParameterizedTypeReference<List<Pet>> typeRef = new ParameterizedTypeReference<List<Pet>>(){};
        return invokeAPI("/pets", "GET", createUrlVariables(), createQueryParameters("limit", limit, "Offset-Value", offsetValue), null).flatMap(e -> mapResponse(e, typeRef));
    }

    public Mono<Pet> createPet(Pet pet) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets", "POST", createUrlVariables(), createQueryParameters(), pet).flatMap(e -> mapResponse(e, typeRef));
    }

    public Mono<Pet> getPetById(Long petId, Boolean details) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets/{petId}", "GET", createUrlVariables("petId", petId), createQueryParameters("details", details), null).flatMap(e -> mapResponse(e, typeRef));
    }

    public Mono<Void> updatePet(Long petId, Pet requestBody) {
        ParameterizedTypeReference typeRef = VOID;
        return invokeAPI("/pets/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), requestBody).flatMap(e -> mapResponse(e, typeRef));
    }

    public Mono<Void> deletePetById(Long petId) {
        ParameterizedTypeReference typeRef = VOID;
        return invokeAPI("/pets/{petId}", "DELETE", createUrlVariables("petId", petId), createQueryParameters(), null).flatMap(e -> mapResponse(e, typeRef));
    }

    public Mono<Pet> updatePetRefById(Long petId, Pet requestBody) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets-ref/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), requestBody).flatMap(e -> mapResponse(e, typeRef));
    }

    public Mono<String> getPetBody(Long petId) {
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>(){};
        return invokeAPI("/pets/{petId}/body", "GET", createUrlVariables("petId", petId), createQueryParameters(), null).flatMap(e -> mapResponse(e, typeRef));
    }

    public Mono<byte[]> getPetThumbnail(Long petId) {
        ParameterizedTypeReference<byte[]> typeRef = new ParameterizedTypeReference<byte[]>(){};
        return invokeAPI("/pets/{petId}/thumbnail", "GET", createUrlVariables("petId", petId), createQueryParameters(), null).flatMap(e -> mapResponse(e, typeRef));
    }

    public Flux<ServerSentEvent> getPetEvents(Long petId) {
        ParameterizedTypeReference<ServerSentEvent> typeRef = new ParameterizedTypeReference<ServerSentEvent>(){};
        return invokeAPI("/pets/{petId}/events", "GET", createUrlVariables("petId", petId), createQueryParameters(), null).flatMapMany(e -> e.bodyToFlux(typeRef));
    }

    public Mono<ClientResponse> getPetDetails(Long petId) {
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets/{petId}/details", "GET", createUrlVariables("petId", petId), createQueryParameters(), null);
    }
}
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

    public Mono<Pet> createPet(Pet pet) {
        ParameterizedTypeReference<Pet> requestType = new ParameterizedTypeReference<Pet>(){};
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets", "POST", createUrlVariables(), createQueryParameters(), createQueryParameters(), pet, requestType).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<Void> deletePetById(Long petId) {
        ParameterizedTypeReference responseType = VOID;
        return invokeAPI("/pets/{petId}", "DELETE", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<String> getPetBody(Long petId) {
        ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<String>(){};
        return invokeAPI("/pets/{petId}/body", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<Pet> getPetById(Long petId, Boolean details) {
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets/{petId}", "GET", createUrlVariables("petId", petId), createQueryParameters("details", details), createQueryParameters(), null, null).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<ClientResponse> getPetDetails(Long petId) {
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets/{petId}/details", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null);
    }

    public Flux<ServerSentEvent> getPetEvents(Long petId, String lastEventId) {
        ParameterizedTypeReference<ServerSentEvent> responseType = new ParameterizedTypeReference<ServerSentEvent>(){};
        return invokeAPI("/pets/{petId}/events", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters("Last-Event-Id", lastEventId), null, null).flatMapMany(e -> e.bodyToFlux(responseType));
    }

    public Mono<byte[]> getPetThumbnail(Long petId) {
        ParameterizedTypeReference<byte[]> responseType = new ParameterizedTypeReference<byte[]>(){};
        return invokeAPI("/pets/{petId}/thumbnail", "GET", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), null, null).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<Void> hiddenServerOp() {
        ParameterizedTypeReference responseType = VOID;
        return invokeAPI("/hidden-server", "GET", createUrlVariables(), createQueryParameters(), createQueryParameters(), null, null).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<List<Pet>> listPets(Integer limit, Integer offsetValue) {
        ParameterizedTypeReference<List<Pet>> responseType = new ParameterizedTypeReference<List<Pet>>(){};
        return invokeAPI("/pets", "GET", createUrlVariables(), createQueryParameters("limit", limit, "Offset-Value", offsetValue), createQueryParameters(), null, null).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<Void> updatePet(Long petId, Pet requestBody) {
        ParameterizedTypeReference<Pet> requestType = new ParameterizedTypeReference<Pet>(){};
        ParameterizedTypeReference responseType = VOID;
        return invokeAPI("/pets/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), requestBody, requestType).flatMap(e -> mapResponse(e, responseType));
    }

    public Mono<Pet> updatePetRefById(Long petId, Pet requestBody) {
        ParameterizedTypeReference<Pet> requestType = new ParameterizedTypeReference<Pet>(){};
        ParameterizedTypeReference<Pet> responseType = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets-ref/{petId}", "PUT", createUrlVariables("petId", petId), createQueryParameters(), createQueryParameters(), requestBody, requestType).flatMap(e -> mapResponse(e, responseType));
    }
}

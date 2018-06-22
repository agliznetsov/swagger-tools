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
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class PetsWebClient extends RestWebClient {
    public PetsWebClient(WebClient webClient) {
        super(webClient);
    }

    public PetsWebClient(WebClient webClient, String basePath) {
        super(webClient, basePath);
    }

    public PetsWebClient(WebClient webClient, String basePath,
            MultiValueMap<String, String> headers) {
        super(webClient, basePath, headers);
    }

    public Mono<List<Pet>> listPets(Integer limit) {
        Map urlVariables = createUrlVariables();
        MultiValueMap parameters = createQueryParameters("limit", limit);
        ParameterizedTypeReference<List<Pet>> typeRef = new ParameterizedTypeReference<List<Pet>>(){};
        return invokeAPI("/pets", HttpMethod.GET, urlVariables, parameters, null).bodyToMono(typeRef);
    }

    public Mono<Pet> createPet(Pet requestBody) {
        Map urlVariables = createUrlVariables();
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets", HttpMethod.POST, urlVariables, parameters, requestBody).bodyToMono(typeRef);
    }

    public Mono<Pet> getPetById(Long petId, Boolean details) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters("details", details);
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets/{petId}", HttpMethod.GET, urlVariables, parameters, null).bodyToMono(typeRef);
    }

    public Mono<Void> updatePet(Long petId, Pet requestBody) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference typeRef = VOID;
        return invokeAPI("/pets/{petId}", HttpMethod.PUT, urlVariables, parameters, requestBody).bodyToMono(typeRef);
    }

    public Mono<Void> deletePetById(Long petId) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference typeRef = VOID;
        return invokeAPI("/pets/{petId}", HttpMethod.DELETE, urlVariables, parameters, null).bodyToMono(typeRef);
    }

    public Mono<Pet> updatePetRefById(Long petId, Pet requestBody) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>(){};
        return invokeAPI("/pets-ref/{petId}", HttpMethod.PUT, urlVariables, parameters, requestBody).bodyToMono(typeRef);
    }

    public Mono<String> getPetBody(Long petId) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference<String> typeRef = new ParameterizedTypeReference<String>(){};
        return invokeAPI("/pets/{petId}/body", HttpMethod.GET, urlVariables, parameters, null).bodyToMono(typeRef);
    }
}
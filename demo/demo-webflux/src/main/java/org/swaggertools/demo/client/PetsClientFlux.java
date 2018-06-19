package org.swaggertools.demo.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.swaggertools.demo.model.Pet;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public class PetsClientFlux extends RestWebClient {
    public PetsClientFlux(WebClient webClient) {
        super(webClient);
    }

    public PetsClientFlux(WebClient webClient, String basePath) {
        super(webClient, basePath);
    }

    public PetsClientFlux(WebClient webClient, String basePath, MultiValueMap<String, String> headers) {
        super(webClient, basePath, headers);
    }

    public Mono<List<Pet>> listPets(Integer limit) {
        Map urlVariables = createUrlVariables();
        MultiValueMap parameters = createQueryParameters("limit", limit);
        ParameterizedTypeReference<List<Pet>> typeRef = new ParameterizedTypeReference<List<Pet>>() {
        };
        return invoke("/pets", HttpMethod.GET, urlVariables, parameters, null).bodyToMono(typeRef);
    }

    public Mono<Pet> createPet(Pet requestBody) {
        Map urlVariables = createUrlVariables();
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>() {
        };
        return invoke("/pets", HttpMethod.POST, urlVariables, parameters, requestBody).bodyToMono(typeRef);
    }

    public Mono<Pet> getPetById(Long petId, Boolean details) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters("details", details);
        ParameterizedTypeReference<Pet> typeRef = new ParameterizedTypeReference<Pet>() {
        };
        return invoke("/pets/{petId}", HttpMethod.GET, urlVariables, parameters, null).bodyToMono(typeRef);
    }

    public Mono<Void> updatePet(Long petId, Pet requestBody) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference typeRef = VOID;
        return invoke("/pets/{petId}", HttpMethod.PUT, urlVariables, parameters, requestBody).bodyToMono(typeRef);
    }

    public Mono<Void> deletePetById(Long petId) {
        Map urlVariables = createUrlVariables("petId", petId);
        MultiValueMap parameters = createQueryParameters();
        ParameterizedTypeReference typeRef = VOID;
        return invoke("/pets/{petId}", HttpMethod.DELETE, urlVariables, parameters, null).bodyToMono(typeRef);
    }
}

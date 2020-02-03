package org.swaggertools.demo.web;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.swaggertools.demo.model.Pet;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PetsController implements PetsApi {
    Long counter = 0L;
    Map<Long, Pet> pets = new HashMap<>();

    @Override
    public Mono<List<Pet>> listPets(Integer limit) {
        Stream<Pet> stream = pets.values().stream();
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return Mono.just(stream.collect(Collectors.toList()));
    }

    @Override
    public Mono<Pet> createPet(@RequestBody Pet requestBody) {
        savePet(requestBody);
        return Mono.just(requestBody);
    }

    private void savePet(@RequestBody Pet requestBody) {
        requestBody.setId(++counter);
        pets.put(requestBody.getId(), requestBody);
    }

    @Override
    public Mono<Pet> getPetById(@PathVariable Long petId, Boolean details, String userId) {
        return Mono.just(getPet(petId, userId));
    }

    @Override
    public Mono<Void> updatePet(@PathVariable Long petId, @RequestBody Pet requestBody) {
        pets.put(petId, requestBody);
        return Mono.empty();
    }

    @Override
    public Mono<Void> deletePetById(@PathVariable Long petId) {
        getPet(petId, null);
        pets.remove(petId);
        return Mono.empty();
    }

    @Override
    public Mono<List<Pet>> createPets(List<Pet> requestBody) {
        requestBody.forEach(this::savePet);
        return Mono.just(requestBody);
    }

    @Override
    public Flux<ServerSentEvent> getPetEvents(Long petId, String lastEventId) {
        return Flux.interval(Duration.ofMillis(100))
                .map(sequence -> ServerSentEvent.<String> builder()
                        .id(String.valueOf(sequence))
                        .data("{\"id\": " + sequence + "}")
                        .build());
    }

    private Pet getPet(Long petId, String userId) {
        Pet pet = pets.get(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found: " + petId);
        }
        pet.setUserId(userId);
        return pet;
    }
}

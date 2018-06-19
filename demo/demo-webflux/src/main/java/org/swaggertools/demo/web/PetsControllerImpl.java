package org.swaggertools.demo.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.swaggertools.demo.model.Pet;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/v1")
public class PetsControllerImpl {
    Long counter = 0L;
    Map<Long, Pet> pets = new HashMap<>();

    @GetMapping("/pets")
    public Mono<List<Pet>> listPets(Integer limit) {
        Stream<Pet> stream = pets.values().stream();
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return Mono.just(stream.collect(Collectors.toList()));
    }

    @PostMapping("/pets")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Pet> createPet(@RequestBody Pet requestBody) {
        requestBody.setId(++counter);
        pets.put(requestBody.getId(), requestBody);
        return Mono.just(requestBody);
    }

    @GetMapping("/pets/{petId}")
    public Mono<Pet> getPetById(@PathVariable Long petId, Boolean details) {
        return Mono.just(getPet(petId));
    }

    @PutMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePet(@PathVariable Long petId, @RequestBody Pet requestBody) {
        pets.put(petId, requestBody);
    }

    @DeleteMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePetById(@PathVariable Long petId) {
        getPet(petId);
        pets.remove(petId);
    }

    private Pet getPet(Long petId) {
        Pet pet = pets.get(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found: " + petId);
        }
        return pet;
    }

}

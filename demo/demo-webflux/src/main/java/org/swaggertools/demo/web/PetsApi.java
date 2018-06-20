package org.swaggertools.demo.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.swaggertools.demo.model.Pet;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1")
public interface PetsApi {
    @GetMapping("/pets")
    Mono<List<Pet>> listPets(@RequestParam(name = "limit", required = false) Integer limit);

    @PostMapping("/pets")
    @ResponseStatus(HttpStatus.CREATED)
    Mono<Pet> createPet(@RequestBody Pet requestBody);

    @GetMapping("/pets/{petId}")
    Mono<Pet> getPetById(@PathVariable(name = "petId", required = true) Long petId,
                   @RequestParam(name = "details", required = false, defaultValue = "false") Boolean details);

    @PutMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> updatePet(@PathVariable(name = "petId", required = true) Long petId,
                   @RequestBody Pet requestBody);

    @DeleteMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> deletePetById(@PathVariable(name = "petId", required = true) Long petId);
}

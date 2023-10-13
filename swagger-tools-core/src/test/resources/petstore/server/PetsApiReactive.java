package com.example.web;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.lang.Void;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
public interface PetsApi {
    @PostMapping("/pets")
    @ResponseStatus(HttpStatus.CREATED)
    Mono<Pet> createPet(@RequestBody(required = true) Pet pet);

    @DeleteMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> deletePetById(@PathVariable(name = "petId", required = true) Long petId);

    @GetMapping(
            value = "/pets/{petId}/body",
            produces = "text/plain"
    )
    Mono<String> getPetBody(@PathVariable(name = "petId", required = true) Long petId);

    @GetMapping("/pets/{petId}")
    Mono<Pet> getPetById(@PathVariable(name = "petId", required = true) Long petId,
            @RequestParam(name = "details", required = false, defaultValue = "false") Boolean details);

    @GetMapping("/pets/{petId}/details")
    Mono<ResponseEntity<Pet>> getPetDetails(
            @PathVariable(name = "petId", required = true) Long petId);

    @GetMapping(
            value = "/pets/{petId}/events",
            produces = "text/event-stream"
    )
    Flux<ServerSentEvent> getPetEvents(@PathVariable(name = "petId", required = true) Long petId,
            @RequestHeader(name = "Last-Event-Id", required = false) String lastEventId);

    @GetMapping(
            value = "/pets/{petId}/thumbnail",
            produces = "image/jpeg"
    )
    Mono<byte[]> getPetThumbnail(@PathVariable(name = "petId", required = true) Long petId);

    @GetMapping("/hidden-client")
    Mono<Void> hiddenClientOp();

    @GetMapping("/pets")
    Mono<List<Pet>> listPets(@RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "Offset-Value", required = false) Integer offsetValue);

    @PutMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    Mono<Void> updatePet(@PathVariable(name = "petId", required = true) Long petId,
            @RequestBody(required = true) Pet requestBody);

    @PutMapping("/pets-ref/{petId}")
    Mono<Pet> updatePetRefById(@PathVariable(name = "petId", required = true) Long petId,
            @RequestBody(required = true) Pet requestBody);

    @PostMapping(
            value = "/xmlTest",
            produces = "application/xml"
    )
    Mono<String> xmlOperation(@RequestBody(required = true) String pet);
}

package com.example.web;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1")
public interface PetsApi {
    @GetMapping("/pets")
    List<Pet> listPets(@RequestParam(name = "limit", required = false) Integer limit,
            @RequestParam(name = "Offset-Value", required = false) Integer offsetValue);

    @PostMapping("/pets")
    @ResponseStatus(HttpStatus.CREATED)
    Pet createPet(@RequestBody(required = true) Pet pet);

    @GetMapping("/pets/{petId}")
    Pet getPetById(@PathVariable(name = "petId", required = true) Long petId,
            @RequestParam(name = "details", required = false, defaultValue = "false") Boolean details);

    @PutMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updatePet(@PathVariable(name = "petId", required = true) Long petId,
            @RequestBody(required = true) Pet requestBody);

    @DeleteMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePetById(@PathVariable(name = "petId", required = true) Long petId);

    @PutMapping("/pets-ref/{petId}")
    Pet updatePetRefById(@PathVariable(name = "petId", required = true) Long petId,
            @RequestBody(required = true) Pet requestBody);

    @GetMapping(
            value = "/pets/{petId}/body",
            produces = "text/plain"
    )
    String getPetBody(@PathVariable(name = "petId", required = true) Long petId);

    @GetMapping(
            value = "/pets/{petId}/thumbnail",
            produces = "image/jpeg"
    )
    byte[] getPetThumbnail(@PathVariable(name = "petId", required = true) Long petId);

    @GetMapping(
            value = "/pets/{petId}/events",
            produces = "text/event-stream"
    )
    SseEmitter getPetEvents(@PathVariable(name = "petId", required = true) Long petId,
            @RequestHeader(name = "Last-Event-Id", required = false) String lastEventId);

    @GetMapping("/pets/{petId}/details")
    ResponseEntity<Pet> getPetDetails(@PathVariable(name = "petId", required = true) Long petId);
}

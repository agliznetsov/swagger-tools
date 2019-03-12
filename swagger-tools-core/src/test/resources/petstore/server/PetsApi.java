package com.example.web;

import com.example.model.Pet;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.String;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public interface PetsApi {
    @GetMapping("/pets")
    List<Pet> listPets(@RequestParam(name = "limit", required = false) Integer limit);

    @PostMapping("/pets")
    @ResponseStatus(HttpStatus.CREATED)
    Pet createPet(@RequestBody Pet pet);

    @GetMapping("/pets/{petId}")
    Pet getPetById(@PathVariable(name = "petId", required = true) Long petId,
            @RequestParam(name = "details", required = false, defaultValue = "false") Boolean details);

    @PutMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updatePet(@PathVariable(name = "petId", required = true) Long petId,
            @RequestBody Pet requestBody);

    @DeleteMapping("/pets/{petId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePetById(@PathVariable(name = "petId", required = true) Long petId);

    @PutMapping("/pets-ref/{petId}")
    Pet updatePetRefById(@PathVariable(name = "petId", required = true) Long petId,
            @RequestBody Pet requestBody);

    @GetMapping("/pets/{petId}/body")
    String getPetBody(@PathVariable(name = "petId", required = true) Long petId);

    @GetMapping("/pets/{petId}/thumbnail")
    byte[] getPetThumbnail(@PathVariable(name = "petId", required = true) Long petId);
}
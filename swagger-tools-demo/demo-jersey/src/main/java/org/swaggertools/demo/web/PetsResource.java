package org.swaggertools.demo.web;

import org.swaggertools.demo.model.Cat;
import org.swaggertools.demo.model.Pet;

import jakarta.inject.Singleton;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class PetsResource implements PetsApi {
    Long counter = 0L;
    Map<Long, Pet> pets = new HashMap<>();

    public PetsResource() {
        pets.put(0L, new Cat());
    }

    @Override
    public List<Pet> listPets(Integer limit) {
        Stream<Pet> stream = pets.values().stream();
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return stream.collect(Collectors.toList());
    }

    @Override
    public Pet createPet(Pet requestBody) {
        requestBody.setId(++counter);
        pets.put(requestBody.getId(), requestBody);
        return requestBody;
    }

    @Override
    public Pet getPetById(Long petId, Boolean details, String userId) {
        return getPet(petId, userId);
    }

    @Override
    public void updatePet(Long petId, Pet requestBody) {
        pets.put(petId, requestBody);
    }

    @Override
    public void deletePetById(Long petId) {
        getPet(petId, null);
        pets.remove(petId);
    }

    @Override
    public void getPetEvents(Long petId, String lastEventId, SseEventSink sseEventSink, Sse sse) {
        try(SseEventSink sink = sseEventSink){
            sink.send(sse.newEvent("data"));
            sink.send(sse.newEvent("MyEventName","more data"));

            OutboundSseEvent event = sse.newEventBuilder().
                    id("EventId").
                    name("EventName").
                    data("Data").
                    reconnectDelay(10000).
                    comment("Anything i wanna comment here!").
                    build();

            sink.send(event);
        }
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

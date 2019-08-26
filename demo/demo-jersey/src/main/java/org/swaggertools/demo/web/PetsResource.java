package org.swaggertools.demo.web;

import org.swaggertools.demo.model.Cat;
import org.swaggertools.demo.model.Pet;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

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
    public Pet getPetById(Long petId, Boolean details) {
        return getPet(petId);
    }

    @Override
    public void updatePet(Long petId, Pet requestBody) {
        pets.put(petId, requestBody);
    }

    @Override
    public void deletePetById(Long petId) {
        getPet(petId);
        pets.remove(petId);
    }

    @Override
    public void getPetEvents(Long petId, SseEventSink sseEventSink, Sse sse) {
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

    private Pet getPet(Long petId) {
        Pet pet = pets.get(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found: " + petId);
        }
        return pet;
    }

}

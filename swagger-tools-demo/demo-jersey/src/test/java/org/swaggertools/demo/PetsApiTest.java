package org.swaggertools.demo;

import org.apache.http.impl.client.HttpClients;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.swaggertools.demo.client.HttpStatusException;
import org.swaggertools.demo.client.PetsClient;
import org.swaggertools.demo.model.Cat;
import org.swaggertools.demo.model.Pet;

import java.util.List;

import static org.junit.Assert.*;

public class PetsApiTest extends JerseyTest {

    PetsClient petsClient;

    @Override
    protected ResourceConfig configure() {
        return new App();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String baseUrl = getBaseUri().toString() + "v1";
        petsClient = new PetsClient(HttpClients.createDefault(), baseUrl);
    }

    @Test
    public void create() {
        Long id = postPet();
        assertNotNull(id);
    }

    @Test
    public void getOne() {
        Long id = postPet();
        Cat cat = (Cat) petsClient.getPetById(id, true, "123");
        assertNotNull(cat);
        assertEquals("cat", cat.getName());
        assertEquals(100, cat.getThumbnail().length);
        assertEquals("123", cat.getUserId());
    }

    @Test
    public void getOneWrongId() {
        try {
            petsClient.getPetById(666L, false, null);
            fail("Exception expected");
        } catch (HttpStatusException e) {
            assertEquals(500, e.getStatusCode());
            assertEquals("Request failed.", e.getStatusText());
            assertTrue(e.getResponseBody().length > 0);
        }
    }

    @Test
    public void update() {
        Long id = postPet();
        Pet pet = petsClient.getPetById(id, true, null);
        pet.setName("new name");
        petsClient.updatePet(id, pet);
        pet = petsClient.getPetById(id, true, null);
        assertEquals("new name", pet.getName());
    }

    @Test
    public void list() {
        postPet();
        postPet();
        List<Pet> response = petsClient.listPets(1);
        assertEquals(1, response.size());
    }

    @Test
    public void delete() {
        Long id = postPet();
        petsClient.deletePetById(id);
        List<Pet> pets = petsClient.listPets(Integer.MAX_VALUE);
        int count = (int) pets.stream().filter(it -> it.getId().equals(id)).count();
        assertEquals(0, count);
    }

    private Pet createPet() {
        Cat cat = new Cat();
        cat.setName("cat");
        cat.setThumbnail(new byte[100]);
        return cat;
    }

    private Long postPet() {
        Pet pet = createPet();
        pet = petsClient.createPet(pet);
        return pet.getId();
    }

}

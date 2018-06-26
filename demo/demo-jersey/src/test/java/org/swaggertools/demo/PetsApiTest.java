package org.swaggertools.demo;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        petsClient = new PetsClient(getBaseUri().toString());
    }

    @Test
    public void create() {
        Long id = postPet();
        assertNotNull(id);
    }

    @Test
    public void getOne() {
        Long id = postPet();
        Cat cat = (Cat) petsClient.getPetById(id, true);
        assertNotNull(cat);
        assertEquals("cat", cat.getName());
        assertEquals(100, cat.getThumbnail().length);
    }

    @Test
    public void getOneWrongId() {
//        try {
            petsClient.getPetById(666L, false);
            fail("Exception expected");
//        } catch (HttpServerErrorException e) {
//            assertEquals(500, e.getStatusCode().value());
//        }
    }

    @Test
    public void update() {
        Long id = postPet();
        Pet pet = petsClient.getPetById(id, true);
        pet.setName("new name");
        petsClient.updatePet(id, pet);
        pet = petsClient.getPetById(id, true);
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

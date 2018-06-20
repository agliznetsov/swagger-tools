package org.swaggertools.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.swaggertools.demo.client.PetsClientFlux;
import org.swaggertools.demo.model.Cat;
import org.swaggertools.demo.model.Pet;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebClientTest {
    @Autowired
    ApplicationContext applicationContext;

    PetsClientFlux petsClient;

    @Before
    public void setUp() {
        String port = this.applicationContext.getEnvironment().getProperty("local.server.port", "8080");
        String baseUrl = "http://localhost:" + port;
        petsClient = new PetsClientFlux(WebClient.builder().baseUrl(baseUrl).build(), "/v1");

    }

    @Test
    public void create() {
        Long id = postPet();
        assertNotNull(id);
    }

    @Test
    public void getOne() {
        Long id = postPet();
        Cat cat = (Cat) petsClient.getPetById(id, true).block();
        assertNotNull(cat);
        assertEquals("cat", cat.getName());
        assertEquals(100, cat.getThumbnail().length);
    }

    @Test
    public void getOneWrongId() {
        try {
            petsClient.getPetById(666L, false).block();
            fail("Exception expected");
        } catch (WebClientResponseException e) {
            assertEquals(500, e.getStatusCode().value());
        }
    }

    @Test
    public void update() {
        Long id = postPet();
        Pet pet = petsClient.getPetById(id, true).block();
        pet.setName("new name");
        petsClient.updatePet(id, pet).block();
        pet = petsClient.getPetById(id, true).block();
        assertEquals("new name", pet.getName());
    }

    @Test
    public void list() {
        postPet();
        postPet();
        List<Pet> response = petsClient.listPets(1).block();
        assertEquals(1, response.size());
    }

    @Test
    public void delete() {
        Long id = postPet();
        petsClient.deletePetById(id).block();
        List<Pet> pets = petsClient.listPets(Integer.MAX_VALUE).block();
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
        return petsClient.createPet(createPet()).block().getId();
    }

}

package org.swaggertools.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.swaggertools.demo.client.PetsClient;
import org.swaggertools.demo.model.Cat;
import org.swaggertools.demo.model.Pet;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetsApiTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    PetsClient petsClient;

    @Before
    public void setUp() throws Exception {
        testRestTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
        petsClient = new PetsClient(testRestTemplate.getRestTemplate());
        petsClient.setBasePath("/v1");
    }

    @Test
    public void create() {
        Long id = postPet();
        assertNotNull(id);
    }

    @Test
    public void getOne() {
        Long id = postPet();
        Pet pet = petsClient.getPetById(id, true);
        assertNotNull(pet);
        assertEquals("cat", pet.getName());
    }

    @Test
    public void getOneWrongId() {
        try {
            petsClient.getPetById(666L, false);
            fail("Exception expected");
        } catch (HttpServerErrorException e) {
            assertEquals(500, e.getStatusCode().value());
        }
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
        return cat;
    }

    private Long postPet() {
        Pet pet = createPet();
        pet = petsClient.createPet(pet);
        return pet.getId();
    }

}

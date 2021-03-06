package org.swaggertools.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.swaggertools.demo.client.PetStore;
import org.swaggertools.demo.model.Cat;
import org.swaggertools.demo.model.Pet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PetsApiTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    PetStore template;

    @Before
    public void setUp() throws Exception {
        testRestTemplate.getRestTemplate().setErrorHandler(new DefaultResponseErrorHandler());
        template = new PetStore(testRestTemplate.getRestTemplate(), "/");
    }

    @Test
    public void create() {
        Long id = postPet();
        assertNotNull(id);
    }

    @Test
    public void createBulk() {
        List<Pet> request = Arrays.asList(createPet(), createPet());
        List<Pet> response = template.pets().createPets(request);
        assertEquals(request.size(), response.size());
    }

    @Test(expected = HttpClientErrorException.class)
    public void test_validation() {
        Cat cat = new Cat();
        cat.setName(null);
        cat.setPrice(-1.0);
        Pet pet = template.pets().createPet(cat);
    }

    @Test
    public void getOne() {
        Long id = postPet();
        Cat cat = (Cat) template.pets().getPetById(id, true, "123");
        assertNotNull(cat);
        assertEquals("cat", cat.getName());
        assertEquals(100, cat.getThumbnail().length);
        assertEquals("123", cat.getUserId());
    }

    @Test
    public void getOneWrongId() {
        try {
            template.pets().getPetById(666L, false, null);
            fail("Exception expected");
        } catch (HttpServerErrorException e) {
            assertEquals(500, e.getStatusCode().value());
        }
    }

    @Test
    public void update() {
        Long id = postPet();
        Pet pet = template.pets().getPetById(id, true, null);
        pet.setName("new name");
        template.pets().updatePet(id, pet);
        pet = template.pets().getPetById(id, true, null);
        assertEquals("new name", pet.getName());
    }

    @Test
    public void list() {
        postPet();
        postPet();
        List<Pet> response = template.pets().listPets(1);
        assertEquals(1, response.size());
    }

    @Test
    public void delete() {
        Long id = postPet();
        template.pets().deletePetById(id);
        List<Pet> pets = template.pets().listPets(Integer.MAX_VALUE);
        int count = (int) pets.stream().filter(it -> it.getId().equals(id)).count();
        assertEquals(0, count);
    }

    @Test
    public void events() {
        template.pets().getPetEvents(1L, (response) -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody()));
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    log.info(line);
                }
            } catch (IOException e) {
            }
            return response;
        });
    }

    private Pet createPet() {
        Cat cat = new Cat();
        cat.setName("cat");
        cat.setPrice(1.0);
        cat.setThumbnail(new byte[100]);
        return cat;
    }

    private Long postPet() {
        Pet pet = createPet();
        pet = template.pets().createPet(pet);
        return pet.getId();
    }

}

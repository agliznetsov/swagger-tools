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

import java.util.List;

import static org.junit.Assert.*;

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
        Cat cat = (Cat) template.pets().getPetById(id, true);
        assertNotNull(cat);
        assertEquals("cat", cat.getName());
        assertEquals(100, cat.getThumbnail().length);
    }

    @Test
    public void getOneWrongId() {
        try {
            template.pets().getPetById(666L, false);
            fail("Exception expected");
        } catch (HttpServerErrorException e) {
            assertEquals(500, e.getStatusCode().value());
        }
    }

    @Test
    public void update() {
        Long id = postPet();
        Pet pet = template.pets().getPetById(id, true);
        pet.setName("new name");
        template.pets().updatePet(id, pet);
        pet = template.pets().getPetById(id, true);
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

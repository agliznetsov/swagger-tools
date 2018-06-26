package org.glassfish.jersey.examples.jackson;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.swaggertools.demo.App;
import org.swaggertools.demo.model.Pet;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JacksonTest extends JerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected ResourceConfig configure() {
        return new App();
    }

    @Test
    public void test_pets() throws Exception {
        String url = getBaseUri().toString();
        HttpResponse<String> jsonResponse = Unirest.get(url + "/pets").asString();
        System.out.println(jsonResponse.getBody());
        assertEquals(200, jsonResponse.getStatus());
    }

    @Test
    public void test_pet_not_found() throws Exception {
        String url = getBaseUri().toString();
        HttpResponse<InputStream> jsonResponse = Unirest.get(url + "/pets/123").asBinary();
        assertEquals(500, jsonResponse.getStatus());
    }

    @Test
    public void test_pet() throws Exception {
        String url = getBaseUri().toString();
        HttpResponse<InputStream> jsonResponse = Unirest.get(url + "/pets/1").asBinary();
        assertEquals(200, jsonResponse.getStatus());
        Pet pet = new com.fasterxml.jackson.databind.ObjectMapper().readValue(jsonResponse.getBody(), Pet.class);
        assertNotNull(pet);
    }
}

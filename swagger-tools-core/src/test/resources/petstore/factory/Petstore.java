package com.example.client;

import java.lang.String;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class Petstore {
    private final RestTemplate restTemplate;

    private final MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();

    private final PetsClient pets;

    private final OwnersClient owners;

    public Petstore(RestTemplate restTemplate, String basePath) {
        this.restTemplate = restTemplate;
        pets = new PetsClient(restTemplate, basePath, headers);
        owners = new OwnersClient(restTemplate, basePath, headers);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public PetsClient pets() {
        return pets;
    }

    public OwnersClient owners() {
        return owners;
    }
}
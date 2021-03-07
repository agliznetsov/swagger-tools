package com.example.client;

import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.web.client.RestTemplate;

public class Petstore {
    private final RestTemplate client;

    private final Map<String, List<String>> headers = new HashMap<String, List<String>>();

    private final PetsClient pets;

    public Petstore(RestTemplate client, String basePath) {
        this.client = client;
        pets = new PetsClient(client, basePath, headers);
    }

    public RestTemplate getClient() {
        return client;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setRequestCustomizer(Consumer<BodyBuilder> requestCustomizer) {
        pets.setRequestCustomizer(requestCustomizer);
    }

    public PetsClient pets() {
        return pets;
    }
}

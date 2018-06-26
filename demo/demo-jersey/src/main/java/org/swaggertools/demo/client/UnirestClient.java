package org.swaggertools.demo.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpMethod;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;

public class UnirestClient {
    protected static final TypeReference<Void> VOID = new TypeReference<Void>() {};
    protected static final Map<String, Collection<String>> EMPTY_MAP = new HashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();
    private String basePath;

    public UnirestClient(String basePath) {
        this.basePath = basePath;
    }

    protected Map<String, Collection<String>> createQueryParameters(Object... keyValues) {
        Map<String, Collection<String>> parameters = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (value != null) {
                if (value instanceof List) {
                    parameters.put(key.toString(), (List) value);
                } else {
                    parameters.put(key.toString(), Collections.singletonList(value.toString()));
                }
            }
        }
        return parameters;
    }

    protected Map<String, String> createUrlVariables(Object... keyValues) {
        Map<String, String> parameters = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (value != null) {
                parameters.put(key.toString(), value.toString());
            }
        }
        return parameters;
    }

    protected <T> T invokeAPI(String path, HttpMethod method, Map<String, String> urlVariables, Map<String, Collection<?>> queryParams, Object body, TypeReference<T> returnType) {
        HttpRequestWithBody request = new HttpRequestWithBody(method, basePath + path);
        urlVariables.forEach(request::routeParam);
        queryParams.forEach(request::queryString);
        try {
            if (body != null) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                objectMapper.writeValue(bout, body);
                request.body(bout.toByteArray());
            }

            HttpResponse<InputStream> response = request.asBinary();
            T responseBody = new ObjectMapper().readValue(response.getBody(), returnType);
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

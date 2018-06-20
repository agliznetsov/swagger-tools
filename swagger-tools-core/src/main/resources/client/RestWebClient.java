package {{package}};

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RestWebClient {
    protected static final ParameterizedTypeReference<Void> VOID = new ParameterizedTypeReference<Void>() {};
    protected static final MultiValueMap<String, String> EMPTY_MAP = new LinkedMultiValueMap<>();

    protected final WebClient webClient;
    protected String basePath = "";
    protected MultiValueMap<String, String> headers;

    public RestWebClient(WebClient webClient, String basePath) {
        this.webClient = webClient;
        this.basePath = basePath;
    }

    public RestWebClient(WebClient webClient, String basePath, MultiValueMap<String, String> headers) {
        this.webClient = webClient;
        this.basePath = basePath;
        this.headers = headers;
    }

    public WebClient getWebClient() {
        return webClient;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    public RestWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected MultiValueMap<String, String> createQueryParameters(Object... keyValues) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
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

    protected Map<String, Object> createUrlVariables(Object... keyValues) {
        Map<String, Object> parameters = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (value != null) {
                parameters.put(key.toString(), value);
            }
        }
        return parameters;
    }

    protected WebClient.ResponseSpec invokeAPI(String path, HttpMethod method, Map<String, ?> urlVariables, MultiValueMap<String, String> queryParams, Object body) {
        WebClient.RequestBodySpec request1 = webClient
                .method(method)
                .uri(builder -> builder
                        .path(basePath + path)
                        .queryParams(queryParams)
                        .build(urlVariables)
                );
        customizeRequest(request1);
        if (body != null) {
            request1.syncBody(body);
        }
        WebClient.RequestBodySpec request = request1;
        return request.retrieve();
    }

    protected void customizeRequest(WebClient.RequestBodySpec request) {
        if (headers != null) {
            headers.forEach((k, v) -> request.header(k, v.toArray(new String[0])));
        }
    }
}

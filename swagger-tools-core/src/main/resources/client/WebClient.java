package {{package}};

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class BaseClient {
    protected static final ParameterizedTypeReference<Void> VOID = new ParameterizedTypeReference<Void>() {};
    private static final Map<String, String> EMPTY_MAP = new HashMap<>();
    private static final MultiValueMap<String, String> EMPTY_MULTI_MAP = new LinkedMultiValueMap<>();

    protected final WebClient webClient;
    protected String basePath = "";
    protected Map<String, List<String>> headers;
    protected Consumer<RequestBodySpec> requestCustomizer;

    public BaseClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public BaseClient(WebClient webClient, String basePath) {
        this.webClient = webClient;
        this.basePath = basePath;
    }

    public BaseClient(WebClient webClient, String basePath, Map<String, List<String>> headers) {
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

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Consumer<RequestBodySpec> getRequestCustomizer() {
        return requestCustomizer;
    }

    public void setRequestCustomizer(Consumer<RequestBodySpec> requestCustomizer) {
        this.requestCustomizer = requestCustomizer;
    }

    protected MultiValueMap<String, String> createQueryParameters(Object... keyValues) {
        if (keyValues.length == 0) {
            return EMPTY_MULTI_MAP;
        }
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

    protected Map<String, String> createUrlVariables(Object... keyValues) {
        if (keyValues.length == 0) {
            return EMPTY_MAP;
        }
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

    protected Mono<ClientResponse> invokeAPI(String path, String method, Map<String, String> urlVariables, MultiValueMap<String, String> queryParams, Object body) {
        WebClient.RequestBodySpec request1 = webClient
                .method(HttpMethod.resolve(method))
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
        return request.exchange();
    }

    protected void customizeRequest(WebClient.RequestBodySpec request) {
        if (headers != null) {
            headers.forEach((k, v) -> request.header(k, v.toArray(new String[0])));
        }
        if (requestCustomizer != null) {
            requestCustomizer.accept(request);
        }
    }
}

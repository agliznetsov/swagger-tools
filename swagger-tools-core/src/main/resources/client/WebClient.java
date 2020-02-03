package {{package}};

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    protected Mono<ClientResponse> invokeAPI(String path,
                                             String method,
                                             Map<String, String> urlVariables,
                                             MultiValueMap<String, String> queryParams,
                                             MultiValueMap<String, String> headerParams,
                                             Object body,
                                             ParameterizedTypeReference requestTypeRef) {
        WebClient.RequestBodySpec request = webClient
                .method(HttpMethod.resolve(method))
                .uri(builder -> builder
                        .path(basePath + path)
                        .queryParams(queryParams)
                        .build(urlVariables)
                );
        headerParams.forEach((k,v) -> request.header(k, v.toArray(new String[0])));
        customizeRequest(request);
        if (body != null) {
            request.body(Mono.just(body), requestTypeRef);
        }
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

    protected  <R> Mono<? extends R> mapResponse(ClientResponse clientResponse, ParameterizedTypeReference<R> typeRef) {
        if (clientResponse.statusCode().isError()) {
            return createResponseException(clientResponse);
        } else {
            return clientResponse.bodyToMono(typeRef);
        }
    }

    protected Mono createResponseException(ClientResponse response) {
        return DataBufferUtils.join(response.body(BodyExtractors.toDataBuffers()))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .defaultIfEmpty(new byte[0])
                .flatMap(bodyBytes -> {
                    String msg = String.format("ClientResponse has erroneous status code: %d %s", response.statusCode().value(),
                            response.statusCode().getReasonPhrase());
                    Charset charset = response.headers().contentType()
                            .map(MimeType::getCharset)
                            .orElse(StandardCharsets.ISO_8859_1);
                    return Mono.error(new WebClientResponseException(msg,
                            response.statusCode().value(),
                            response.statusCode().getReasonPhrase(),
                            response.headers().asHttpHeaders(),
                            bodyBytes,
                            charset));
                });
    }
}

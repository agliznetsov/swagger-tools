package {{package}};

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.RequestBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;

public abstract class BaseClient {
    protected static final TypeReference<Void> VOID = new TypeReference<Void>() {
    };
    protected static final Map<String, Collection<String>> EMPTY_MAP = new HashMap<>();

    private CloseableHttpClient httpClient;
    private String basePath = "";
    private Map<String, List<String>> headers;
    private ObjectMapper objectMapper = new ObjectMapper();
    protected Consumer<RequestBuilder> requestCustomizer;

    public BaseClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public BaseClient(CloseableHttpClient httpClient, String basePath) {
        this.httpClient = httpClient;
        this.basePath = basePath;
    }

    public BaseClient(CloseableHttpClient httpClient, String basePath, Map<String, List<String>> headers) {
        this.httpClient = httpClient;
        this.basePath = basePath;
        this.headers = headers;
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

    public Consumer<RequestBuilder> getRequestCustomizer() {
        return requestCustomizer;
    }

    public void setRequestCustomizer(Consumer<RequestBuilder> requestCustomizer) {
        this.requestCustomizer = requestCustomizer;
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

    protected <T> T invokeAPI(String path, String method, Map<String, String> urlVariables, Map<String, Collection<String>> queryParams, Object body, TypeReference<T> returnType) {
        URI uri = buildUri(path, urlVariables, queryParams);
        RequestBuilder requestBuilder = RequestBuilder.create(method);
        requestBuilder.setUri(uri);
        customizeRequest(requestBuilder);
        if (body != null) {
            setBody(requestBuilder, body);
        }
        HttpUriRequest request = requestBuilder.build();
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                throwHttpStatusException(response);
            }
            HttpEntity entity = response.getEntity();
            T data = null;
            if (returnType != VOID) {
                data = objectMapper.readValue(entity.getContent(), returnType);
            }
            EntityUtils.consumeQuietly(entity);
            return data;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwHttpStatusException(CloseableHttpResponse response) {
        try {
            byte[] body = EntityUtils.toByteArray(response.getEntity());
            throw new HttpStatusException(
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase(),
                    body,
                    Arrays.asList(response.getAllHeaders())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setBody(RequestBuilder request, Object body) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            objectMapper.writeValue(bout, body);
            request.setEntity(new ByteArrayEntity(bout.toByteArray(), ContentType.APPLICATION_JSON));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private URI buildUri(String path, Map<String, String> urlVariables, Map<String, Collection<String>> queryParams) {
        for (Map.Entry<String, String> e : urlVariables.entrySet()) {
            String key = "\\{" + e.getKey() + "\\}";
            path = path.replaceAll(key, e.getValue());
        }

        try {
            URIBuilder uriBuilder = new URIBuilder(new URI(basePath + path));
            for (Map.Entry<String, Collection<String>> e : queryParams.entrySet()) {
                for (String v : e.getValue()) {
                    uriBuilder.setParameter(e.getKey(), v);
                }
            }
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected void customizeRequest(RequestBuilder requestBuilder) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                for (String v : e.getValue()) {
                    requestBuilder.addHeader(e.getKey(), v);
                }
            }
        }
    }

}

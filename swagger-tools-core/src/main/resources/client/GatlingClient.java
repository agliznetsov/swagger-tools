package {{package}};

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Body;
import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpRequestActionBuilder;
import lombok.SneakyThrows;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static io.gatling.javaapi.core.CoreDsl.StringBody;

public abstract class BaseClient {
    protected static final Map<String, Collection<String>> EMPTY_MAP = new HashMap<>();

    private String basePath = "";
    private Map<String, List<String>> headers;
    private ObjectMapper objectMapper = new ObjectMapper();
    protected Consumer<HttpRequestActionBuilder> requestCustomizer;

    public BaseClient() {

    }

    public BaseClient(String basePath) {
        this.basePath = basePath;
    }

    public BaseClient(String basePath, ObjectMapper objectMapper) {
        this.basePath = basePath;
        this.objectMapper = objectMapper;
    }

    public BaseClient(String basePath, ObjectMapper objectMapper, Map<String, List<String>> headers) {
        this.basePath = basePath;
        this.objectMapper = objectMapper;
        this.headers = headers;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Consumer<HttpRequestActionBuilder> getRequestCustomizer() {
        return requestCustomizer;
    }

    public void setRequestCustomizer(Consumer<HttpRequestActionBuilder> requestCustomizer) {
        this.requestCustomizer = requestCustomizer;
    }

    protected Map<String, Collection<String>> createQueryParameters(Object... keyValues) {
        if (keyValues.length == 0) {
            return EMPTY_MAP;
        }
        Map<String, Collection<String>> parameters = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            Object key = keyValues[i];
            Object value = keyValues[i + 1];
            if (value != null) {
                if (value instanceof Collection) {
                    parameters.put(key.toString(), new ArrayList((Collection) value));
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

    protected HttpRequestActionBuilder invokeAPI(String name,
                                                 String path,
                                                 String method,
                                                 Map<String, String> urlVariables,
                                                 Map<String, Collection<String>> queryParams,
                                                 Map<String, Collection<String>> headerParams,
                                                 Object body) {
        String uri = buildUri(path, urlVariables, queryParams);
        var builder = HttpDsl.http(name)
                .httpRequest(method, uri)
                .asJson();
        for (Map.Entry<String, Collection<String>> e : headerParams.entrySet()) {
            for (String v : e.getValue()) {
                builder.header(e.getKey(), v);
            }
        }
        if(body != null) {
            builder = builder.body(toBody(body));
        }
        customizeRequest(builder);
        return builder;
    }

    private String buildUri(String path, Map<String, String> urlVariables, Map<String, Collection<String>> queryParams) {
        for (Map.Entry<String, String> e : urlVariables.entrySet()) {
            String key = "\\{" + e.getKey() + "\\}";
            path = path.replaceAll(key, e.getValue());
        }
        if(!queryParams.isEmpty()) {
            StringBuilder sb = new StringBuilder(path.contains("?") ? path.substring(path.indexOf("?")) : "?");
            for (Map.Entry<String, Collection<String>> e : queryParams.entrySet()) {
                for (String v : e.getValue()) {
                    sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
                    sb.append('=');
                    sb.append(URLEncoder.encode(v, StandardCharsets.UTF_8));
                    sb.append("&");
                }
            }
            sb.delete(sb.length()-1, sb.length()); // remove the last '&'
            return basePath + path + sb.toString();
        } else {
            return basePath + path;
        }
    }

    protected void customizeRequest(HttpRequestActionBuilder requestBuilder) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                for (String v : e.getValue()) {
                    requestBuilder.header(e.getKey(), v);
                }
            }
        }
        if (requestCustomizer != null) {
            requestCustomizer.accept(requestBuilder);
        }
    }

    @SneakyThrows(IOException.class)
    private Body.WithString toBody(Object o) {
        return StringBody(objectMapper.writeValueAsString(o));
    }
}

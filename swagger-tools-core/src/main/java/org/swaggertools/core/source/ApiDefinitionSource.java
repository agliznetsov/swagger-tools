package org.swaggertools.core.source;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.SneakyThrows;
import org.swaggertools.core.config.AutoConfigurable;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.run.Source;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ApiDefinitionSource extends AutoConfigurable<ApiDefinitionSource.Options> implements Source {

    public ApiDefinitionSource() {
        super(new Options());
    }

    @Override
    @SneakyThrows
    public ApiDefinition getApiDefinition() {
        validateConfiguration();
        ObjectMapper mapper = getMapper();
        try (InputStream inputStream = getInputStream()) {
            JsonNode node = mapper.readValue(inputStream, JsonNode.class);
            JsonNode openapi = node.get("openapi");
            JsonNode swagger = node.get("swagger");
            if (openapi != null) {
                return new OpenApiMapper().map(node);
            } else if (swagger != null) {
                return new SwaggerMapper().map(node);
            } else {
                throw new IllegalArgumentException("Unknown input file format");
            }
        }
    }

    @SneakyThrows
    protected InputStream getInputStream() {
        return new FileInputStream(new File(options.location));
    }

    protected ObjectMapper getMapper() {
        if (options.location.toLowerCase().endsWith(".json")) {
            return new ObjectMapper(new JsonFactory());
        } else {
            return new ObjectMapper(new YAMLFactory());
        }
    }

    @Data
    public static class Options {
        String location;
    }

}

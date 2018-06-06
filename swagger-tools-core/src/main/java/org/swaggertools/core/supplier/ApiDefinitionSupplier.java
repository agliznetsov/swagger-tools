package org.swaggertools.core.supplier;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.swaggertools.core.FileFormat;
import org.swaggertools.core.model.ApiDefinition;

import java.io.InputStream;
import java.util.function.Supplier;

@AllArgsConstructor
public class ApiDefinitionSupplier implements Supplier<ApiDefinition> {
    private final InputStream inputStream;
    private final FileFormat fileFormat;

    @Override
    @SneakyThrows
    public ApiDefinition get() {
        ObjectMapper mapper = getMapper();
        try {
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
        } finally {
            inputStream.close();
        }
    }

    private ObjectMapper getMapper() {
        switch (fileFormat) {
            case JSON:
                return new ObjectMapper(new JsonFactory());
            case YAML:
                return new ObjectMapper(new YAMLFactory());
            default:
                throw new IllegalArgumentException("Unknown file format: " + fileFormat);
        }
    }
}

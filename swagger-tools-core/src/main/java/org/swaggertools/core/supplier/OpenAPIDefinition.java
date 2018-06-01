package org.swaggertools.core.supplier;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.swaggertools.core.FileFormat;

import java.io.InputStream;
import java.util.function.Supplier;

@AllArgsConstructor
public class OpenAPIDefinition implements Supplier<OpenAPI> {
    private final InputStream inputStream;
    private final FileFormat fileFormat;

    @Override
    @SneakyThrows
    public OpenAPI get() {
        try {
            return getMapper().readValue(inputStream, OpenAPI.class);
        } finally {
            inputStream.close();
        }
    }

    private ObjectMapper getMapper() {
        switch (fileFormat) {
            case JSON:
                return Json.mapper();
            case YAML:
                return Yaml.mapper();
        }
        throw new IllegalArgumentException("Unknown file format: " + fileFormat);
    }
}

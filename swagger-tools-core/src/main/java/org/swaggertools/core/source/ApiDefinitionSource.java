package org.swaggertools.core.source;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.AutoConfigurable;
import org.swaggertools.core.config.ConfigurationProperty;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.run.Source;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ApiDefinitionSource extends AutoConfigurable<ApiDefinitionSource.Options> implements Source {
    private final ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public ApiDefinitionSource() {
        super(new Options());
        jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    @SneakyThrows
    public ApiDefinition getApiDefinition(String[] sources) {
        validateConfiguration();
        if (sources == null || sources.length == 0) {
            sources = new String[]{options.location};
        }
        if (sources.length == 1) {
            if (sources[0] == null) {
                throw new IllegalArgumentException("File source is not set");
            }
            return mapJson(readFile(new File(sources[0])));
        } else {
            return mergeSources(sources);
        }
    }

    private ApiDefinition mapJson(JsonNode node) {
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

    @SneakyThrows
    private ApiDefinition mergeSources(String[] sources) {
        List<JsonNode> nodes = new ArrayList<>();
        for (String source : sources) {
            if (source != null && !source.isEmpty()) {
                nodes.add(readFile(new File(source)));
            }
        }
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("File source is not set");
        }

        Optional<JsonNode> swaggerSource = nodes.stream().filter(it -> it.get("swagger") != null).findAny();
        if (swaggerSource.isPresent()) {
            throw new IllegalArgumentException("Multiple swagger sources is not supported");
        }

        List<JsonNode> openapiSources = nodes.stream().filter(it -> it.get("openapi") != null).collect(Collectors.toList());
        OpenAPI openAPI = new OpenApiMerger().merge(openapiSources);

        if (options.merged != null) {
            log.info("Writing merged API definition to {}", options.merged);
            try (FileOutputStream out = new FileOutputStream(options.merged)) {
                if (options.merged.toLowerCase().endsWith(".json")) {
                    jsonMapper.writeValue(out, openAPI);
                } else {
                    yamlMapper.writeValue(out, openAPI);
                }
            }
        }

        return new OpenApiMapper().map(openAPI);
    }

    @SneakyThrows
    protected JsonNode readFile(File file) {
        if (file.getName().toLowerCase().endsWith(".json")) {
            return jsonMapper.readValue(file, JsonNode.class);
        } else {
            return yamlMapper.readValue(file, JsonNode.class);
        }
    }

    @Override
    public String getGroupName() {
        return "source";
    }

    @Data
    public static class Options {
        @ConfigurationProperty(description = "Api definition location")
        String location;

        @ConfigurationProperty(description = "Merged Api definition location")
        String merged;
    }

}

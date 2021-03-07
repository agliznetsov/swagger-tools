package org.swaggertools.core.source;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import lombok.SneakyThrows;
import org.swaggertools.core.config.AutoConfigurable;
import org.swaggertools.core.config.ConfigurationProperty;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.run.Source;

import java.io.File;

public class ApiDefinitionSource extends AutoConfigurable<ApiDefinitionSource.Options> implements Source {

    public ApiDefinitionSource() {
        super(new Options());
    }

    ObjectMapper jsonMapper = new ObjectMapper(new JsonFactory());
    ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    @Override
    @SneakyThrows
    public ApiDefinition getApiDefinition(String[] sources) {
        validateConfiguration();
        JsonNode node = getInput(sources);
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

    protected JsonNode getInput(String[] sources) {
        String fileLocation;
        if (sources != null && sources.length > 0) {
            fileLocation = sources[0];
        } else {
            fileLocation = options.location;
        }
        if (fileLocation == null) {
            throw new IllegalArgumentException("File source is not set");
        }
        return readFile(new File(fileLocation));
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
    }

}

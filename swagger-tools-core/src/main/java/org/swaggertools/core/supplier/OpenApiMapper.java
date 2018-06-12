package org.swaggertools.core.supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.swaggertools.core.model.*;

import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.swaggertools.core.util.AssertUtils.notEmpty;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class OpenApiMapper {
    private static final String JSON = "application/json";
    private static final String X_IGNORE = "x-ignore";
    private static final String X_BASE_PATH = "x-base-path";

    static final Pattern REF_PATTERN = Pattern.compile("#/components/(.+)/(.+)");

    OpenAPI openAPI;
    ApiDefinition apiDefinition;

    public ApiDefinition map(JsonNode node) {
        ObjectMapper objectMapper = Json.mapper();
        openAPI = objectMapper.convertValue(node, OpenAPI.class);
        apiDefinition = new ApiDefinition();

        if (openAPI.getExtensions() != null) {
            Object basePath = openAPI.getExtensions().get(X_BASE_PATH);
            if (basePath != null) {
                apiDefinition.setBasePath(basePath.toString());
            }
        }

        if (openAPI.getPaths() != null) {
            openAPI.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((k, v) -> processOperation(path, k, v)));
        }
        if (openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
            openAPI.getComponents().getSchemas().forEach(this::processSchema);
        }
        return apiDefinition;
    }

    private void processSchema(String name, io.swagger.v3.oas.models.media.Schema schema) {
        Schema res = mapSchema(schema);
        res.setName(name);
        apiDefinition.getSchemas().put(name, res);
    }

    private void processOperation(String path, PathItem.HttpMethod method, io.swagger.v3.oas.models.Operation operation) {
        Operation operationInfo = mapOperation(operation);
        if (operationInfo != null) {
            operationInfo.setPath(path);
            operationInfo.setMethod(HttpMethod.valueOf(method.name()));
            apiDefinition.getOperations().add(operationInfo);
        }
    }

    protected Operation mapOperation(io.swagger.v3.oas.models.Operation operation) {
        if (operation.getExtensions() != null && operation.getExtensions().get(X_IGNORE) != null) {
            return null;
        }

        notNull(operation.getOperationId(), "operationId is not set");
        notEmpty(operation.getTags(), "tag is not set");
        Operation res = new Operation();
        res.setOperationId(operation.getOperationId());
        res.setTag(operation.getTags().get(0));
        if (operation.getParameters() != null) {
            operation.getParameters().forEach(it -> res.getParameters().add(mapParameter(it)));
        }
        Parameter body = mapRequestBody(operation.getRequestBody());
        if (body != null) {
            res.getParameters().add(body);
        }
        addResponse(res, operation.getResponses());
        return res;
    }

    private Parameter mapParameter(io.swagger.v3.oas.models.parameters.Parameter parameter) {
        if (parameter.get$ref() != null) {
            parameter = resolveRef(parameter.get$ref());
        }
        Parameter res = new Parameter();
        res.setName(parameter.getName());
        res.setKind("path".equals(parameter.getIn()) ? ParameterKind.PATH : ParameterKind.QUERY);
        res.setRequired(parameter.getRequired() == null ? false : parameter.getRequired());
        res.setSchema(mapSchema(parameter.getSchema()));
        return res;
    }

    private Parameter mapRequestBody(RequestBody requestBody) {
        if (requestBody != null) {
            if (requestBody.get$ref() != null) {
                requestBody = resolveRef(requestBody.get$ref());
            }
            if (requestBody.getContent() != null) {
                MediaType mediaType = requestBody.getContent().get(JSON);
                if (mediaType != null && mediaType.getSchema() != null) {
                    Parameter res = new Parameter();
                    res.setName("requestBody");
                    res.setKind(ParameterKind.BODY);
                    res.setRequired(true);
                    res.setSchema(mapSchema(mediaType.getSchema()));
                    return res;
                }
            }
        }
        return null;
    }

    private void addResponse(Operation info, ApiResponses responses) {
        for (Map.Entry<String, ApiResponse> e : responses.entrySet()) {
            if (!"default".equals(e.getKey())) {
                int statusCode = Integer.valueOf(e.getKey());
                if (statusCode >= 200 && statusCode <= 299) {
                    info.setResponseStatus(HttpStatus.valueOf(statusCode));
                    ApiResponse response = e.getValue();
                    if (response.get$ref() != null) {
                        response = resolveRef(response.get$ref());
                    }
                    if (response.getContent() != null) {
                        MediaType mediaType = response.getContent().get(JSON);
                        if (mediaType != null) {
                            info.setResponseSchema(mapSchema(mediaType.getSchema()));
                        }
                    }
                    break;
                }
            }
        }
    }

    private Schema mapSchema(io.swagger.v3.oas.models.media.Schema<?> schema) {
        if (schema.get$ref() != null) {
            return new ObjectSchema(resolveName(schema.get$ref()));
        } else if (schema instanceof io.swagger.v3.oas.models.media.ArraySchema) {
            return mapArraySchema((io.swagger.v3.oas.models.media.ArraySchema) schema);
        } else if (schema instanceof ComposedSchema) {
            return mapComposedSchema((ComposedSchema) schema);
        } else if ("object".equals(schema.getType())) {
            return mapObjectSchema(schema);
        } else {
            return mapPrimitiveSchema(schema);
        }
    }

    private Schema mapPrimitiveSchema(io.swagger.v3.oas.models.media.Schema<?> schema) {
        PrimitiveSchema res = new PrimitiveSchema();
        res.setType(PrimitiveType.fromSwaggerValue(schema.getType()));
        res.setFormat(schema.getFormat());
        if (schema.getDefault() != null) {
            res.setDefaultValue(schema.getDefault().toString());
        }
        if (schema.getReadOnly() != null) {
            res.setReadOnly(schema.getReadOnly());
        }
        if (schema.getEnum() != null) {
            res.setEnumValues(new LinkedList<>());
            schema.getEnum().forEach(it -> res.getEnumValues().add(it.toString()));
        }
        return res;
    }

    private Schema mapObjectSchema(io.swagger.v3.oas.models.media.Schema<?> schema) {
        ObjectSchema res = new ObjectSchema();
        if (schema.getAdditionalProperties() != null) {
            if (schema.getAdditionalProperties() instanceof io.swagger.v3.oas.models.media.Schema) {
                res.setAdditionalProperties(mapSchema((io.swagger.v3.oas.models.media.Schema<?>) schema.getAdditionalProperties()));
            }
        }
        if (schema.getProperties() != null) {
            res.setProperties(new LinkedList<>());
            schema.getProperties().forEach((k, v) -> res.getProperties().add(mapProperty(k, v)));
        }
        if (schema.getDiscriminator() != null) {
            res.setDiscriminator(schema.getDiscriminator().getPropertyName());
        }
        return res;
    }

    private Schema mapArraySchema(io.swagger.v3.oas.models.media.ArraySchema schema) {
        ArraySchema res = new ArraySchema();
        io.swagger.v3.oas.models.media.Schema items = schema.getItems();
        if (items != null) {
            res.setItemsSchema(mapSchema(items));
        }
        return res;
    }

    private Schema mapComposedSchema(ComposedSchema schema) {
        if (schema.getAllOf() != null) {
            ObjectSchema res = new ObjectSchema();
            for (io.swagger.v3.oas.models.media.Schema s : schema.getAllOf()) {
                if (s.get$ref() != null) {
                    res.setSuperSchema(resolveName(s.get$ref()));
                } else if (s.getProperties() != null) {
                    if (res.getProperties() == null) {
                        res.setProperties(new LinkedList<>());
                    }
                    Map<String, io.swagger.v3.oas.models.media.Schema> properties = s.getProperties();
                    properties.forEach((k, v) -> res.getProperties().add(mapProperty(k, v)));
                }
            }
            return res;
        } else {
            throw new IllegalArgumentException("Unsupported composed schema: " + schema.getTitle());
        }
    }

    private Property mapProperty(String name, io.swagger.v3.oas.models.media.Schema schema) {
        Property res = new Property();
        res.setName(name);
        res.setSchema(mapSchema(schema));
        return res;
    }

    private <T> T resolveRef(String ref) {
        Matcher matcher = REF_PATTERN.matcher(ref);
        if (matcher.matches()) {
            String type = matcher.group(1);
            String name = matcher.group(2);
            switch (type) {
                case "schemas":
                    return (T) openAPI.getComponents().getSchemas().get(name);
                case "parameters":
                    return (T) openAPI.getComponents().getParameters().get(name);
                case "requestBodies":
                    return (T) openAPI.getComponents().getRequestBodies().get(name);
                case "responses":
                    return (T) openAPI.getComponents().getResponses().get(name);
            }
        }
        throw new IllegalArgumentException("Invalid ref: " + ref);
    }

    private String resolveName(String ref) {
        Matcher matcher = REF_PATTERN.matcher(ref);
        if (matcher.matches()) {
            return matcher.group(2);
        }
        throw new IllegalArgumentException("Invalid ref: " + ref);
    }
}
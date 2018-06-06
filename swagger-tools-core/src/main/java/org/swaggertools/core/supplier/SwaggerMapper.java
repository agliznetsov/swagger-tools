package org.swaggertools.core.supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.*;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.*;
import io.swagger.util.Json;
import org.swaggertools.core.model.*;
import org.swaggertools.core.model.HttpMethod;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.Property;

import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.swaggertools.core.util.AssertUtils.notEmpty;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class SwaggerMapper {
    private static final String X_IGNORE = "x-ignore";

    static final Pattern SCHEMA_REF = Pattern.compile("#\\/definitions\\/(.+)");

    ObjectMapper objectMapper;
    Swagger swagger;
    ApiDefinition apiDefinition;

    public ApiDefinition map(JsonNode node) {
        objectMapper = Json.mapper();
        swagger = objectMapper.convertValue(node, Swagger.class);
        apiDefinition = new ApiDefinition();
        apiDefinition.setBasePath(swagger.getBasePath());

        if (swagger.getPaths() != null) {
            swagger.getPaths().forEach((path, pathItem) -> pathItem.getOperationMap().forEach((k, v) -> processOperation(path, k, v)));
        }
        if (swagger.getDefinitions() != null) {
            swagger.getDefinitions().forEach(this::processSchema);
        }
        return apiDefinition;
    }

    private void processSchema(String name, Model model) {
        Schema schema = mapSchema(model);
        schema.setName(name);
        apiDefinition.getSchemas().put(name, schema);
    }

    private void processOperation(String path, io.swagger.models.HttpMethod method, io.swagger.models.Operation operation) {
        Operation operationInfo = mapOperation(operation);
        if (operationInfo != null) {
            operationInfo.setPath(path);
            operationInfo.setMethod(HttpMethod.valueOf(method.name()));
            apiDefinition.getOperations().add(operationInfo);
        }
    }

    protected Operation mapOperation(io.swagger.models.Operation operation) {
        if (operation.getVendorExtensions() != null && operation.getVendorExtensions().get(X_IGNORE) != null) {
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
        addResponse(res, operation.getResponses());
        return res;
    }

    private Parameter mapParameter(io.swagger.models.parameters.Parameter parameter) {
        Parameter res = new Parameter();
        res.setName(parameter.getName());
        res.setKind(ParameterKind.valueOf(parameter.getIn().toUpperCase()));
        res.setRequired(parameter.getRequired());
        if (parameter instanceof BodyParameter) {
            res.setSchema(mapSchema(((BodyParameter) parameter).getSchema()));
        } else if (parameter instanceof AbstractSerializableParameter){
            AbstractSerializableParameter sp = (AbstractSerializableParameter) parameter;
            Schema schema = new Schema();
            schema.setType(sp.getType());
            schema.setFormat(sp.getFormat());
            if (sp.getDefaultValue() != null) {
                schema.setDefaultValue(sp.getDefaultValue().toString());
            }
            res.setSchema(schema);
        } else {
            res = res;
        }
        return res;
    }

//    private Parameter mapRequestBody(RequestBody requestBody) {
//        if (requestBody != null && requestBody.getContent() != null) {
//            MediaType mediaType = requestBody.getContent().get(JSON);
//            if (mediaType != null && mediaType.getSchema() != null) {
//                Parameter res = new Parameter();
//                res.setName("requestBody");
//                res.setKind(ParameterKind.BODY);
//                res.setRequired(true);
//                res.setSchema(mapSchema(mediaType.getSchema()));
//                return res;
//            }
//        }
//        return null;
//    }

    private void addResponse(Operation info, Map<String, Response> responses) {
        for (Map.Entry<String, Response> e : responses.entrySet()) {
            if (!"default".equals(e.getKey())) {
                int statusCode = Integer.valueOf(e.getKey());
                if (statusCode >= 200 && statusCode <= 299) {
                    info.setResponseStatus(HttpStatus.valueOf(statusCode));
                    if (e.getValue().getResponseSchema() != null) {
                        info.setResponseSchema(mapSchema(e.getValue().getResponseSchema()));
                    }
                    break;
                }
            }
        }
    }

    private Schema mapSchema(Model model) {
        if (model == null) {
            return null;
        }

        Schema schema = new Schema();
        if (model instanceof RefModel) {
            schema.setRef(resolveSchemaName(((RefModel) model).get$ref()));
        } else if (model instanceof ArrayModel) {
            io.swagger.models.properties.Property items = ((ArrayModel) model).getItems();
            if (items != null) {
                schema.setType("array");
                schema.setItems(mapPropertySchema(items));
            }
        } else if (model instanceof ModelImpl) {
            mapModelImpl(schema, (ModelImpl) model);
        } else if (model instanceof ComposedModel) {
            mapComposedSchema(schema, (ComposedModel) model);
        } else {
            System.out.println(model.getClass());
        }
        return schema;
    }

    private void mapModelImpl(Schema schema, ModelImpl model) {
        schema.setType(model.getType());
        schema.setFormat(model.getFormat());
        schema.setDiscriminator(model.getDiscriminator());
        schema.setEnumValues(model.getEnum());
        if (model.getDefaultValue() != null) {
            schema.setDefaultValue(model.getDefaultValue().toString());
        }
        if (model.getAdditionalProperties() != null) {
            schema.setType("map");
            schema.setAdditionalProperties(mapPropertySchema(model.getAdditionalProperties()));
        }
        if (model.getProperties() != null) {
            schema.setType("object");
            schema.setProperties(new LinkedList<>());
            model.getProperties().forEach((k, v) -> {
                Property property = mapProperty(k, v);
                schema.getProperties().add(property);
            });
        }
    }

    private Property mapProperty(String name, io.swagger.models.properties.Property property) {
        Property res = new Property();
        res.setName(name);
        res.setSchema(mapPropertySchema(property));
        return res;
    }

    private void mapComposedSchema(Schema schema, ComposedModel model) {
        schema.setType("object");
        if (model.getAllOf() != null) {
            for (Model m : model.getAllOf()) {
                if (m instanceof RefModel) {
                    schema.setSuperSchema(resolveSchemaName(((RefModel) m).get$ref()));
                } else if (m.getProperties() != null) {
                    if (schema.getProperties() == null) {
                        schema.setProperties(new LinkedList<>());
                    }
                    m.getProperties().forEach((k, v) -> schema.getProperties().add(mapProperty(k, v)));
                }
            }
        }
    }

    private Schema mapPropertySchema(io.swagger.models.properties.Property property) {
        Schema schema = new Schema();
        if (property instanceof RefProperty) {
            schema.setRef(resolveSchemaName(((RefProperty) property).get$ref()));
        } else {
            schema.setType(property.getType());
            schema.setFormat(property.getFormat());

            Map map = objectMapper.convertValue(property, Map.class);
            Object def = map.get("default");
            if (def != null) {
                schema.setDefaultValue(def.toString());
            }

            if (property instanceof StringProperty) {
                schema.setEnumValues(((StringProperty) property).getEnum());
            }

            if (property instanceof ArrayProperty) {
                io.swagger.models.properties.Property items = ((ArrayProperty) property).getItems();
                if (items != null) {
                    schema.setItems(mapPropertySchema(items));
                }
            }
        }
        return schema;
    }

    private String resolveSchemaName(String ref) {
        Matcher matcher = SCHEMA_REF.matcher(ref);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid ref: " + ref);
        }
    }
}

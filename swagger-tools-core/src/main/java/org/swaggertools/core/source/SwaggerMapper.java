package org.swaggertools.core.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.*;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.properties.*;
import io.swagger.util.Json;
import org.swaggertools.core.model.*;
import org.swaggertools.core.model.HttpMethod;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Property;
import org.swaggertools.core.util.NameUtils;

import java.util.LinkedList;
import java.util.Map;

import static org.swaggertools.core.model.Extensions.*;
import static org.swaggertools.core.model.Extensions.X_IGNORE_SERVER;
import static org.swaggertools.core.util.AssertUtils.notEmpty;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class SwaggerMapper {

    ObjectMapper objectMapper;
    Swagger swagger;
    ApiDefinition apiDefinition;

    public ApiDefinition map(JsonNode node) {
        objectMapper = Json.mapper();
        swagger = objectMapper.convertValue(node, Swagger.class);
        apiDefinition = new ApiDefinition();
        apiDefinition.setBasePath(swagger.getBasePath());

        if (swagger.getVendorExtensions() != null) {
            Object modelPackage = swagger.getVendorExtensions().get(X_MODEL_PACKAGE);
            if (modelPackage != null) {
                apiDefinition.setModelPackage(modelPackage.toString());
            }
        }

        if (swagger.getPaths() != null) {
            swagger.getPaths().forEach((path, pathItem) -> pathItem.getOperationMap().forEach((k, v) -> processOperation(path, k, v)));
        }
        if (swagger.getDefinitions() != null) {
            swagger.getDefinitions().forEach(this::processSchema);
        }
        return apiDefinition;
    }

    private void processSchema(String name, Model model) {
        Schema schema = mapModel(name, model);
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
        if (operation.getProduces() != null && !operation.getProduces().isEmpty()) {
            String mediaType = operation.getProduces().get(0);
            res.setResponseMediaType(mediaType);
        }
        if (operation.getConsumes() != null && !operation.getConsumes().isEmpty()) {
            String mediaType = operation.getConsumes().get(0);
            res.setRequestMediaType(mediaType);
        }
        res.setResponseEntity(isResponseEntity(operation));

        if (operation.getVendorExtensions() != null) {
            if (operation.getVendorExtensions().get(X_IGNORE_CLIENT) != null) {
                res.setGenerateClient(false);
            }
            if (operation.getVendorExtensions().get(X_IGNORE_SERVER) != null) {
                res.setGenerateServer(false);
            }
        }

        return res;
    }

    private boolean isResponseEntity(io.swagger.models.Operation operation) {
        if (operation.getVendorExtensions() != null) {
            Object value = operation.getVendorExtensions().get(X_RESPONSE_ENTITY);
            if (value != null) {
                return "true".equals(value.toString());
            }
        }
        return false;
    }

    private Parameter mapParameter(io.swagger.models.parameters.Parameter parameter) {
        if (parameter instanceof RefParameter) {
            parameter = swagger.getParameters().get(((RefParameter) parameter).getSimpleRef());
        }
        Parameter res = new Parameter();
        res.setName(parameter.getName());
        res.setKind(ParameterKind.valueOf(parameter.getIn().toUpperCase()));
        res.setRequired(parameter.getRequired());
        if (parameter instanceof BodyParameter) {
            res.setSchema(mapModel(null, ((BodyParameter) parameter).getSchema()));
        } else if (parameter instanceof AbstractSerializableParameter) {
            res.setSchema(mapSerializableParameter((AbstractSerializableParameter) parameter));
        }
        return res;
    }

    private Schema mapSerializableParameter(AbstractSerializableParameter sp) {
        if (sp.getItems() != null) {
            ArraySchema schema = new ArraySchema();
            schema.setItemsSchema(mapPropertySchema(null, null, sp.getItems()));
            return schema;
        } else {
            PrimitiveSchema schema = new PrimitiveSchema();
            schema.setType(PrimitiveType.fromSwaggerValue(sp.getType()));
            schema.setFormat(sp.getFormat());
            if (sp.getDefaultValue() != null) {
                schema.setDefaultValue(sp.getDefaultValue().toString());
            }
            return schema;
        }
    }

    private void addResponse(Operation info, Map<String, Response> responses) {
        for (Map.Entry<String, Response> e : responses.entrySet()) {
            if (!"default".equals(e.getKey())) {
                int statusCode = Integer.valueOf(e.getKey());
                if (statusCode >= 200 && statusCode <= 299) {
                    info.setResponseStatus(HttpStatus.valueOf(statusCode));
                    Response response = e.getValue();
                    if (response instanceof RefResponse) {
                        response = swagger.getResponses().get(((RefResponse) response).getSimpleRef());
                    }
                    Model model = response.getResponseSchema();
                    if (model != null) {
                        if (model instanceof RefModel) {
                            info.setResponseSchema(new ObjectSchema(((RefModel) model).getSimpleRef()));
                        } else {
                            info.setResponseSchema(mapModel(null, model));
                        }
                    }
                    break;
                }
            }
        }
    }

    private Schema mapModel(String name, Model model) {
        if (model instanceof RefModel) {
            ObjectSchema objectSchema = new ObjectSchema();
            objectSchema.setName(((RefModel) model).getSimpleRef());
            return objectSchema;
        } else if (model instanceof ArrayModel) {
            return mapArrayModel((ArrayModel) model);
        } else if (model instanceof ModelImpl) {
            return mapModelImpl(name, (ModelImpl) model);
        } else if (model instanceof ComposedModel) {
            return mapComposedSchema((ComposedModel) model);
        } else {
            throw new IllegalArgumentException("Unknown model: " + model);
        }
    }

    private Schema mapArrayModel(ArrayModel model) {
        ArraySchema schema = new ArraySchema();
        io.swagger.models.properties.Property items = model.getItems();
        if (items != null) {
            schema.setItemsSchema(mapPropertySchema(null, null, items));
        }
        schema.setUniqueItems(model.getUniqueItems());
        schema.setMaxLength(model.getMaxLength());
        schema.setMinLength(model.getMinLength());
        return schema;
    }

    private Schema mapModelImpl(String name, ModelImpl model) {
        if (model.getEnum() != null) {
            PrimitiveSchema schema = new PrimitiveSchema();
            schema.setType(PrimitiveType.STRING);
            schema.setEnumValues(model.getEnum());
            if (model.getDefaultValue() != null) {
                schema.setDefaultValue(model.getDefaultValue().toString());
            }
            return schema;
        } else {
            if (model.getType() == null || model.getType().equals("object")) {
                ObjectSchema schema = new ObjectSchema();
                schema.setDiscriminator(model.getDiscriminator());
                if (model.getAdditionalProperties() != null) {
                    schema.setAdditionalProperties(mapPropertySchema(null, null, model.getAdditionalProperties()));
                }
                if (model.getProperties() != null) {
                    schema.setProperties(new LinkedList<>());
                    model.getProperties().forEach((k, v) -> {
                        Property property = mapProperty(name, k, v);
                        schema.getProperties().add(property);
                    });
                }
                return schema;
            } else {
                PrimitiveSchema schema = new PrimitiveSchema();
                schema.setType(PrimitiveType.fromSwaggerValue(model.getType()));
                schema.setFormat(model.getFormat());
                return schema;
            }
        }
    }

    private Property mapProperty(String className, String propertyName, io.swagger.models.properties.Property property) {
        Property res = new Property();
        res.setName(propertyName);
        res.setSchema(mapPropertySchema(className, propertyName, property));
        return res;
    }

    private Schema mapComposedSchema(ComposedModel model) {
        if (model.getAllOf() != null) {
            ObjectSchema schema = new ObjectSchema();
            for (Model m : model.getAllOf()) {
                if (m instanceof RefModel) {
                    schema.setSuperSchema(((RefModel) m).getSimpleRef());
                } else if (m.getProperties() != null) {
                    if (schema.getProperties() == null) {
                        schema.setProperties(new LinkedList<>());
                    }
                    m.getProperties().forEach((k, v) -> schema.getProperties().add(mapProperty(null, k, v)));
                }
            }
            return schema;
        } else {
            throw new IllegalArgumentException("Unsupported composed schema definition: " + model.getTitle());
        }
    }

    private Schema mapPropertySchema(String className, String propertyName, io.swagger.models.properties.Property property) {
        if (property instanceof RefProperty) {
            ObjectSchema schema = new ObjectSchema();
            schema.setName(((RefProperty) property).getSimpleRef());
            return schema;
        } else if (property instanceof ArrayProperty) {
            ArrayProperty arrayProperty = (ArrayProperty) property;
            ArraySchema schema = new ArraySchema();
            io.swagger.models.properties.Property items = arrayProperty.getItems();
            if (items != null) {
                schema.setItemsSchema(mapPropertySchema(null, null, items));
            }
            schema.setMinLength(arrayProperty.getMinItems());
            schema.setMaxLength(arrayProperty.getMaxItems());
            schema.setUniqueItems(arrayProperty.getUniqueItems());
            return schema;
        } else if (property instanceof MapProperty) {
            ObjectSchema schema = new ObjectSchema();
            schema.setAdditionalProperties(mapPropertySchema(null, null, ((MapProperty) property).getAdditionalProperties()));
            return schema;
        } else if (property instanceof ObjectProperty) {
            return mapObjectProperty(className, propertyName, (ObjectProperty) property);
        } else if (property instanceof UntypedProperty) {
            return null;
        } else {
            PrimitiveSchema schema = new PrimitiveSchema();
            schema.setType(PrimitiveType.fromSwaggerValue(property.getType()));
            schema.setFormat(property.getFormat());

            Map map = objectMapper.convertValue(property, Map.class);
            Object def = map.get("default");
            if (def != null) {
                schema.setDefaultValue(def.toString());
            }

            if (property.getReadOnly() != null) {
                schema.setReadOnly(property.getReadOnly());
            }

            if (property instanceof StringProperty) {
                schema.setEnumValues(((StringProperty) property).getEnum());
            }
            return schema;
        }
    }

    private Schema mapObjectProperty(String className, String propertyName, ObjectProperty property) {
        ObjectSchema schema = new ObjectSchema();
        if (property.getProperties() != null && !property.getProperties().isEmpty()) {
            if (className != null && propertyName != null) {
                String name = className + NameUtils.pascalCase(propertyName);
                schema.setName(name);

                ModelImpl model = new ModelImpl();
                model.setProperties(property.getProperties());
                processSchema(name, model);
            }
        }
        return schema;
    }

}

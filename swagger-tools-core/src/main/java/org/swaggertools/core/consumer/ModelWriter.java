package org.swaggertools.core.consumer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.squareup.javapoet.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.lang.model.element.Modifier;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.consumer.NameUtils.pascalCase;
import static org.swaggertools.core.consumer.NameUtils.sanitize;
import static org.swaggertools.core.util.AssertUtils.notNull;


@Getter
@Setter
public class ModelWriter implements Consumer<OpenAPI> {
    static final TypeName STRING = TypeName.get(String.class);
    static final ClassName LIST = ClassName.get(List.class);
    static final ClassName LINKED_LIST = ClassName.get(LinkedList.class);
    static final ClassName HASH_MAP = ClassName.get(HashMap.class);

    private String packageName;
    private JavaFileWriter writer;
    private RefResolver refResolver;
    private final Map<String, Class> stringFormats = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Map<String, ModelInfo> models = new HashMap<>();

    public ModelWriter() {
        stringFormats.put("date", LocalDate.class);
        stringFormats.put("date-time", OffsetDateTime.class);
        stringFormats.put("uuid", UUID.class);
    }

    @Override
    public void accept(OpenAPI openAPI) {
        notNull(packageName, "packageName is not set");
        notNull(writer, "writer is not set");
        this.refResolver = new RefResolver(openAPI);
        openAPI.getComponents().getSchemas().forEach(this::createModel);
        models.values().forEach(it -> {
            addSubtypes(it);
            writer.write(JavaFile.builder(packageName, it.model.build()).build());
        });
    }

    private void addSubtypes(ModelInfo modelInfo) {
        if (!modelInfo.subTypes.isEmpty()) {
            AnnotationSpec typeInfo = AnnotationSpec.builder(JsonTypeInfo.class)
                    .addMember("use", "$L", "JsonTypeInfo.Id.NAME")
                    .addMember("property", "$S", modelInfo.schema.getDiscriminator().getPropertyName())
                    .addMember("visible", "$L", "true")
                    .build();

            AnnotationSpec.Builder subTypesBuilder = AnnotationSpec.builder(JsonSubTypes.class);
            for (String className : modelInfo.subTypes) {
                subTypesBuilder.addMember("value", "$L", AnnotationSpec.builder(JsonSubTypes.Type.class)
                        .addMember("value", "$L", className + ".class")
                        .addMember("name", "$S", className)
                        .build());
            }

            modelInfo.model.addAnnotation(typeInfo);
            modelInfo.model.addAnnotation(subTypesBuilder.build());
        }
    }

    @SneakyThrows
    private void createModel(String name, Schema<?> schema) {
        TypeSpec.Builder model = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC);

        if (schema.getEnum() != null) {
            model = createEnum(name, schema);
        } else if (schema instanceof ObjectSchema) {
            addProperties(model, schema);
        } else if (schema instanceof ArraySchema) {
            model.superclass(getArrayType((ArraySchema) schema, LINKED_LIST));
        } else if (schema instanceof MapSchema) {
            extendMap(model, (MapSchema) schema);
            addProperties(model, schema);
        } else if (schema instanceof ComposedSchema) {
            processComposedSchema(name, model, (ComposedSchema) schema);
        }

        getModel(name).model = model;
        getModel(name).schema = schema;
    }

    private TypeSpec.Builder createEnum(String name, Schema schema) {
        TypeSpec.Builder model = TypeSpec.enumBuilder(name);
        schema.getEnum().forEach(it -> model.addEnumConstant(it.toString()));
        return model;
    }

    private ModelInfo getModel(String name) {
        return models.computeIfAbsent(name, it -> new ModelInfo());
    }

    private void processComposedSchema(String name, TypeSpec.Builder model, ComposedSchema schema) {
        if (schema.getAllOf() != null) {
            for (Schema s : schema.getAllOf()) {
                if (s instanceof ObjectSchema) {
                    addProperties(model, s);
                } else if (s.get$ref() != null) {
                    String superName = refResolver.resolveSchemaName(s.get$ref());
                    ClassName superClass = ClassName.get(packageName, superName);
                    model.superclass(superClass);
                    getModel(superName).subTypes.add(name);
                }
            }
        }
    }

    private void extendMap(TypeSpec.Builder model, MapSchema schema) {
        Schema valueSchema = (Schema) schema.getAdditionalProperties();
        TypeName valueType = valueSchema != null ? getType(valueSchema) : OBJECT;
        model.superclass(ParameterizedTypeName.get(HASH_MAP, STRING, valueType));
    }

    private void addProperties(TypeSpec.Builder model, Schema<?> schema) {
        if (schema.getProperties() != null) {
            schema.getProperties().forEach((propertyName, propertySchema) -> {
                if (propertySchema.getEnum() != null) {
                    String enumName = pascalCase(propertyName + "Enum");
                    TypeSpec enumSpec = createEnum(enumName, propertySchema)
                            .addModifiers(Modifier.PUBLIC)
                            .build();
                    ClassName typeName = ClassName.get(packageName, model.build().name, enumName);
                    model.addType(enumSpec);
                    addProperty(model, propertyName, typeName, propertySchema);
                } else {
                    addProperty(model, propertyName, getType(propertySchema), propertySchema);
                }
            });
        }
    }

    private void addProperty(TypeSpec.Builder model, String propertyName, TypeName propertyType, Schema propertySchema) {
        FieldSpec field = field(propertyName, propertyType, propertySchema);
        model.addField(field);
        model.addMethod(getter(field));
        model.addMethod(setter(field));
    }

    private FieldSpec field(String name, TypeName type, Schema schema) {
        FieldSpec.Builder builder = FieldSpec.builder(type, sanitize(name))
                .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", name)
                        .build()
                );

        if (schema.getDefault() != null) {
            if (type == STRING) {
                builder.initializer("$S", schema.getDefault());
            } else if (type == FLOAT || type == FLOAT.box()) {
                builder.initializer("$LF", schema.getDefault());
            } else if (type == LONG || type == LONG.box()) {
                builder.initializer("$LL", schema.getDefault());
            } else {
                builder.initializer("$L", schema.getDefault());
            }
        }

        return builder.build();
    }

    private MethodSpec getter(FieldSpec field) {
        String prefix = isBoolean(field.type) ? "is" : "get";
        String name = prefix + pascalCase(field.name);
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(field.type)
                .addStatement("return $N", field.name)
                .build();
    }

    private boolean isBoolean(TypeName type) {
        return type == BOOLEAN || type == BOOLEAN.box();
    }

    private MethodSpec setter(FieldSpec field) {
        String name = "set" + pascalCase(field.name);
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(field.type, field.name)
                .addStatement("this.$N = $N", field.name, field.name)
                .build();
    }

    private TypeName getType(Schema schema) {
        if (schema instanceof ArraySchema) {
            return getArrayType((ArraySchema) schema, LIST);
        } else if (schema.get$ref() != null) {
            String name = refResolver.resolveSchemaName(schema.get$ref());
            return ClassName.get(packageName, name);
        } else {
            return getSimpleType(schema);
        }
    }

    private TypeName getSimpleType(Schema schema) {
        String format = schema.getFormat();
        switch (schema.getType()) {
            case "integer":
                return "int64".equals(format) ? LONG.box() : INT.box();
            case "number":
                return "float".equals(format) ? FLOAT.box() : DOUBLE.box();
            case "boolean":
                return BOOLEAN.box();
            case "string":
                Class clazz = stringFormats.get(format);
                return clazz != null ? TypeName.get(clazz) : STRING;
            case "object":
                return OBJECT;
        }
        throw new IllegalArgumentException("Unknown type: " + schema.getType() + ":" + schema.getFormat());
    }

    private TypeName getArrayType(ArraySchema schema, ClassName superClass) {
        if (schema.getItems() != null) {
            TypeName itemType = getType(schema.getItems());
            return ParameterizedTypeName.get(superClass, itemType);
        } else {
            return superClass;
        }
    }

    private static class ModelInfo {
        public TypeSpec.Builder model;
        public Schema schema;
        public List<String> subTypes = new LinkedList<>();
    }
}

package org.swaggertools.core.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.squareup.javapoet.*;
import lombok.SneakyThrows;
import org.swaggertools.core.consumer.JavaGenerator;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Schema;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.consumer.NameUtils.pascalCase;
import static org.swaggertools.core.consumer.NameUtils.sanitize;


public class JacksonModelGenerator extends JavaGenerator implements Consumer<ApiDefinition> {
    final Map<String, ModelInfo> models = new HashMap<>();

    @Override
    public void accept(ApiDefinition apiDefinition) {
        super.accept(apiDefinition);
        apiDefinition.getSchemas().values().forEach(this::createModel);
        models.values().forEach(it -> {
            addSubtypes(it);
            writer.write(JavaFile.builder(modelPackageName, it.model.build()).indent(indent).build());
        });
    }

    private void addSubtypes(ModelInfo modelInfo) {
        if (!modelInfo.subTypes.isEmpty()) {
            AnnotationSpec typeInfo = AnnotationSpec.builder(JsonTypeInfo.class)
                    .addMember("use", "$L", "JsonTypeInfo.Id.NAME")
                    .addMember("property", "$S", modelInfo.schema.getDiscriminator())
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
    private void createModel(Schema schema) {
        TypeSpec.Builder model = TypeSpec.classBuilder(schema.getName())
                .addModifiers(Modifier.PUBLIC);

        if (schema.getEnumValues() != null) {
            model = createEnum(schema.getName(), schema);
        } else if ("array".equals(schema.getType())) {
            model.superclass(getArrayType(schema, LINKED_LIST));
        } else {
            if (schema.getSuperSchema() != null) {
                extendSchema(model, schema);
            }
            if (schema.getAdditionalProperties() != null) {
                extendMap(model, schema);
            }
            addProperties(model, schema);
        }

        getModel(schema.getName()).model = model;
        getModel(schema.getName()).schema = schema;
    }

    private TypeSpec.Builder createEnum(String name, Schema schema) {
        TypeSpec.Builder model = TypeSpec.enumBuilder(name);
        schema.getEnumValues().forEach(model::addEnumConstant);
        return model;
    }

    private ModelInfo getModel(String name) {
        return models.computeIfAbsent(name, it -> new ModelInfo());
    }

    private void extendSchema(TypeSpec.Builder model, Schema schema) {
        ClassName superClass = ClassName.get(modelPackageName, schema.getSuperSchema());
        model.superclass(superClass);
        getModel(schema.getSuperSchema()).subTypes.add(schema.getName());
    }

    private void extendMap(TypeSpec.Builder model, Schema schema) {
        Schema valueSchema = schema.getAdditionalProperties();
        TypeName valueType = valueSchema != null ? getType(valueSchema) : OBJECT;
        model.superclass(ParameterizedTypeName.get(HASH_MAP, STRING, valueType));
    }

    private void addProperties(TypeSpec.Builder model, Schema schema) {
        if (schema.getProperties() != null) {
            schema.getProperties().forEach((property) -> {
                if (property.getSchema().getEnumValues() != null) {
                    String enumName = pascalCase(property.getName() + "Enum");
                    TypeSpec enumSpec = createEnum(enumName, property.getSchema())
                            .addModifiers(Modifier.PUBLIC)
                            .build();
                    ClassName typeName = ClassName.get(modelPackageName, model.build().name, enumName);
                    model.addType(enumSpec);
                    addProperty(model, property.getName(), typeName, property.getSchema());
                } else {
                    addProperty(model, property.getName(), getType(property.getSchema()), property.getSchema());
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

        if (schema.getDefaultValue() != null) {
            if (schema.getEnumValues() != null) {
                builder.initializer("$T.$L", type, schema.getDefaultValue());
            } else if (type == STRING) {
                builder.initializer("$S", schema.getDefaultValue());
            } else if (type == FLOAT || type == FLOAT.box()) {
                builder.initializer("$LF", schema.getDefaultValue());
            } else if (type == LONG || type == LONG.box()) {
                builder.initializer("$LL", schema.getDefaultValue());
            } else {
                builder.initializer("$L", schema.getDefaultValue());
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

    private static class ModelInfo {
        public TypeSpec.Builder model;
        public Schema schema;
        public List<String> subTypes = new LinkedList<>();
    }
}

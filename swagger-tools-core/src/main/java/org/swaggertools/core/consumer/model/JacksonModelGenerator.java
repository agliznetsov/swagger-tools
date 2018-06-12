package org.swaggertools.core.consumer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.swaggertools.core.consumer.JavaGenerator;
import org.swaggertools.core.model.*;
import org.swaggertools.core.util.NameUtils;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.util.NameUtils.pascalCase;
import static org.swaggertools.core.util.NameUtils.sanitize;


public class JacksonModelGenerator extends JavaGenerator implements Consumer<ApiDefinition> {
    final Map<String, ModelInfo> models = new HashMap<>();

    @Getter
    @Setter
    boolean initializeCollectionFields = true;

    @Override
    public void accept(ApiDefinition apiDefinition) {
        super.accept(apiDefinition);
        apiDefinition.getSchemas().values().forEach(this::createModel);
        models.values().forEach(it -> {
            addSubtypes(it);
            writer.write(JavaFile.builder(modelPackageName, it.typeSpec.build()).indent(indent).build());
        });
    }

    private void addSubtypes(ModelInfo modelInfo) {
        if (!modelInfo.subTypes.isEmpty()) {
            ObjectSchema schema = (ObjectSchema) modelInfo.schema;
            AnnotationSpec typeInfo = AnnotationSpec.builder(JsonTypeInfo.class)
                    .addMember("use", "$L", "JsonTypeInfo.Id.NAME")
                    .addMember("property", "$S", schema.getDiscriminator())
                    .addMember("visible", "$L", "true")
                    .build();

            AnnotationSpec.Builder subTypesBuilder = AnnotationSpec.builder(JsonSubTypes.class);
            for (String className : modelInfo.subTypes) {
                subTypesBuilder.addMember("value", "$L", AnnotationSpec.builder(JsonSubTypes.Type.class)
                        .addMember("value", "$L", className + ".class")
                        .addMember("name", "$S", className)
                        .build());
            }

            modelInfo.typeSpec.addAnnotation(typeInfo);
            modelInfo.typeSpec.addAnnotation(subTypesBuilder.build());
        }
    }

    @SneakyThrows
    private void createModel(Schema schema) {
        TypeSpec.Builder typeSpec = null;

        if (schema instanceof PrimitiveSchema) {
            PrimitiveSchema primitiveSchema = (PrimitiveSchema) schema;
            if (primitiveSchema.getEnumValues() != null) {
                typeSpec = createEnum(schema.getName(), primitiveSchema);
            }
        } else if (schema instanceof ArraySchema) {
            typeSpec = TypeSpec.classBuilder(schema.getName()).addModifiers(Modifier.PUBLIC)
                    .superclass(getArrayType((ArraySchema) schema, true));
        } else if (schema instanceof ObjectSchema) {
            typeSpec = createClass((ObjectSchema) schema);
        }

        if (typeSpec != null) {
            ModelInfo info = getModel(schema.getName());
            info.typeSpec = typeSpec;
            info.schema = schema;
        }
    }

    private TypeSpec.Builder createClass(ObjectSchema schema) {
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(schema.getName()).addModifiers(Modifier.PUBLIC);
        if (schema.getSuperSchema() != null) {
            ClassName superClass = ClassName.get(modelPackageName, schema.getSuperSchema());
            typeSpec.superclass(superClass);
            getModel(schema.getSuperSchema()).subTypes.add(schema.getName());
        } else if (schema.getAdditionalProperties() != null) {
            Schema valueSchema = schema.getAdditionalProperties();
            TypeName valueType = valueSchema != null ? getType(valueSchema, false) : OBJECT;
            typeSpec.superclass(ParameterizedTypeName.get(HASH_MAP, STRING, valueType));
        }
        addProperties(typeSpec, schema);
        return typeSpec;
    }

    private TypeSpec.Builder createEnum(String name, PrimitiveSchema schema) {
        TypeSpec.Builder model = TypeSpec.enumBuilder(name).addModifiers(Modifier.PUBLIC);
        for (String value : schema.getEnumValues()) {
            String valueName = NameUtils.upperCase(value);
            model.addEnumConstant(valueName, TypeSpec.anonymousClassBuilder("")
                    .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                            .addMember("value", "$S", value)
                            .build())
                    .build());
        }
        return model;
    }

    private ModelInfo getModel(String name) {
        return models.computeIfAbsent(name, it -> new ModelInfo());
    }

    private void addProperties(TypeSpec.Builder model, ObjectSchema schema) {
        if (schema.getProperties() != null) {
            schema.getProperties().forEach((property) -> {
                if (property.getSchema().getEnumValues() != null) {
                    String enumName = pascalCase(property.getName() + "Enum");
                    TypeSpec enumSpec = createEnum(enumName, (PrimitiveSchema) property.getSchema()).build();
                    ClassName typeName = ClassName.get(modelPackageName, model.build().name, enumName);
                    model.addType(enumSpec);
                    addProperty(model, property, typeName);
                } else {
                    addProperty(model, property, getType(property.getSchema(), false));
                }
            });
        }
    }

    private void addProperty(TypeSpec.Builder model, Property property, TypeName propertyType) {
        FieldSpec field = field(property.getName(), propertyType, property.getSchema());
        model.addField(field);
        model.addMethod(getter(field));
        if (!property.getSchema().isReadOnly()) {
            model.addMethod(setter(field));
        }
    }

    private FieldSpec field(String name, TypeName type, Schema schema) {
        FieldSpec.Builder builder = FieldSpec.builder(type, sanitize(name))
                .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", name)
                        .build()
                );

        if (schema.isReadOnly()) {
            builder.addModifiers(Modifier.FINAL);
        }

        if (schema.getDefaultValue() != null) {
            if (schema.getEnumValues() != null) {
                builder.initializer("$T.$L", type, NameUtils.upperCase(schema.getDefaultValue()));
            } else if (type == STRING) {
                builder.initializer("$S", schema.getDefaultValue());
            } else if (type == FLOAT || type == FLOAT.box()) {
                builder.initializer("$LF", schema.getDefaultValue());
            } else if (type == LONG || type == LONG.box()) {
                builder.initializer("$LL", schema.getDefaultValue());
            } else {
                builder.initializer("$L", schema.getDefaultValue());
            }
        } else if (initializeCollectionFields && schema.isCollection()) {
            builder.initializer("new $T()", getType(schema, true));
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
        public TypeSpec.Builder typeSpec;
        public Schema schema;
        public List<String> subTypes = new LinkedList<>();
    }
}

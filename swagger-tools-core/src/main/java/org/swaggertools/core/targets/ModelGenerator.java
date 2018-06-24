package org.swaggertools.core.targets;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.squareup.javapoet.*;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.ConfigurationProperty;
import org.swaggertools.core.model.*;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.run.Target;
import org.swaggertools.core.util.NameUtils;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.util.JavaUtils.*;
import static org.swaggertools.core.util.NameUtils.pascalCase;
import static org.swaggertools.core.util.NameUtils.sanitize;

@Slf4j
public class ModelGenerator extends JavaFileGenerator<ModelGenerator.Options> implements Target {
    public static final String NAME = "model";

    static final ClassName TO_STRING = ClassName.get("lombok", "ToString");
    static final ClassName EQUALS = ClassName.get("lombok", "EqualsAndHashCode");

    final Map<String, ModelInfo> models = new HashMap<>();
    final SchemaMapper schemaMapper = new SchemaMapper();

    public ModelGenerator() {
        super(new Options());
    }

    @Override
    public void accept(ApiDefinition apiDefinition) {
        validateConfiguration();
        log.info("Generating model in {}/{}", options.location, options.modelPackage);
        schemaMapper.setModelPackage(options.modelPackage);
        apiDefinition.getSchemas().values().forEach(this::createModel);
        JavaFileWriter writer = createWriter(options.location);
        models.values().forEach(it -> {
            addSubtypes(it);
            writer.write(JavaFile.builder(options.modelPackage, it.typeSpec.build()).indent(indent).build());
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
            typeSpec = TypeSpec.classBuilder(schema.getName())
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(schemaMapper.getType(schema, true));
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
            ClassName superClass = ClassName.get(options.modelPackage, schema.getSuperSchema());
            typeSpec.superclass(superClass);
            getModel(schema.getSuperSchema()).subTypes.add(schema.getName());
        } else if (schema.getAdditionalProperties() != null) {
            Schema valueSchema = schema.getAdditionalProperties();
            TypeName valueType = valueSchema != null ? schemaMapper.getType(valueSchema, false) : OBJECT;
            typeSpec.superclass(ParameterizedTypeName.get(HASH_MAP, STRING, valueType));
        }
        if (options.lombok) {
            typeSpec.addAnnotation(TO_STRING).addAnnotation(EQUALS);
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
                    ClassName typeName = ClassName.get(options.modelPackage, model.build().name, enumName);
                    model.addType(enumSpec);
                    addProperty(model, property, typeName);
                } else {
                    addProperty(model, property, schemaMapper.getType(property.getSchema(), false));
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
                .addModifiers(Modifier.PRIVATE)
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
        } else if (options.initializeCollections && schema.isCollection()) {
            builder.initializer("new $T()", schemaMapper.getType(schema, true));
        }

        return builder.build();
    }

    @Override
    public String getGroupName() {
        return NAME;
    }


    private static class ModelInfo {
        public TypeSpec.Builder typeSpec;
        public Schema schema;
        public List<String> subTypes = new LinkedList<>();
    }

    @Data
    public static class Options {
        @ConfigurationProperty(description = "Model classes target directory", required = true)
        String location;
        @ConfigurationProperty(description = "Models package name", required = true)
        String modelPackage;
        @ConfigurationProperty(description = "Initialize collection fields with empty collection", defaultValue = "true")
        boolean initializeCollections = true;
        @ConfigurationProperty(description = "Annotate model classes with lombok to generate equals/hashCode/toString", defaultValue = "false")
        boolean lombok = false;
    }

}

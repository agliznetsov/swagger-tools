package org.swaggertools.core.targets.model;

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
import org.swaggertools.core.targets.JavaFileGenerator;
import org.swaggertools.core.targets.SchemaMapper;
import org.swaggertools.core.util.NameUtils;

import javax.lang.model.element.Modifier;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.util.JavaUtils.*;
import static org.swaggertools.core.util.NameUtils.*;

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
            writer.write(JavaFile.builder(options.modelPackage, it.typeSpec.build()).indent(INDENT).build());
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
        String schemaName = javaIdentifier(schema.getName());

        if (schema instanceof PrimitiveSchema) {
            PrimitiveSchema primitiveSchema = (PrimitiveSchema) schema;
            if (primitiveSchema.getEnumValues() != null) {
                typeSpec = createEnum(schemaName, primitiveSchema);
            }
        } else if (schema instanceof ArraySchema) {
            typeSpec = TypeSpec.classBuilder(schemaName)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(schemaMapper.getType(schema, true));
        } else if (schema instanceof ObjectSchema) {
            typeSpec = createClass((ObjectSchema) schema);
        }

        if (typeSpec != null) {
            ModelInfo info = getModel(schemaName);
            info.typeSpec = typeSpec;
            info.schema = schema;
        }
    }

    private TypeSpec.Builder createClass(ObjectSchema schema) {
        String schemaName = javaIdentifier(schema.getName());
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(schemaName).addModifiers(Modifier.PUBLIC);
        if (schema.getSuperSchema() != null) {
            String superName = javaIdentifier(schema.getSuperSchema());
            ClassName superClass = ClassName.get(options.modelPackage, superName);
            typeSpec.superclass(superClass);
            getModel(superName).subTypes.add(schemaName);
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
                    String enumName = pascalCase(javaIdentifier(property.getName()) + "Enum");
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
        FieldSpec field = field(property, propertyType, property.getSchema());
        model.addField(field);
        model.addMethod(getter(field));
        if (!property.getSchema().isReadOnly()) {
            model.addMethod(setter(field));
        }
    }

    private FieldSpec field(Property property, TypeName type, Schema schema) {
        String name = property.getName();

        FieldSpec.Builder builder = FieldSpec.builder(type, sanitize(javaIdentifier(name)))
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(AnnotationSpec.builder(JsonProperty.class)
                        .addMember("value", "$S", name)
                        .build()
                );

        if (schema.getDefaultValue() != null) {
            if (schema.isReadOnly()) {
                builder.addModifiers(Modifier.FINAL);
            }
            
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

        if (options.validation) {
            addValidation(builder, property, schema);
        }

        return builder.build();
    }

    private void addValidation(FieldSpec.Builder builder, Property property, Schema schema) {
        if (property.isRequired()) {
            builder.addAnnotation(NotNull.class);
        }
        if (schema instanceof PrimitiveSchema) {
            PrimitiveSchema s = (PrimitiveSchema) schema;
            if (s.getMaximum() != null) {
                builder.addAnnotation(AnnotationSpec.builder(Max.class)
                        .addMember("value", "$L", s.getMaximum())
                        .build()
                );
            }
            if (s.getMinimum() != null) {
                builder.addAnnotation(AnnotationSpec.builder(Min.class)
                        .addMember("value", "$L", s.getMaximum())
                        .build()
                );
            }
            if (s.getPattern() != null) {
                builder.addAnnotation(AnnotationSpec.builder(Pattern.class)
                        .addMember("regexp", "$S", s.getPattern())
                        .build()
                );
            }
            if (s.getMaxLength() != null || s.getMinLength() != null) {
                AnnotationSpec.Builder ann = AnnotationSpec.builder(Size.class);
                if (s.getMaxLength() != null) {
                    ann.addMember("max", "$L", s.getMaxLength());
                }
                if (s.getMinLength() != null) {
                    ann.addMember("min", "$L", s.getMinLength());
                }
                builder.addAnnotation(ann.build());
            }
        }
        if (schema instanceof ObjectSchema || schema instanceof ArraySchema) {
            builder.addAnnotation(Valid.class);
        }
        if (schema instanceof ArraySchema) {
            ArraySchema s = (ArraySchema) schema;
            if (s.getMaxLength() != null || s.getMinLength() != null) {
                AnnotationSpec.Builder ann = AnnotationSpec.builder(Size.class);
                if (s.getMaxLength() != null) {
                    ann.addMember("max", "$L", s.getMaxLength());
                }
                if (s.getMinLength() != null) {
                    ann.addMember("min", "$L", s.getMinLength());
                }
                builder.addAnnotation(ann.build());
            }
        }
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
        @ConfigurationProperty(description = "Annotate model properties with javax.validation.constraints.*", defaultValue = "false")
        boolean validation = false;
    }

}

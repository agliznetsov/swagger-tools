package org.swaggertools.core.targets.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.squareup.javapoet.*;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
public class ModelGenerator extends JavaFileGenerator<ModelOptions> implements Target {
    public static final String NAME = "model";

    static final ClassName TO_STRING = ClassName.get("lombok", "ToString");
    static final ClassName EQUALS = ClassName.get("lombok", "EqualsAndHashCode");
    static final ClassName BUILDER = ClassName.get("lombok", "Builder");
    static final ClassName SUPER_BUILDER = ClassName.get("lombok.experimental", "SuperBuilder");
    static final ClassName ALL_ARGS_CONSTRUCTOR = ClassName.get("lombok", "AllArgsConstructor");
    static final ClassName NO_ARGS_CONSTRUCTOR = ClassName.get("lombok", "NoArgsConstructor");

    final Map<String, ModelInfo> models = new HashMap<>();
    final SchemaMapper schemaMapper;

    public ModelGenerator() {
        super(new ModelOptions());
        schemaMapper = new SchemaMapper(options);
    }

    @Override
    public void accept(ApiDefinition apiDefinition) {
        validateConfiguration();
        setModelPackage(apiDefinition, options);

        log.info("Generating model in {}/{}", options.getLocation(), options.getModelPackage());
        apiDefinition.getSchemas().values().forEach(this::createModel);
        apiDefinition.getSchemas().values().forEach(this::setRootClass);
        JavaFileWriter writer = createWriter(options.getLocation());
        models.values().forEach(it -> {
            addSubtypes(it);
            writer.write(JavaFile.builder(options.getModelPackage(), it.typeSpec.build()).indent(INDENT).build());
        });
    }

    private void addSubtypes(ModelInfo modelInfo) {
        if (modelInfo.schema instanceof ObjectSchema) {
            if (!modelInfo.subTypes.isEmpty()) {
                ObjectSchema schema = (ObjectSchema) modelInfo.schema;
                AnnotationSpec typeInfo = AnnotationSpec.builder(JsonTypeInfo.class)
                        .addMember("use", "$L", "JsonTypeInfo.Id.NAME")
                        .addMember("property", "$S", schema.getDiscriminator())
                        .addMember("visible", "$L", "false")
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
            typeSpec.superclass(ClassName.get(options.getModelPackage(), superName));
        } else if (schema.getAdditionalProperties() != null) {
            Schema valueSchema = schema.getAdditionalProperties();
            TypeName valueType = valueSchema != null ? schemaMapper.getType(valueSchema, false) : OBJECT;
            typeSpec.superclass(ParameterizedTypeName.get(HASH_MAP, STRING, valueType));
        }
        if (options.lombok) {
            typeSpec.addAnnotation(TO_STRING).addAnnotation(EQUALS).addAnnotation(NO_ARGS_CONSTRUCTOR);
            if (schema.getProperties() != null) {
                long propsCount = schema.getProperties().stream().filter(it -> !it.getName().equals(schema.getDiscriminator())).count();
                if (propsCount > 0) {
                    typeSpec.addAnnotation(ALL_ARGS_CONSTRUCTOR);
                }
            }
            ClassName builderName = options.lombokSuperBuilder ? SUPER_BUILDER : BUILDER;
            AnnotationSpec.Builder builder = AnnotationSpec.builder(builderName);
            if (options.lombokUniqueBuilder) {
                builder.addMember("builderMethodName", "$S", camelCase(schemaName) + "Builder");
            }
            typeSpec.addAnnotation(builder.build());
        }
        addProperties(typeSpec, schema);
        return typeSpec;
    }

    private void setRootClass(Schema schema) {
        if (schema instanceof ObjectSchema) {
            ObjectSchema objectSchema = (ObjectSchema)schema;
            if (objectSchema.getSuperSchema() != null) {
                String schemaName = javaIdentifier(schema.getName());
                ModelInfo rootModel = findRootModel(objectSchema.getSuperSchema());
                rootModel.subTypes.add(schemaName);
            }
        }
    }
    private ModelInfo findRootModel(String schemaName) {
        String superName = javaIdentifier(schemaName);
        ModelInfo model = getModel(superName);
        ObjectSchema schema = (ObjectSchema) model.schema;
        if (schema != null && schema.getSuperSchema() != null) {
            return findRootModel(schema.getSuperSchema());
        }
        return model;
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
                if (!property.getName().equals(schema.getDiscriminator())) {
                    if (property.getSchema().getEnumValues() != null) {
                        String enumName = pascalCase(javaIdentifier(property.getName()) + "Enum");
                        TypeSpec enumSpec = createEnum(enumName, (PrimitiveSchema) property.getSchema()).build();
                        ClassName typeName = ClassName.get(options.getModelPackage(), model.build().name, enumName);
                        model.addType(enumSpec);
                        addProperty(model, property, typeName);
                    } else {
                        addProperty(model, property, schemaMapper.getType(property.getSchema(), false));
                    }
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
                        .addMember("value", "$L", s.getMinimum())
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

}

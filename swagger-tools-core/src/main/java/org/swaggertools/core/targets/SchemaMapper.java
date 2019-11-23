package org.swaggertools.core.targets;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.swaggertools.core.model.ArraySchema;
import org.swaggertools.core.model.ObjectSchema;
import org.swaggertools.core.model.PrimitiveSchema;
import org.swaggertools.core.model.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.util.AssertUtils.notNull;
import static org.swaggertools.core.util.JavaUtils.*;
import static org.swaggertools.core.util.NameUtils.javaIdentifier;

public class SchemaMapper {
    @Getter
    protected final Map<String, Class> stringFormats = new HashMap<>();

    private SchemaOptions options;

    public SchemaMapper(SchemaOptions schemaOptions) {
        this.options = schemaOptions;
        stringFormats.put("date", LocalDate.class);
        stringFormats.put("uuid", UUID.class);
        stringFormats.put("binary", byte[].class);
    }

    public TypeName getType(Schema schema, boolean concrete) {
        if (schema instanceof PrimitiveSchema) {
            return getSimpleType((PrimitiveSchema) schema);
        } else if (schema instanceof ArraySchema) {
            return getArrayType((ArraySchema) schema, concrete);
        } else if (schema instanceof ObjectSchema) {
            return getObjectSchema((ObjectSchema) schema, concrete);
        }
        throw new IllegalArgumentException("Unknown type: " + schema.getClass());
    }

    private TypeName getObjectSchema(ObjectSchema schema, boolean concrete) {
        if (schema.getAdditionalProperties() != null) {
            ClassName superClass = concrete ? HASH_MAP : MAP;
            TypeName valueType = getType(schema.getAdditionalProperties(), false);
            return ParameterizedTypeName.get(superClass, STRING, valueType);
        } else {
            if (schema.getName() != null) {
                notNull(options.getModelPackage(), "modelPackage is not set");
                return ClassName.get(options.getModelPackage(), javaIdentifier(schema.getName()));
            } else {
                return OBJECT;
            }
        }
    }

    private TypeName getSimpleType(PrimitiveSchema schema) {
        String format = schema.getFormat();
        switch (schema.getType()) {
            case INTEGER:
                return "int64".equals(format) ? LONG.box() : INT.box();
            case NUMBER:
                return "float".equals(format) ? FLOAT.box() : DOUBLE.box();
            case BOOLEAN:
                return BOOLEAN.box();
            case STRING:
                if ("date-time".equals(format)) {
                    return TypeName.get(getDateTimeClass());
                } else {
                    Class clazz = stringFormats.get(format);
                    return clazz != null ? TypeName.get(clazz) : STRING;
                }
            case FILE:
                return ArrayTypeName.of(TypeName.BYTE);
        }
        throw new IllegalArgumentException("Unknown type: " + schema.getType() + ":" + schema.getFormat());
    }

    @SneakyThrows
    private Class getDateTimeClass() {
        if (options.getDateTimeClass() == null) {
            return OffsetDateTime.class;
        } else {
            return Class.forName(options.getDateTimeClass());
        }
    }

    private TypeName getArrayType(ArraySchema schema, boolean concrete) {
        ClassName superClass = concrete ? ARRAY_LIST : LIST;
        if (schema.getItemsSchema() != null) {
            TypeName itemType = getType(schema.getItemsSchema(), false);
            return ParameterizedTypeName.get(superClass, itemType);
        } else {
            return superClass;
        }
    }
}

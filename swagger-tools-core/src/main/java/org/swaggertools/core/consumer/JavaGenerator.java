package org.swaggertools.core.consumer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.model.*;
import org.swaggertools.core.util.JavaFileWriter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.util.AssertUtils.notNull;

public abstract class JavaGenerator implements Consumer<ApiDefinition> {

    protected static final TypeName STRING = TypeName.get(String.class);
    protected static final ClassName LIST = ClassName.get(List.class);
    protected static final ClassName ARRAY_LIST = ClassName.get(ArrayList.class);
    protected static final ClassName MAP = ClassName.get(Map.class);
    protected static final ClassName HASH_MAP = ClassName.get(HashMap.class);

    @Getter
    @Setter
    protected JavaFileWriter writer;

    @Getter
    @Setter
    protected String modelPackageName;

    @Getter
    protected final Map<String, Class> stringFormats = new HashMap<>();

    @Getter
    @Setter
    protected String indent = "    ";

    public JavaGenerator() {
        stringFormats.put("date", LocalDate.class);
        stringFormats.put("date-time", OffsetDateTime.class);
        stringFormats.put("uuid", UUID.class);
    }

    @Override
    public void accept(ApiDefinition apiDefinition) {
        notNull(writer, "writer is not set");
        notNull(modelPackageName, "modelPackageName is not set");
    }

    protected TypeName getType(Schema schema, boolean concrete) {
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
                return ClassName.get(modelPackageName, schema.getName());
            } else {
                return OBJECT;
            }
        }
    }

    protected TypeName getSimpleType(PrimitiveSchema schema) {
        String format = schema.getFormat();
        switch (schema.getType()) {
            case INTEGER:
                return "int64".equals(format) ? LONG.box() : INT.box();
            case NUMBER:
                return "float".equals(format) ? FLOAT.box() : DOUBLE.box();
            case BOOLEAN:
                return BOOLEAN.box();
            case STRING:
                Class clazz = stringFormats.get(format);
                return clazz != null ? TypeName.get(clazz) : STRING;
        }
        throw new IllegalArgumentException("Unknown type: " + schema.getType() + ":" + schema.getFormat());
    }

    protected TypeName getArrayType(ArraySchema schema, boolean concrete) {
        ClassName superClass = concrete ? ARRAY_LIST : LIST;
        if (schema.getItemsSchema() != null) {
            TypeName itemType = getType(schema.getItemsSchema(), false);
            return ParameterizedTypeName.get(superClass, itemType);
        } else {
            return superClass;
        }
    }

}

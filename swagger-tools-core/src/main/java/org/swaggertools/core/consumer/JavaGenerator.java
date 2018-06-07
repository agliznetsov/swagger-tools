package org.swaggertools.core.consumer;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.model.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;

import static com.squareup.javapoet.TypeName.*;
import static org.swaggertools.core.util.AssertUtils.notNull;

public abstract class JavaGenerator implements Consumer<ApiDefinition> {
    protected static final TypeName STRING = TypeName.get(String.class);
    protected static final ClassName LIST = ClassName.get(List.class);
    protected static final ClassName LINKED_LIST = ClassName.get(LinkedList.class);
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

    protected TypeName getType(Schema schema) {
        if ("array".equals(schema.getType())) {
            return getArrayType(schema, LIST);
        } else if ("map".equals(schema.getType())) {
            TypeName valueType = getType(schema.getAdditionalProperties());
            return ParameterizedTypeName.get(MAP, STRING, valueType);
        } else if (schema.getRef() != null) {
            return ClassName.get(modelPackageName, schema.getRef());
        } else {
            return getSimpleType(schema);
        }
    }

    protected TypeName getSimpleType(Schema schema) {
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

    protected TypeName getArrayType(Schema schema, ClassName superClass) {
        if (schema.getItems() != null) {
            TypeName itemType = getType(schema.getItems());
            return ParameterizedTypeName.get(superClass, itemType);
        } else {
            return superClass;
        }
    }

}

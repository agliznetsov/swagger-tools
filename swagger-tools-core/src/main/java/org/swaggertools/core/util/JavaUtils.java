package org.swaggertools.core.util;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import java.util.*;

import static com.squareup.javapoet.TypeName.BOOLEAN;
import static org.swaggertools.core.util.NameUtils.pascalCase;

public class JavaUtils {
    public static final TypeName STRING = TypeName.get(String.class);
    public static final ClassName LIST = ClassName.get(List.class);
    public static final ClassName SET = ClassName.get(Set.class);
    public static final ClassName HASH_SET = ClassName.get(HashSet.class);
    public static final ClassName ARRAY_LIST = ClassName.get(ArrayList.class);
    public static final ClassName MAP = ClassName.get(Map.class);
    public static final ClassName HASH_MAP = ClassName.get(HashMap.class);


    public static boolean isBoolean(TypeName type) {
        return type == BOOLEAN || type == BOOLEAN.box();
    }

    public static MethodSpec getter(FieldSpec field) {
        String prefix = isBoolean(field.type) ? "is" : "get";
        String name = prefix + pascalCase(field.name);
        return getter(field, name);
    }

    public static MethodSpec getter(FieldSpec field, String name) {
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .returns(field.type)
                .addStatement("return $N", field.name)
                .build();
    }

    public static MethodSpec setter(FieldSpec field) {
        String name = "set" + pascalCase(field.name);
        return MethodSpec.methodBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(field.type, field.name)
                .addStatement("this.$N = $N", field.name, field.name)
                .build();
    }

}

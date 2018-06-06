//package org.swaggertools.core.consumer;
//
//import lombok.AllArgsConstructor;
//import org.swaggertools.core.model.*;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//
//@AllArgsConstructor
//public class RefResolver {
//    static final Pattern SCHEMA_REF = Pattern.compile("#\\/components\\/schemas\\/(.+)");
//
//    final ApiDefinition api;
//
//    public String resolveSchemaName(String ref) {
//        Matcher matcher = SCHEMA_REF.matcher(ref);
//        if (matcher.matches()) {
//            return matcher.group(1);
//        } else {
//            throw new IllegalArgumentException("Invalid ref: " + ref);
//        }
//    }
//
//    public Schema resolveSchema(String ref) {
//        String name = resolveSchemaName(ref);
//        Schema schema = api.getSchemas().get(name);
//        if (schema != null) {
//            return schema;
//        } else {
//            throw new IllegalArgumentException("Schema not found: " + name);
//        }
//    }
//}

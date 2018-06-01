package org.swaggertools.core.consumer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import lombok.AllArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@AllArgsConstructor
public class RefResolver {
    static final Pattern SCHEMA_REF = Pattern.compile("#\\/components\\/schemas\\/(.+)");

    final OpenAPI api;

    public String resolveSchemaName(String ref) {
        Matcher matcher = SCHEMA_REF.matcher(ref);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Invalid ref: " + ref);
        }
    }

    public Schema resolveSchema(String ref) {
        String name = resolveSchemaName(ref);
        Schema schema = api.getComponents().getSchemas().get(name);
        if (schema != null) {
            return schema;
        } else {
            throw new IllegalArgumentException("Schema not found: " + name);
        }
    }
}

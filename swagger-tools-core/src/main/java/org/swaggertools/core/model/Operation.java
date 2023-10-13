package org.swaggertools.core.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

import static org.swaggertools.core.util.NameUtils.camelCase;
import static org.swaggertools.core.util.NameUtils.javaIdentifier;

@Data
public class Operation {
    String operationId;
    String tag;
    String path;
    HttpMethod method;
    String name;
    final List<Parameter> parameters = new LinkedList<>();
    Schema responseSchema;
    HttpStatus responseStatus;
    String responseMediaType;
    String requestMediaType;
    boolean responseEntity;
    boolean generateClient = true;
    boolean generateServer = true;

    public String getJavaIdentifier() {
        return camelCase(javaIdentifier(operationId));
    }
}

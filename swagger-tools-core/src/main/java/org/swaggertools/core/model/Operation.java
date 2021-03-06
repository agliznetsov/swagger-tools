package org.swaggertools.core.model;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

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
    boolean responseEntity;
    boolean generateClient = true;
    boolean generateServer = true;
}

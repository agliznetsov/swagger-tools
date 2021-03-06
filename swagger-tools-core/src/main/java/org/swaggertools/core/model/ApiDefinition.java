package org.swaggertools.core.model;

import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
public class ApiDefinition {
    String basePath;
    String modelPackage;
    final Collection<Operation> operations = new LinkedList<>();
    final Map<String, Schema> schemas = new HashMap<>();
}

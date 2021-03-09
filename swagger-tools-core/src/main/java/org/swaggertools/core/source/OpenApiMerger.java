package org.swaggertools.core.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenApiMerger {
    ObjectNode result;
    JsonNodeFactory nodeFactory;

    public JsonNode merge(List<JsonNode> sources) {
        nodeFactory = JsonNodeFactory.instance;
        result = new ObjectNode(nodeFactory);
        for (JsonNode part : sources) {
            if (part instanceof ObjectNode) {
                merge((ObjectNode) part);
            }
        }
        return result;
    }

    private void merge(ObjectNode part) {
        result.set("openapi", new TextNode("3.0.1"));
        copyNode(part, "info");
        copyNode(part, "externalDocs");
        copyNode(part, "servers");
        copyNode(part, "security");
        copyNode(part, "tags");
        mergeNode(part, result, "paths");
        mergeComponents(part);
        mergeExtensions(part);
    }

    private void copyNode(ObjectNode part, String path) {
        JsonNode node = part.get(path);
        if (node != null) {
            if (result.get(path) == null) {
                result.set(path, node);
            } else {
                throw new IllegalArgumentException("Duplicate '" + path + "'");
            }
        }
    }

    private void mergeNode(JsonNode source, ObjectNode target, String path) {
        ObjectNode value = (ObjectNode) source.get(path);
        if (value == null) {
            return;
        }

        ObjectNode destination = (ObjectNode) target.get(path);
        if (destination == null) {
            destination = new ObjectNode(nodeFactory);
            target.set(path, destination);
        }

        for (Iterator<Map.Entry<String, JsonNode>> it = value.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> e = it.next();
            if (destination.has(e.getKey())) {
                throw new IllegalArgumentException("Duplicate node: " + e.getKey());
            }
            destination.set(e.getKey(), e.getValue());
        }
    }

    private void mergeComponents(ObjectNode part) {
        ObjectNode value = (ObjectNode) part.get("components");
        if (value == null) {
            return;
        }

        ObjectNode destination = (ObjectNode) result.get("components");
        if (destination == null) {
            destination = new ObjectNode(nodeFactory);
            result.set("components", destination);
        }

        mergeNode(value, destination, "schemas");
        mergeNode(value, destination, "responses");
        mergeNode(value, destination, "parameters");
        mergeNode(value, destination, "examples");
        mergeNode(value, destination, "requestBodies");
        mergeNode(value, destination, "headers");
        mergeNode(value, destination, "securitySchemes");
        mergeNode(value, destination, "links");
        mergeNode(value, destination, "callbacks");
    }


    private void mergeExtensions(ObjectNode part) {
        for (Iterator<Map.Entry<String, JsonNode>> it = part.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> e = it.next();
            if (e.getKey().startsWith("x-")) {
                Object existingValue = result.get(e.getKey());
                if (existingValue == null) {
                    result.set(e.getKey(), e.getValue());
                } else if (existingValue.equals(e.getValue())) {
                    //value is already set
                } else {
                    throw new IllegalArgumentException("Conflict extension: " + e.getKey());
                }
            }
        }
    }

}

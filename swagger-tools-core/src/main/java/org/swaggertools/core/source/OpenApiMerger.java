package org.swaggertools.core.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OpenApiMerger {
    ObjectMapper objectMapper = Json.mapper();
    OpenAPI openAPI = new OpenAPI();

    public OpenAPI merge(List<JsonNode> sources) {
        for (JsonNode node : sources) {
            OpenAPI part = objectMapper.convertValue(node, OpenAPI.class);
            merge(part);
        }
        return openAPI;
    }

    private void merge(OpenAPI part) {
        mergeInfo(part);
        mergeExternalDocs(part);
        mergeServers(part);
        mergeSecurity(part);
        mergeTags(part);
        mergePaths(part);
        mergeComponents(part);
        mergeExtensions(part);
    }

    private void mergeInfo(OpenAPI part) {
        if (part.getInfo() != null) {
            if (openAPI.getInfo() == null) {
                openAPI.setInfo(part.getInfo());
            } else {
                throw new IllegalArgumentException("Duplicate 'info'");
            }
        }
    }

    private void mergeExternalDocs(OpenAPI part) {
        if (part.getExternalDocs() != null) {
            if (openAPI.getExternalDocs() == null) {
                openAPI.setExternalDocs(part.getExternalDocs());
            } else {
                throw new IllegalArgumentException("Duplicate 'externalDocs'");
            }
        }
    }

    private void mergeServers(OpenAPI part) {
        if (part.getServers() != null) {
            if (openAPI.getServers() == null) {
                openAPI.setServers(new ArrayList<>());
            }
            openAPI.getServers().addAll(part.getServers());
        }
    }

    private void mergeSecurity(OpenAPI part) {
        if (part.getSecurity() != null) {
            if (openAPI.getSecurity() == null) {
                openAPI.setSecurity(new ArrayList<>());
            }
            openAPI.getSecurity().addAll(part.getSecurity());
        }
    }

    private void mergeTags(OpenAPI part) {
        if (part.getTags() != null) {
            if (openAPI.getTags() == null) {
                openAPI.setTags(new ArrayList<>());
            }
            for (Tag tag : part.getTags()) {
                if (openAPI.getTags().stream().filter(it -> it.getName().equals(tag.getName())).findAny().isPresent()) {
                    throw new IllegalArgumentException("Duplicate tag: " + tag.getName());
                }
                openAPI.getTags().add(tag);
            }
        }
    }

    private void mergePaths(OpenAPI part) {
        if (part.getPaths() != null) {
            if (openAPI.getPaths() == null) {
                openAPI.setPaths(new Paths());
            }
            for (Map.Entry<String, PathItem> e : part.getPaths().entrySet()) {
                if (openAPI.getPaths().containsKey(e.getKey())) {
                    throw new IllegalArgumentException("Duplicate path: " + e.getKey());
                }
                openAPI.getPaths().put(e.getKey(), e.getValue());
            }
        }
    }

    private void mergeComponents(OpenAPI part) {
        if (part.getComponents() != null) {
            if (openAPI.getComponents() == null) {
                openAPI.setComponents(new Components());
            }

            mergeComponents(part.getComponents().getSchemas(), openAPI.getComponents()::getSchemas, openAPI.getComponents()::setSchemas);
            mergeComponents(part.getComponents().getResponses(), openAPI.getComponents()::getResponses, openAPI.getComponents()::setResponses);
            mergeComponents(part.getComponents().getParameters(), openAPI.getComponents()::getParameters, openAPI.getComponents()::setParameters);
            mergeComponents(part.getComponents().getExamples(), openAPI.getComponents()::getExamples, openAPI.getComponents()::setExamples);
            mergeComponents(part.getComponents().getRequestBodies(), openAPI.getComponents()::getRequestBodies, openAPI.getComponents()::setRequestBodies);
            mergeComponents(part.getComponents().getHeaders(), openAPI.getComponents()::getHeaders, openAPI.getComponents()::setHeaders);
            mergeComponents(part.getComponents().getSecuritySchemes(), openAPI.getComponents()::getSecuritySchemes, openAPI.getComponents()::setSecuritySchemes);
            mergeComponents(part.getComponents().getLinks(), openAPI.getComponents()::getLinks, openAPI.getComponents()::setLinks);
            mergeComponents(part.getComponents().getCallbacks(), openAPI.getComponents()::getCallbacks, openAPI.getComponents()::setCallbacks);
        }
    }

    private <T> void mergeComponents(Map<String, T> source, Supplier<Map<String, T>> getter, Consumer<Map<String, T>> setter) {
        if (source == null) {
            return;
        }

        Map<String, T> target = getter.get();
        if (target == null) {
            target = new HashMap<>();
            setter.accept(target);
        }

        for (Map.Entry<String, T> e : source.entrySet()) {
            if (target.containsKey(e.getKey())) {
                throw new IllegalArgumentException("Duplicate component: " + e.getKey());
            }
            target.put(e.getKey(), e.getValue());
        }
    }

    private void mergeExtensions(OpenAPI part) {
        if (part.getExtensions() != null) {
            if (openAPI.getExtensions() == null) {
                openAPI.setExtensions(new HashMap<>());
            }
            mergeExtensionValues(openAPI.getExtensions(), part.getExtensions());
        }
    }

    private static void mergeExtensionValues(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> e : source.entrySet()) {
            Object existingValue = target.get(e.getKey());
            if (existingValue == null) {
                target.put(e.getKey(), e.getValue());
            } else if (existingValue.equals(e.getValue())) {
                //value is already set
            } else {
                throw new IllegalArgumentException("Conflict extension: " + e.getKey());
            }
        }
    }

}

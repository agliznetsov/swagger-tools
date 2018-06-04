package org.swaggertools.core.consumer.spring.web;

import com.squareup.javapoet.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.Getter;
import lombok.Setter;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.swaggertools.core.consumer.NameUtils.*;
import static org.swaggertools.core.util.AssertUtils.notEmpty;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class ServerGenerator extends ApiGenerator implements Consumer<OpenAPI> {
    private static final String SPRING_ANNOTATIONS = "org.springframework.web.bind.annotation";
    private static final String SPRING_HTTP = "org.springframework.http";
    private static final String REST_CONTROLLER = "RestController";
    private static final String REQUEST_BODY = "RequestBody";
    private static final String REQUEST_PARAM = "RequestParam";
    private static final String PATH_VARIABLE = "PathVariable";
    private static final String RESPONSE_STATUS = "ResponseStatus";
    private static final String HTTP_STATUS = "HttpStatus";

    @Getter
    @Setter
    String apiPackageName;

    @Getter
    @Setter
    String apiSuffix = "Api";

    final Map<String, ApiInfo> apis = new HashMap<>();

    @Override
    public void accept(OpenAPI openAPI) {
        super.accept(openAPI);
        notNull(modelPackageName, "modelPackageName is not set");
        notNull(apiPackageName, "apiPackageName is not set");
        openAPI.getPaths().forEach(this::processPath);
        apis.forEach((k, v) -> writer.write(JavaFile.builder(apiPackageName, v.api.build()).build()));
    }

    private void processPath(String path, PathItem pathItem) {
        pathItem.readOperationsMap().forEach((k, v) -> processOperation(path, k, v));
    }

    private void processOperation(String path, PathItem.HttpMethod method, Operation operation) {
        notNull(operation.getOperationId(), "operationId is not set");
        notEmpty(operation.getTags(), "tag is not set");
        String tag = operation.getTags().get(0);

        OperationInfo operationInfo = getOperationInfo(operation);

        String methodName = camelCase(sanitize(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        addMapping(path, method, builder);
        addParameters(builder, operationInfo);
        addResponse(builder, operationInfo);

        getApi(tag).api.addMethod(builder.build());
    }

    private void addMapping(String path, PathItem.HttpMethod method, MethodSpec.Builder builder) {
        String mappingName = pascalCase(method.name().toLowerCase()) + "Mapping";
        builder.addAnnotation(AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, mappingName))
                .addMember("value", "$S", path)
                .build()
        );
    }

    private void addParameters(MethodSpec.Builder builder, OperationInfo operationInfo) {
        operationInfo.parameters.forEach(p -> {
            AnnotationSpec.Builder anno;
            if (p.kind == ParameterKind.BODY) {
                anno = AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, REQUEST_BODY));
            } else {
                String inType = p.kind == ParameterKind.PATH ? PATH_VARIABLE : REQUEST_PARAM;
                anno = AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, inType))
                        .addMember("name", "$S", p.name)
                        .addMember("required", "$L", p.required);

                if (p.schema.getDefault() != null) {
                    anno.addMember("defaultValue", "$S", p.schema.getDefault().toString());
                }
            }
            ParameterSpec param = ParameterSpec.builder(getType(p.schema), p.name)
                    .addAnnotation(anno.build())
                    .build();
            builder.addParameter(param);
        });
    }

    private void addResponse(MethodSpec.Builder builder, OperationInfo operationInfo) {
        if (operationInfo.responseSchema != null) {
            builder.returns(getType(operationInfo.responseSchema));
        }
        if (operationInfo.responseStatus != HttpStatus.OK) {
            String statusName = operationInfo.responseStatus.name();
            builder.addAnnotation(
                    AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, RESPONSE_STATUS))
                            .addMember("value", "$T." + statusName, ClassName.get(SPRING_HTTP, HTTP_STATUS))
                            .build()
            );
        }
    }

    private ApiInfo getApi(String tag) {
        return apis.computeIfAbsent(tag, it -> new ApiInfo(pascalCase(sanitize(it) + apiSuffix)));
    }

    private static class ApiInfo {
        public final TypeSpec.Builder api;

        public ApiInfo(String name) {
            api = TypeSpec.interfaceBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, REST_CONTROLLER)).build());
        }
    }

}

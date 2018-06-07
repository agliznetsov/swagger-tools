package org.swaggertools.core.consumer.spring.web;

import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.consumer.JavaGenerator;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.HttpStatus;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.ParameterKind;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.swaggertools.core.util.NameUtils.*;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class ServerGenerator extends JavaGenerator implements Consumer<ApiDefinition> {
    private static final String SPRING_ANNOTATIONS = "org.springframework.web.bind.annotation";
    private static final ClassName REST_CONTROLLER = ClassName.get(SPRING_ANNOTATIONS, "RestController");
    private static final ClassName REQUEST_BODY = ClassName.get(SPRING_ANNOTATIONS, "RequestBody");
    private static final ClassName REQUEST_PARAM = ClassName.get(SPRING_ANNOTATIONS, "RequestParam");
    private static final ClassName REQUEST_MAPPING = ClassName.get(SPRING_ANNOTATIONS, "RequestMapping");
    private static final ClassName PATH_VARIABLE = ClassName.get(SPRING_ANNOTATIONS, "PathVariable");
    private static final ClassName RESPONSE_STATUS = ClassName.get(SPRING_ANNOTATIONS, "ResponseStatus");
    private static final ClassName HTTP_STATUS = ClassName.get("org.springframework.http", "HttpStatus");

    @Getter
    @Setter
    String apiPackageName;

    @Getter
    @Setter
    String apiSuffix = "Api";

    final Map<String, ApiInfo> apis = new HashMap<>();

    @Override
    public void accept(ApiDefinition apiDefinition) {
        super.accept(apiDefinition);
        notNull(modelPackageName, "modelPackageName is not set");
        notNull(apiPackageName, "apiPackageName is not set");
        apiDefinition.getOperations().forEach(this::processOperation);
        apis.forEach((k, v) -> writeApi(apiDefinition, v));
    }

    private void writeApi(ApiDefinition apiDefinition, ApiInfo apiInfo) {
        if (apiDefinition.getBasePath() != null) {
            apiInfo.api.addAnnotation(AnnotationSpec.builder(REQUEST_MAPPING)
                    .addMember("value", "$S", apiDefinition.getBasePath())
                    .build()
            );
        }
        writer.write(JavaFile.builder(apiPackageName, apiInfo.api.build()).indent(indent).build());
    }

    private void processOperation(Operation operation) {
        String methodName = camelCase(sanitize(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        addMapping(builder, operation);
        addParameters(builder, operation);
        addResponse(builder, operation);

        getApi(operation.getTag()).api.addMethod(builder.build());
    }

    private void addMapping(MethodSpec.Builder builder, Operation operation) {
        String mappingName = pascalCase(operation.getMethod().name().toLowerCase()) + "Mapping";
        builder.addAnnotation(AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, mappingName))
                .addMember("value", "$S", operation.getPath())
                .build()
        );
    }

    private void addParameters(MethodSpec.Builder builder, Operation operationInfo) {
        operationInfo.getParameters().forEach(p -> {
            AnnotationSpec.Builder anno;
            if (p.getKind() == ParameterKind.BODY) {
                anno = AnnotationSpec.builder(REQUEST_BODY);
            } else {
                ClassName inType = p.getKind() == ParameterKind.PATH ? PATH_VARIABLE : REQUEST_PARAM;
                anno = AnnotationSpec.builder(inType)
                        .addMember("name", "$S", p.getName())
                        .addMember("required", "$L", p.isRequired());

                if (p.getSchema().getDefaultValue() != null) {
                    anno.addMember("defaultValue", "$S", p.getSchema().getDefaultValue());
                }
            }
            ParameterSpec param = ParameterSpec.builder(getType(p.getSchema()), p.getName())
                    .addAnnotation(anno.build())
                    .build();
            builder.addParameter(param);
        });
    }

    private void addResponse(MethodSpec.Builder builder, Operation operationInfo) {
        if (operationInfo.getResponseSchema() != null) {
            builder.returns(getType(operationInfo.getResponseSchema()));
        }
        if (operationInfo.getResponseStatus() != HttpStatus.OK) {
            String statusName = operationInfo.getResponseStatus().name();
            builder.addAnnotation(
                    AnnotationSpec.builder(RESPONSE_STATUS)
                            .addMember("value", "$T." + statusName, HTTP_STATUS)
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
                    .addAnnotation(AnnotationSpec.builder(REST_CONTROLLER).build());
        }
    }

}

package org.swaggertools.core.consumer.spring.mvc.server;

import com.squareup.javapoet.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.consumer.JavaGenerator;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.swaggertools.core.consumer.NameUtils.*;
import static org.swaggertools.core.util.AssertUtils.notEmpty;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class MvcServerGenerator extends JavaGenerator implements Consumer<OpenAPI> {
    private static final String JSON = "application/json";

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
        pathItem.readOperationsMap().forEach((k, v) -> processOperation(path, pathItem, k, v));
    }

    private void processOperation(String path, PathItem pathItem, PathItem.HttpMethod method, Operation operation) {
        notNull(operation.getOperationId(), "operationId is not set");
        notEmpty(operation.getTags(), "tag is not set");
        String tag = operation.getTags().get(0);

        ApiInfo apiInfo = getApi(tag);

        String methodName = camelCase(sanitize(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        addMapping(path, method, builder);
        if (operation.getParameters() != null) {
            operation.getParameters().forEach(it -> addParameter(builder, it));
        }
        addRequestBody(builder, operation.getRequestBody());
        addResponse(operation, builder);

        apiInfo.api.addMethod(builder.build());
    }

    private void addRequestBody(MethodSpec.Builder builder, RequestBody requestBody) {
        if (requestBody != null && requestBody.getContent() != null) {
            MediaType mediaType = requestBody.getContent().get(JSON);
            if (mediaType != null && mediaType.getSchema() != null) {
                TypeName type = getType(mediaType.getSchema());
                ParameterSpec param = ParameterSpec.builder(type, "requestBody")
                        .addAnnotation(
                                AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, REQUEST_BODY)).build()
                        )
                        .build();
                builder.addParameter(param);
            }
        }
    }

    private void addResponse(Operation operation, MethodSpec.Builder builder) {
        for (Map.Entry<String, ApiResponse> e : operation.getResponses().entrySet()) {
            if (!"default".equals(e.getKey())) {
                int statusCode = Integer.valueOf(e.getKey());
                if (statusCode >= 200 && statusCode <= 299) {
                    if (e.getValue().getContent() != null) {
                        MediaType mediaType = e.getValue().getContent().get(JSON);
                        if (mediaType != null) {
                            builder.returns(getType(mediaType.getSchema()));
                        }
                    }

                    if (statusCode != 200) {
                        String statusName = HttpStatus.valueOf(statusCode).name();
                        builder.addAnnotation(
                                AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, RESPONSE_STATUS))
                                        .addMember("value", "$T." + statusName, ClassName.get(SPRING_HTTP, HTTP_STATUS))
                                        .build()
                        );
                    }
                }
            }
        }
    }

    private void addMapping(String path, PathItem.HttpMethod method, MethodSpec.Builder builder) {
        String mappingName = pascalCase(method.name().toLowerCase()) + "Mapping";
        builder.addAnnotation(AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, mappingName))
                .addMember("value", "$S", path)
                .build()
        );
    }

    private void addParameter(MethodSpec.Builder builder, Parameter parameter) {
        String inType = "path".equals(parameter.getIn()) ? PATH_VARIABLE : REQUEST_PARAM;

        AnnotationSpec.Builder anno = AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, inType))
                .addMember("name", "$S", parameter.getName())
                .addMember("required", "$L", parameter.getRequired());

        if (parameter.getSchema() != null && parameter.getSchema().getDefault() != null) {
            anno.addMember("defaultValue", "$S", parameter.getSchema().getDefault().toString());
        }

        ParameterSpec param = ParameterSpec.builder(getType(parameter.getSchema()), parameter.getName())
                .addAnnotation(anno.build())
                .build();
        builder.addParameter(param);
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

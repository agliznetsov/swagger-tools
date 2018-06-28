package org.swaggertools.core.targets;

import com.squareup.javapoet.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.ConfigurationProperty;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.HttpStatus;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Map;

import static com.squareup.javapoet.TypeName.VOID;
import static org.swaggertools.core.util.NameUtils.*;

@Slf4j
public class ServerGenerator extends JavaFileGenerator<ServerGenerator.Options> {
    public static final String NAME = "server";

    private static final String SPRING_ANNOTATIONS = "org.springframework.web.bind.annotation";
    private static final ClassName REST_CONTROLLER = ClassName.get(SPRING_ANNOTATIONS, "RestController");
    private static final ClassName REQUEST_BODY = ClassName.get(SPRING_ANNOTATIONS, "RequestBody");
    private static final ClassName REQUEST_PARAM = ClassName.get(SPRING_ANNOTATIONS, "RequestParam");
    private static final ClassName REQUEST_MAPPING = ClassName.get(SPRING_ANNOTATIONS, "RequestMapping");
    private static final ClassName PATH_VARIABLE = ClassName.get(SPRING_ANNOTATIONS, "PathVariable");
    private static final ClassName RESPONSE_STATUS = ClassName.get(SPRING_ANNOTATIONS, "ResponseStatus");
    private static final ClassName HTTP_STATUS = ClassName.get("org.springframework.http", "HttpStatus");
    private static final ClassName MONO = ClassName.get("reactor.core.publisher", "Mono");

    final Map<String, ApiInfo> apis = new HashMap<>();
    final SchemaMapper schemaMapper = new SchemaMapper();

    public ServerGenerator() {
        super(new Options());
    }

    @Override
    public void accept(ApiDefinition apiDefinition) {
        validateConfiguration();
        log.info("Generating server in {}/{}", options.location, options.apiPackage);
        schemaMapper.setModelPackage(options.modelPackage);
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
        JavaFileWriter writer = createWriter(options.location);
        writer.write(JavaFile.builder(options.apiPackage, apiInfo.api.build()).indent(INDENT).build());
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

                String defaultValue = p.getSchema().getDefaultValue();
                if (defaultValue != null) {
                    anno.addMember("defaultValue", "$S", defaultValue);
                }
            }
            ParameterSpec param = ParameterSpec.builder(schemaMapper.getType(p.getSchema(), false), p.getName())
                    .addAnnotation(anno.build())
                    .build();
            builder.addParameter(param);
        });
    }

    private void addResponse(MethodSpec.Builder builder, Operation operationInfo) {
        if (options.reactive) {
            if (operationInfo.getResponseSchema() != null) {
                TypeName type = schemaMapper.getType(operationInfo.getResponseSchema(), false);
                builder.returns(ParameterizedTypeName.get(MONO, type));
            } else {
                builder.returns(ParameterizedTypeName.get(MONO, VOID.box()));
            }
        } else {
            if (operationInfo.getResponseSchema() != null) {
                builder.returns(schemaMapper.getType(operationInfo.getResponseSchema(), false));
            }
        }
        if (operationInfo.getResponseStatus() != null && operationInfo.getResponseStatus() != HttpStatus.OK) {
            String statusName = operationInfo.getResponseStatus().name();
            builder.addAnnotation(
                    AnnotationSpec.builder(RESPONSE_STATUS)
                            .addMember("value", "$T." + statusName, HTTP_STATUS)
                            .build()
            );
        }
    }

    private ApiInfo getApi(String tag) {
        return apis.computeIfAbsent(tag, it -> new ApiInfo(pascalCase(sanitize(it) + options.apiSuffix)));
    }

    @Override
    public String getGroupName() {
        return NAME;
    }

    private static class ApiInfo {
        public final TypeSpec.Builder api;

        public ApiInfo(String name) {
            api = TypeSpec.interfaceBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(REST_CONTROLLER).build());
        }
    }

    @Data
    public static class Options {
        @ConfigurationProperty(description = "Server classes target directory", required = true)
        String location;
        @ConfigurationProperty(description = "Server classes package name", required = true)
        String apiPackage;
        @ConfigurationProperty(description = "Models package name", required = true)
        String modelPackage;
        @ConfigurationProperty(description = "Server classes name suffix", defaultValue = "Api")
        String apiSuffix = "Api";
        @ConfigurationProperty(description = "Generate reactive, non-blocking API", defaultValue = "false")
        boolean reactive = false;
        @ConfigurationProperty(description = "Implemetation dialect [spring/jaxrs]", defaultValue = "spring")
        String dialect = "spring";
    }
}

package org.swaggertools.core.targets.server;

import com.squareup.javapoet.*;
import org.swaggertools.core.model.*;
import org.swaggertools.core.run.JavaFileWriter;

import static com.squareup.javapoet.TypeName.VOID;
import static org.swaggertools.core.util.NameUtils.pascalCase;

public class SpringBuilder extends ServerBuilder {

    private static final String SPRING_ANNOTATIONS = "org.springframework.web.bind.annotation";
    private static final ClassName REST_CONTROLLER = ClassName.get(SPRING_ANNOTATIONS, "RestController");
    private static final ClassName REQUEST_BODY = ClassName.get(SPRING_ANNOTATIONS, "RequestBody");
    private static final ClassName REQUEST_PARAM = ClassName.get(SPRING_ANNOTATIONS, "RequestParam");
    private static final ClassName REQUEST_MAPPING = ClassName.get(SPRING_ANNOTATIONS, "RequestMapping");
    private static final ClassName PATH_VARIABLE = ClassName.get(SPRING_ANNOTATIONS, "PathVariable");
    private static final ClassName RESPONSE_STATUS = ClassName.get(SPRING_ANNOTATIONS, "ResponseStatus");
    private static final ClassName HTTP_STATUS = ClassName.get("org.springframework.http", "HttpStatus");
    private static final ClassName MONO = ClassName.get("reactor.core.publisher", "Mono");

    public SpringBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ServerOptions options) {
        super(apiDefinition, writer, options);
    }

    @Override
    protected void annotateClass(TypeSpec.Builder builder) {
        builder.addAnnotation(AnnotationSpec.builder(REST_CONTROLLER).build());
        if (apiDefinition.getBasePath() != null) {
            builder.addAnnotation(AnnotationSpec.builder(REQUEST_MAPPING)
                    .addMember("value", "$S", apiDefinition.getBasePath())
                    .build()
            );
        }
    }

    @Override
    protected void annotateMethod(MethodSpec.Builder builder, Operation operation) {
        String mappingName = pascalCase(operation.getMethod().name().toLowerCase()) + "Mapping";
        AnnotationSpec.Builder ab = AnnotationSpec.builder(ClassName.get(SPRING_ANNOTATIONS, mappingName));
        ab.addMember("value", "$S", operation.getPath());
        if (operation.getResponseMediaType() != null) {
            ab.addMember("produces", "$S", operation.getResponseMediaType());
        }
        builder.addAnnotation(ab.build());
    }

    @Override
    protected void annotateParameter(ParameterSpec.Builder paramBuilder, Parameter parameter) {
        super.annotateParameter(paramBuilder, parameter);
        AnnotationSpec.Builder anno;
        if (parameter.getKind() == ParameterKind.BODY) {
            anno = AnnotationSpec.builder(REQUEST_BODY);
        } else {
            ClassName inType = parameter.getKind() == ParameterKind.PATH ? PATH_VARIABLE : REQUEST_PARAM;
            anno = AnnotationSpec.builder(inType)
                    .addMember("name", "$S", parameter.getName())
                    .addMember("required", "$L", parameter.isRequired());

            String defaultValue = parameter.getSchema().getDefaultValue();
            if (defaultValue != null) {
                anno.addMember("defaultValue", "$S", defaultValue);
            }
        }
        paramBuilder.addAnnotation(anno.build());
    }

    @Override
    protected void addResponse(MethodSpec.Builder builder, Operation operationInfo) {
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
}

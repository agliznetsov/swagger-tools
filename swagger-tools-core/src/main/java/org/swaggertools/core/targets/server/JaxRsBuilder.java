package org.swaggertools.core.targets.server;

import com.squareup.javapoet.*;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;

public class JaxRsBuilder extends ServerBuilder {

    private static final String JAX_RS = "javax.ws.rs";
    private static final ClassName PATH = ClassName.get(JAX_RS, "Path");
    private static final ClassName CONSUMES = ClassName.get(JAX_RS, "Consumes");
    private static final ClassName PRODUCES = ClassName.get(JAX_RS, "Produces");
    private static final ClassName REQUEST_PARAM = ClassName.get(JAX_RS, "QueryParam");
    private static final ClassName PATH_VARIABLE = ClassName.get(JAX_RS, "PathParam");

    public JaxRsBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ServerOptions options) {
        super(apiDefinition, writer, options);
    }

    @Override
    protected void annotateClass(TypeSpec.Builder builder) {
        if (apiDefinition.getBasePath() != null) {
            builder.addAnnotation(AnnotationSpec.builder(PATH)
                    .addMember("value", "$S", apiDefinition.getBasePath())
                    .build()
            );
            builder.addAnnotation(AnnotationSpec.builder(CONSUMES).addMember("value", "$S", "application/json").build());
            builder.addAnnotation(AnnotationSpec.builder(PRODUCES).addMember("value", "$S", "application/json").build());
        }
    }

    @Override
    protected void annotateMethod(MethodSpec.Builder builder, Operation operation) {
        builder.addAnnotation(AnnotationSpec.builder(ClassName.get(JAX_RS, operation.getMethod().name())).build());
        builder.addAnnotation(AnnotationSpec.builder(PATH)
                .addMember("value", "$S", operation.getPath())
                .build()
        );
        if (operation.getResponseMediaType() != null) {
            builder.addAnnotation(AnnotationSpec.builder(PRODUCES)
                    .addMember("value", "$S", operation.getResponseMediaType())
                    .build()
            );
        }
    }

    @Override
    protected void annotateParameter(ParameterSpec.Builder paramBuilder, Parameter parameter) {
        super.annotateParameter(paramBuilder, parameter);
        if (parameter.getKind() != ParameterKind.BODY) {
            ClassName inType = parameter.getKind() == ParameterKind.PATH ? PATH_VARIABLE : REQUEST_PARAM;
            AnnotationSpec.Builder anno = AnnotationSpec.builder(inType)
                    .addMember("value", "$S", parameter.getName());
            paramBuilder.addAnnotation(anno.build());
        }
    }

    @Override
    protected void addResponse(MethodSpec.Builder builder, Operation operationInfo) {
        if (options.reactive) {
            throw new IllegalArgumentException("JaxRS dialect does not support reactive API");
        } else {
            if (operationInfo.getResponseSchema() != null) {
                builder.returns(schemaMapper.getType(operationInfo.getResponseSchema(), false));
            }
        }
    }
}

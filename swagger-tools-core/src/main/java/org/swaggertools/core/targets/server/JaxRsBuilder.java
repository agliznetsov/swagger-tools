package org.swaggertools.core.targets.server;

import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class JaxRsBuilder extends ServerBuilder {

    private static final String JAX_RS = "jakarta.ws.rs";
    private static final String JAX_RS_CORE = "jakarta.ws.rs.core";
    private static final String JAX_SSE = "jakarta.ws.rs.sse";
    private static final ClassName PATH = ClassName.get(JAX_RS, "Path");
    private static final ClassName CONSUMES = ClassName.get(JAX_RS, "Consumes");
    private static final ClassName PRODUCES = ClassName.get(JAX_RS, "Produces");
    private static final ClassName REQUEST_PARAM = ClassName.get(JAX_RS, "QueryParam");
    private static final ClassName HEADER_PARAM = ClassName.get(JAX_RS, "HeaderParam");
    private static final ClassName PATH_VARIABLE = ClassName.get(JAX_RS, "PathParam");
    private static final ClassName CONTEXT = ClassName.get(JAX_RS_CORE, "Context");
    private static final ClassName SSE_EVENT_SINK = ClassName.get(JAX_SSE, "SseEventSink");
    private static final ClassName SSE = ClassName.get(JAX_SSE, "Sse");

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
//             disabled for now, because client code does not support custom media type yet
//            builder.addAnnotation(AnnotationSpec.builder(CONSUMES).addMember("value", "$S", "application/json").build());
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
        if (operation.getRequestMediaType() != null) {
            builder.addAnnotation(AnnotationSpec.builder(CONSUMES)
                    .addMember("value", "$S", operation.getRequestMediaType())
                    .build()
            );
        }
    }

    @Override
    protected void annotateParameter(ParameterSpec.Builder paramBuilder, Parameter parameter) {
        super.annotateParameter(paramBuilder, parameter);
        if (parameter.getKind() != ParameterKind.BODY) {
            ClassName inType = getParameterAnnotationClass(parameter.getKind());
            AnnotationSpec.Builder anno = AnnotationSpec.builder(inType)
                    .addMember("value", "$S", parameter.getName());
            paramBuilder.addAnnotation(anno.build());
        }
    }

    private ClassName getParameterAnnotationClass(ParameterKind kind) {
        if (kind ==  ParameterKind.PATH) {
            return PATH_VARIABLE;
        } else if (kind == ParameterKind.QUERY) {
            return REQUEST_PARAM;
        } else if (kind == ParameterKind.HEADER) {
            return HEADER_PARAM;
        } else {
            throw new IllegalArgumentException("Unknown parameter kind: " + kind);
        }
    }

    @Override
    protected void addResponse(MethodSpec.Builder builder, Operation operationInfo) {
        if (options.reactive) {
            throw new IllegalArgumentException("JaxRS dialect does not support reactive API");
        } else {
            if (EVENT_STREAM.equals(operationInfo.getResponseMediaType())) {
                builder.returns(TypeName.VOID.unbox());
            } else {
                if (operationInfo.getResponseSchema() != null) {
                    builder.returns(schemaMapper.getType(operationInfo.getResponseSchema(), false));
                }
            }
        }
    }

    @Override
    protected void addParameters(MethodSpec.Builder builder, Operation operationInfo) {
        super.addParameters(builder, operationInfo);
        if (EVENT_STREAM.equals(operationInfo.getResponseMediaType())) {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(SSE_EVENT_SINK, "sseEventSink");
            paramBuilder.addAnnotation(AnnotationSpec.builder(CONTEXT).build());
            builder.addParameter(paramBuilder.build());

            paramBuilder = ParameterSpec.builder(SSE, "sse");
            paramBuilder.addAnnotation(AnnotationSpec.builder(CONTEXT).build());
            builder.addParameter(paramBuilder.build());
        }
    }
}

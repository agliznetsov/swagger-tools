package org.swaggertools.core.targets.client;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.swaggertools.core.model.*;
import org.swaggertools.core.run.JavaFileWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class RestTemplateBuilder extends ClientBuilder {
    private static final ClassName REST_TEMPLATE = ClassName.get("org.springframework.web.client", "RestTemplate");
    private static final ClassName TYPE_REF = ClassName.get("org.springframework.core", "ParameterizedTypeReference");
    private static final ClassName RESPONSE_ENTITY = ClassName.get("org.springframework.http", "ResponseEntity");
    private static final ClassName RESPONSE_EXTRACTOR = ClassName.get("org.springframework.web.client", "ResponseExtractor");

    public RestTemplateBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientOptions options) {
        super(apiDefinition, writer, options);
    }

    @Override
    protected String getBaseClassTemplate() {
        return "RestTemplate";
    }

    @Override
    protected ClassName getClientClassName() {
        return REST_TEMPLATE;
    }

    @Override
    protected TypeName getRequestBuilderClassName() {
        return ParameterizedTypeName.get(ClassName.get(Consumer.class), ClassName.get("org.springframework.http.RequestEntity", "BodyBuilder"));
    }

    @Override
    protected void addMethodResponse(MethodSpec.Builder builder, Operation operation) {
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
        } else {
            super.addMethodResponse(builder, operation);
        }
    }

    @Override
    protected void addMethodBody(MethodSpec.Builder builder, Operation operation) {
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            executeApi(builder, operation);
        } else {
            createRequestTypeRef(builder, operation);
            createResponseTypeRef(builder, operation);
            invokeApi(builder, operation);
        }
    }

    @Override
    protected void addMethodParameters(MethodSpec.Builder builder, Operation operation) {
        super.addMethodParameters(builder, operation);
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            ParameterSpec param = ParameterSpec.builder(RESPONSE_EXTRACTOR, "responseExtractor").build();
            builder.addParameter(param);
        }
    }

    protected void createRequestTypeRef(MethodSpec.Builder builder, Operation operation) {
        Optional<Parameter> body = getBodyParameter(operation);
        body.ifPresent(parameter -> createTypeRef(builder, "requestType", parameter.getSchema()));
    }

    private void createResponseTypeRef(MethodSpec.Builder builder, Operation operation) {
        createTypeRef(builder, "responseType", operation.getResponseSchema());
    }

    protected void createTypeRef(MethodSpec.Builder builder, String name, Schema schema) {
        if (schema != null) {
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, schemaMapper.getType(schema, false));
            builder.addStatement("$T " + name + " = new $T(){}", typeRef, typeRef);
        } else {
            builder.addStatement("$T " + name + " = VOID", TYPE_REF);
        }
    }

    @Override
    protected TypeName getReturnType(Operation operation) {
        TypeName type = super.getReturnType(operation);
        if (operation.isResponseEntity()) {
            return ParameterizedTypeName.get(RESPONSE_ENTITY, type);
        } else {
            return type;
        }
    }

    private void invokeApi(MethodSpec.Builder builder, Operation operation) {
        String format = "";
        List<Object> args = new LinkedList<>();
        Optional<Parameter> body = getBodyParameter(operation);
        String requestType = body.isPresent() ? "requestType" : "null";
        if (operation.getResponseSchema() != null) {
            TypeName typeRef = ParameterizedTypeName.get(RESPONSE_ENTITY, schemaMapper.getType(operation.getResponseSchema(), false));
            format = "$T response = ";
            args.add(typeRef);
        }
        format += "invokeAPI($S, $S, $L, $L, $L, $L, " + requestType + ", responseType)";
        args.add(operation.getPath());
        args.add(operation.getMethod().name());
        args.add(createUrlVariables(operation));
        args.add(createQueryParameters(operation));
        args.add(createHeaderParameters(operation));
        args.add(body.isPresent() ? body.get().getName() : "null");

        builder.addStatement(format, args.toArray());

        if (operation.getResponseSchema() != null) {
            if (operation.isResponseEntity()) {
                builder.addStatement("return response");
            } else {
                builder.addStatement("return response.getBody()");
            }
        }
    }

    private Optional<Parameter> getBodyParameter(Operation operation) {
        return operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
    }

    private void executeApi(MethodSpec.Builder builder, Operation operation) {
        String format = "executeAPI($S, $S, $L, $L, $L, null, responseExtractor)";
        List<Object> args = new LinkedList<>();
        args.add(operation.getPath());
        args.add(operation.getMethod().name());
        args.add(createUrlVariables(operation));
        args.add(createQueryParameters( operation));
        args.add(createHeaderParameters(operation));
        builder.addStatement(format, args.toArray());
    }

}

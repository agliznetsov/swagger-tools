package org.swaggertools.core.targets.client;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.swaggertools.core.model.*;
import org.swaggertools.core.run.JavaFileWriter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.squareup.javapoet.TypeName.VOID;

public class WebClientBuilder extends ClientBuilder {
    private static final ClassName TYPE_REF = ClassName.get("org.springframework.core", "ParameterizedTypeReference");
    private static final ClassName MONO = ClassName.get("reactor.core.publisher", "Mono");
    private static final ClassName FLUX = ClassName.get("reactor.core.publisher", "Flux");
    private static final ClassName WEB_CLIENT = ClassName.get("org.springframework.web.reactive.function.client", "WebClient");
    private static final ClassName SERVER_SENT_EVENT = ClassName.get("org.springframework.http.codec", "ServerSentEvent");
    private static final ClassName CLIENT_RESPONSE = ClassName.get("org.springframework.web.reactive.function.client", "ClientResponse");

    public WebClientBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientOptions options) {
        super(apiDefinition, writer, options);
    }

    @Override
    protected String getBaseClassTemplate() {
        return "WebClient";
    }

    @Override
    protected ClassName getClientClassName() {
        return WEB_CLIENT;
    }

    @Override
    protected TypeName getRequestBuilderClassName() {
        return ParameterizedTypeName.get(ClassName.get(Consumer.class), ClassName.get("org.springframework.web.reactive.function.client.WebClient", "RequestBodySpec"));
    }

    @Override
    protected void addMethodBody(MethodSpec.Builder builder, Operation operation) {
        createTypeRefs(builder, operation);
        invokeApi(builder, operation);

    }

    protected void createTypeRefs(MethodSpec.Builder builder, Operation operation) {
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, SERVER_SENT_EVENT);
            builder.addStatement("$T responseType = new $T(){}", typeRef, typeRef);
        } else {
            Optional<Parameter> body = getBodyParameter(operation);
            body.ifPresent(parameter -> createTypeRef(builder, "requestType", parameter.getSchema()));
            createTypeRef(builder, "responseType", operation.getResponseSchema());
        }
    }

    private void createTypeRef(MethodSpec.Builder builder, String name, Schema schema) {
        if (schema != null) {
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, schemaMapper.getType(schema, false));
            builder.addStatement("$T " + name + " = new $T(){}", typeRef, typeRef);
        } else {
            builder.addStatement("$T " + name + " = VOID", TYPE_REF);
        }
    }

    private void invokeApi(MethodSpec.Builder builder, Operation operation) {
        List<Object> args = new LinkedList<>();
        Optional<Parameter> body = getBodyParameter(operation);
        String requestType = body.isPresent() ? "requestType" : null;
        String format = "return invokeAPI($S, $S, $L, $L, $L, $L, " + requestType + ")";
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            format += ".flatMapMany(e -> e.bodyToFlux(responseType))";
        } else {
            if (!operation.isResponseEntity()) {
                format += ".flatMap(e -> mapResponse(e, responseType))";
            }
        }
        args.add(operation.getPath());
        args.add(operation.getMethod().name());
        args.add(createUrlVariables(operation));
        args.add(createQueryParameters(operation));
        args.add(createHeaderParameters(operation));
        args.add(body.isPresent() ? body.get().getName() : "null");
        builder.addStatement(format, args.toArray());
    }

    private Optional<Parameter> getBodyParameter(Operation operation) {
        return operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
    }

    @Override
    protected void addMethodResponse(MethodSpec.Builder builder, Operation operation) {
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            builder.returns(ParameterizedTypeName.get(FLUX, SERVER_SENT_EVENT));
        } else {
            if (operation.isResponseEntity()) {
                builder.returns(ParameterizedTypeName.get(MONO, CLIENT_RESPONSE));
            } else {
                if (operation.getResponseSchema() != null) {
                    TypeName type = schemaMapper.getType(operation.getResponseSchema(), false);
                    builder.returns(ParameterizedTypeName.get(MONO, type));
                } else {
                    builder.returns(ParameterizedTypeName.get(MONO, VOID.box()));
                }
            }
        }
    }
}

package org.swaggertools.core.targets.client;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
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
        createTypeRef(builder, operation);
        invokeApi(builder, operation);

    }

    protected void createTypeRef(MethodSpec.Builder builder, Operation operation) {
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, SERVER_SENT_EVENT);
            builder.addStatement("$T typeRef = new $T(){}", typeRef, typeRef);
        } else {
            if (operation.getResponseSchema() != null) {
                TypeName typeRef = ParameterizedTypeName
                        .get(TYPE_REF, schemaMapper.getType(operation.getResponseSchema(), false));
                builder.addStatement("$T typeRef = new $T(){}", typeRef, typeRef);
            } else {
                builder.addStatement("$T typeRef = VOID", TYPE_REF);
            }
        }
    }

    private void invokeApi(MethodSpec.Builder builder, Operation operation) {
        List<Object> args = new LinkedList<>();
        String format = "return invokeAPI($S, $S, $L, $L, $L)";
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            format += ".bodyToFlux(typeRef)";
        } else {
            format += ".bodyToMono(typeRef)";
        }
        args.add(operation.getPath());
        args.add(operation.getMethod().name());
        args.add(createUrlVariables(operation));
        args.add(createQueryParameters(operation));
        Optional<Parameter> body = operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
        args.add(body.isPresent() ? body.get().getName() : "null");
        builder.addStatement(format, args.toArray());
    }

    @Override
    protected void addMethodResponse(MethodSpec.Builder builder, Operation operation) {
        if (EVENT_STREAM.equals(operation.getResponseMediaType())) {
            builder.returns(ParameterizedTypeName.get(FLUX, SERVER_SENT_EVENT));
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

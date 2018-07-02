package org.swaggertools.core.targets.client;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import lombok.SneakyThrows;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.util.StreamUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class HttpClientBuilder extends ClientBuilder {
    private static final ClassName TYPE_REF = ClassName.get("com.fasterxml.jackson.core.type", "TypeReference");
    private static final ClassName HTTP_CLIENT = ClassName.get("org.apache.http.impl.client", "CloseableHttpClient");

    public HttpClientBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientOptions options) {
        super(apiDefinition, writer, options);
    }

    @Override
    protected String getBaseClassTemplate() {
        return "HttpClient";
    }

    @Override
    protected ClassName getClientClassName() {
        return HTTP_CLIENT;
    }

    @Override
    protected TypeName getRequestBuilderClassName() {
        return ParameterizedTypeName.get(ClassName.get(Consumer.class), ClassName.get("org.apache.http.client.methods", "RequestBuilder"));
    }

    @Override
    @SneakyThrows
    protected void writeBaseClient(JavaFileWriter writer) {
        super.writeBaseClient(writer);
        String body = StreamUtils.copyToString(getClass().getResourceAsStream("/client/HttpStatusException.java"));
        body = body.replace("{{package}}", options.clientPackage);
        writer.write(options.clientPackage, "HttpStatusException", body);
    }

    @Override
    protected void addMethodBody(MethodSpec.Builder builder, Operation operation) {
        createTypeRef(builder, operation);
        invokeApi(builder, operation);

    }

    protected void createTypeRef(MethodSpec.Builder builder, Operation operation) {
        if (operation.getResponseSchema() != null) {
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, schemaMapper.getType(operation.getResponseSchema(), false));
            builder.addStatement("$T typeRef = new $T(){}", typeRef, typeRef);
        } else {
            builder.addStatement("$T typeRef = VOID", TYPE_REF);
        }
    }

    private void invokeApi(MethodSpec.Builder builder, Operation operation) {
        List<Object> args = new LinkedList<>();
        String ret = operation.getResponseSchema() != null ? "return " : "";
        String format = ret + "invokeAPI($S, $S, $L, $L, $L, typeRef)";
        args.add(operation.getPath());
        args.add(operation.getMethod().name());
        args.add(createUrlVariables(operation));
        args.add(createQueryParameters(operation));
        Optional<Parameter> body = operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
        args.add(body.isPresent() ? body.get().getName() : "null");
        builder.addStatement(format, args.toArray());
    }

}

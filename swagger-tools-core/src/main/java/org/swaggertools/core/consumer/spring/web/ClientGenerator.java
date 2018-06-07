package org.swaggertools.core.consumer.spring.web;

import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.swaggertools.core.consumer.JavaGenerator;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.util.StreamUtils;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.function.Consumer;

import static org.swaggertools.core.util.NameUtils.*;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class ClientGenerator extends JavaGenerator implements Consumer<ApiDefinition> {
    public static final ClassName REST_TEMPLATE = ClassName.get("org.springframework.web.client", "RestTemplate");
    public static final ClassName MULTI_MAP = ClassName.get("org.springframework.util", "MultiValueMap");
    public static final ClassName TYPE_REF = ClassName.get("org.springframework.core", "ParameterizedTypeReference");
    public static final ClassName RESPONSE_ENTITY = ClassName.get("org.springframework.http", "ResponseEntity");
    public static final ClassName HTTP_METHOD = ClassName.get("org.springframework.http", "HttpMethod");

    @Getter
    @Setter
    String clientPackageName;

    @Getter
    @Setter
    String clientSuffix = "Client";

    final Map<String, ClientInfo> apis = new HashMap<>();

    @Override
    public void accept(ApiDefinition apiDefinition) {
        super.accept(apiDefinition);
        notNull(modelPackageName, "modelPackageName is not set");
        notNull(clientPackageName, "clientPackageName is not set");
        apiDefinition.getOperations().forEach(this::processOperation);
        writeBaseClient();
        apis.forEach((k, v) -> writer.write(JavaFile.builder(clientPackageName, v.client.build()).indent(indent).build()));
    }

    @SneakyThrows
    private void writeBaseClient() {
        String body = StreamUtils.copyToString(getClass().getResourceAsStream("/client/RestTemplateClient.java"));
        body = body.replace("{{package}}", clientPackageName);
        writer.write(clientPackageName, "RestTemplateClient", body);
    }

    private void processOperation(Operation operation) {
        String methodName = camelCase(sanitize(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);

        addMethodParameters(builder, operation);
        addMethodResponse(builder, operation);
        createQueryParameters(builder, operation);
        createTypeRef(builder, operation);
        invokeApi(builder, operation);

        getClient(operation.getTag()).client.addMethod(builder.build());
    }

    private void createQueryParameters(MethodSpec.Builder builder, Operation operation) {
        List<String> names = new LinkedList<>();
        List<Object> args = new LinkedList<>();
        args.add(MULTI_MAP);
        operation.getParameters().forEach(p -> {
            if (p.getKind() != ParameterKind.BODY) {
                names.add("$S");
                names.add("$L");
                args.add(p.getName());
                args.add(p.getName());
            }
        });
        builder.addStatement("$T parameters = createQueryParameters(" + String.join(", ", names) + ")", args.toArray());
    }

    private void createTypeRef(MethodSpec.Builder builder, Operation operation) {
        if (operation.getResponseSchema() != null) {
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, getType(operation.getResponseSchema()));
            builder.addStatement("$T typeRef = new $T(){}", typeRef, typeRef);
        } else {
            builder.addStatement("$T typeRef = VOID", TYPE_REF);
        }
    }

    private void invokeApi(MethodSpec.Builder builder, Operation operation) {
        String format = "";
        List<Object> args = new LinkedList<>();
        if (operation.getResponseSchema() != null) {
            TypeName typeRef = ParameterizedTypeName.get(RESPONSE_ENTITY, getType(operation.getResponseSchema()));
            format = "$T response = ";
            args.add(typeRef);
        }
        format += "invokeAPI($S, $T.$L, parameters, $L, typeRef)";
        args.add(operation.getPath());
        args.add(HTTP_METHOD);
        args.add(operation.getMethod().name());
        Optional<Parameter> body = operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
        args.add(body.isPresent() ? body.get().getName() : "null");
        builder.addStatement(format, args.toArray());
        if (operation.getResponseSchema() != null) {
            builder.addStatement("return response.getBody()");
        }
    }

    private void addMethodParameters(MethodSpec.Builder builder, Operation operation) {
        operation.getParameters().forEach(p -> {
            ParameterSpec param = ParameterSpec.builder(getType(p.getSchema()), p.getName()).build();
            builder.addParameter(param);
        });
    }

    private void addMethodResponse(MethodSpec.Builder builder, Operation operation) {
        if (operation.getResponseSchema() != null) {
            builder.returns(getType(operation.getResponseSchema()));
        }
    }

    private ClientInfo getClient(String tag) {
        return apis.computeIfAbsent(tag, it -> new ClientInfo(pascalCase(sanitize(it) + clientSuffix)));
    }

    private class ClientInfo {
        public final TypeSpec.Builder client;

        public ClientInfo(String name) {
            client = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ClassName.get(clientPackageName, "RestTemplateClient"))
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(REST_TEMPLATE, "restTemplate")
                            .addStatement("super($N)", "restTemplate")
                            .build()
                    );
        }
    }

}

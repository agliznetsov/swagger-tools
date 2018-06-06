package org.swaggertools.core.consumer.spring.web;

import com.squareup.javapoet.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.swaggertools.core.consumer.ApiGenerator;
import org.swaggertools.core.util.StreamUtils;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.swaggertools.core.consumer.NameUtils.*;
import static org.swaggertools.core.util.AssertUtils.notNull;

public class ClientGenerator extends ApiGenerator implements Consumer<OpenAPI> {
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
    public void accept(OpenAPI openAPI) {
        super.accept(openAPI);
        notNull(modelPackageName, "modelPackageName is not set");
        notNull(clientPackageName, "clientPackageName is not set");
        openAPI.getPaths().forEach(this::processPath);
        writeBaseClient();
        apis.forEach((k, v) -> writer.write(JavaFile.builder(clientPackageName, v.client.build()).indent(indent).build()));
    }

    @SneakyThrows
    private void writeBaseClient() {
        String body = StreamUtils.copyToString(getClass().getResourceAsStream("/client/RestTemplateClient.java"));
        body = body.replace("{{package}}", clientPackageName);
        writer.write(clientPackageName, "RestTemplateClient", body);
    }

    private void processPath(String path, PathItem pathItem) {
        pathItem.readOperationsMap().forEach((k, v) -> processOperation(path, k, v));
    }

    private void processOperation(String path, PathItem.HttpMethod method, Operation operation) {
        ApiGenerator.OperationInfo operationInfo = getOperationInfo(operation);
        if (operationInfo != null) {
            operationInfo.path = path;
            operationInfo.method = method.name().toUpperCase();

            String methodName = camelCase(sanitize(operation.getOperationId()));
            MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);

            addMethodParameters(builder, operationInfo);
            addMethodResponse(builder, operationInfo);

            createQueryParameters(builder, operationInfo);
            createTypeRef(builder, operationInfo);
            invokeApi(builder, operationInfo);

            getClient(operationInfo.tag).client.addMethod(builder.build());
        }
    }

    private void createQueryParameters(MethodSpec.Builder builder, OperationInfo operationInfo) {
        List<String> names = new LinkedList<>();
        List<Object> args = new LinkedList<>();
        args.add(MULTI_MAP);
        for (ParameterInfo p : operationInfo.parameters) {
            if (p.kind != ParameterKind.BODY) {
                names.add("$S");
                names.add("$L");
                args.add(p.name);
                args.add(p.name);
            }
        }
        builder.addStatement("$T parameters = createQueryParameters(" + String.join(", ", names) + ")", args.toArray());
    }

    private void createTypeRef(MethodSpec.Builder builder, OperationInfo operationInfo) {
        if (operationInfo.responseSchema != null) {
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, getType(operationInfo.responseSchema));
            builder.addStatement("$T typeRef = new $T(){}", typeRef, typeRef);
        } else {
            builder.addStatement("$T typeRef = VOID", TYPE_REF);
        }
    }

    private void invokeApi(MethodSpec.Builder builder, OperationInfo operationInfo) {
        String format = "";
        List<Object> args = new LinkedList<>();
        if (operationInfo.responseSchema != null) {
            TypeName typeRef = ParameterizedTypeName.get(RESPONSE_ENTITY, getType(operationInfo.responseSchema));
            format = "$T response = ";
            args.add(typeRef);
        }
        format += "invokeAPI($S, $T.$L, parameters, $L, typeRef)";
        args.add(operationInfo.path);
        args.add(HTTP_METHOD);
        args.add(operationInfo.method);
        long bodyCount = operationInfo.parameters.stream().filter(it -> it.kind == ParameterKind.BODY).count();
        args.add(bodyCount > 0 ? "requestBody" : "null");
        builder.addStatement(format, args.toArray());
        if (operationInfo.responseSchema != null) {
            builder.addStatement("return response.getBody()");
        }
    }

    private void addMethodParameters(MethodSpec.Builder builder, ApiGenerator.OperationInfo operationInfo) {
        operationInfo.parameters.forEach(p -> {
            ParameterSpec param = ParameterSpec.builder(getType(p.schema), p.name).build();
            builder.addParameter(param);
        });
    }

    private void addMethodResponse(MethodSpec.Builder builder, ApiGenerator.OperationInfo operationInfo) {
        if (operationInfo.responseSchema != null) {
            builder.returns(getType(operationInfo.responseSchema));
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

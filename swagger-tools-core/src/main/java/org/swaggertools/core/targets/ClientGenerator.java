package org.swaggertools.core.targets;

import com.squareup.javapoet.*;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.ConfigurationProperty;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.util.NameUtils;
import org.swaggertools.core.util.StreamUtils;

import javax.lang.model.element.Modifier;
import java.util.*;

import static com.squareup.javapoet.TypeName.VOID;
import static org.swaggertools.core.util.JavaUtils.MAP;
import static org.swaggertools.core.util.JavaUtils.STRING;
import static org.swaggertools.core.util.NameUtils.*;

@Slf4j
public class ClientGenerator extends JavaFileGenerator<ClientGenerator.Options> {
    public static final String NAME = "client";

    static final ClassName REST_TEMPLATE = ClassName.get("org.springframework.web.client", "RestTemplate");
    static final ClassName MULTI_MAP = ClassName.get("org.springframework.util", "MultiValueMap");
    static final TypeName STRING_MULTI_MAP = ParameterizedTypeName.get(MULTI_MAP, STRING, STRING);
    static final ClassName TYPE_REF = ClassName.get("org.springframework.core", "ParameterizedTypeReference");
    static final ClassName RESPONSE_ENTITY = ClassName.get("org.springframework.http", "ResponseEntity");
    static final ClassName HTTP_METHOD = ClassName.get("org.springframework.http", "HttpMethod");

    static final ClassName MONO = ClassName.get("reactor.core.publisher", "Mono");
    static final ClassName WEB_CLIENT = ClassName.get("org.springframework.web.reactive.function.client", "WebClient");

    final Map<String, ClientInfo> apis = new HashMap<>();
    final SchemaMapper schemaMapper = new SchemaMapper();

    public ClientGenerator() {
        super(new Options());
    }

    @Override
    public void accept(ApiDefinition apiDefinition) {
        validateConfiguration();
        log.info("Generating client in {}/{}", options.location, options.clientPackage);
        schemaMapper.setModelPackage(options.modelPackage);
        apiDefinition.getOperations().forEach(this::processOperation);
        JavaFileWriter writer = createWriter(options.location);
        writeBaseClient(writer);
        apis.forEach((k, v) -> writer.write(JavaFile.builder(options.clientPackage, v.client.build()).indent(indent).build()));
    }

    @SneakyThrows
    private void writeBaseClient(JavaFileWriter writer) {
        String className = getBaseClassName();
        String body = StreamUtils.copyToString(getClass().getResourceAsStream("/client/" + className + ".java"));
        body = body.replace("{{package}}", options.clientPackage);
        writer.write(options.clientPackage, className, body);
    }

    private String getBaseClassName() {
        switch (options.dialect) {
            case RestTemplate:
                return "RestTemplateClient";
            case WebClient:
                return "RestWebClient";
            default:
                throw new IllegalArgumentException("Unknown dialect: " + options.dialect);
        }
    }

    private ClassName getClientType() {
        switch (options.dialect) {
            case RestTemplate:
                return REST_TEMPLATE;
            case WebClient:
                return WEB_CLIENT;
            default:
                throw new IllegalArgumentException("Unknown dialect: " + options.dialect);
        }
    }

    private void processOperation(Operation operation) {
        String methodName = camelCase(sanitize(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);

        addMethodParameters(builder, operation);
        addMethodResponse(builder, operation);
        createUrlVariables(builder, operation);
        createQueryParameters(builder, operation);
        createTypeRef(builder, operation);
        invokeApi(builder, operation);

        getClient(operation.getTag()).client.addMethod(builder.build());
    }

    private void createUrlVariables(MethodSpec.Builder builder, Operation operation) {
        List<String> names = new LinkedList<>();
        List<Object> args = new LinkedList<>();
        args.add(MAP);
        operation.getParameters().forEach(p -> {
            if (p.getKind() == ParameterKind.PATH) {
                names.add("$S");
                names.add("$L");
                args.add(p.getName());
                args.add(p.getName());
            }
        });
        builder.addStatement("$T urlVariables = createUrlVariables(" + String.join(", ", names) + ")", args.toArray());
    }

    private void createQueryParameters(MethodSpec.Builder builder, Operation operation) {
        List<String> names = new LinkedList<>();
        List<Object> args = new LinkedList<>();
        args.add(MULTI_MAP);
        operation.getParameters().forEach(p -> {
            if (p.getKind() == ParameterKind.QUERY) {
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
            TypeName typeRef = ParameterizedTypeName.get(TYPE_REF, schemaMapper.getType(operation.getResponseSchema(), false));
            builder.addStatement("$T typeRef = new $T(){}", typeRef, typeRef);
        } else {
            builder.addStatement("$T typeRef = VOID", TYPE_REF);
        }
    }

    private void invokeApi(MethodSpec.Builder builder, Operation operation) {
        if (options.getDialect() == ClientDialect.RestTemplate) {
            invokeRestTemplate(builder, operation);
        } else if (options.getDialect() == ClientDialect.WebClient) {
            invokeWebClient(builder, operation);
        } else {
            throw new IllegalArgumentException("Unknown dialect: " + options.getDialect());
        }
    }

    private void invokeWebClient(MethodSpec.Builder builder, Operation operation) {
        List<Object> args = new LinkedList<>();
        String format = "return invokeAPI($S, $T.$L, urlVariables, parameters, $L).bodyToMono(typeRef)";
        args.add(operation.getPath());
        args.add(HTTP_METHOD);
        args.add(operation.getMethod().name());
        Optional<Parameter> body = operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
        args.add(body.isPresent() ? body.get().getName() : "null");
        builder.addStatement(format, args.toArray());
    }

    private void invokeRestTemplate(MethodSpec.Builder builder, Operation operation) {
        String format = "";
        List<Object> args = new LinkedList<>();
        if (operation.getResponseSchema() != null) {
            TypeName typeRef = ParameterizedTypeName.get(RESPONSE_ENTITY, schemaMapper.getType(operation.getResponseSchema(), false));
            format = "$T response = ";
            args.add(typeRef);
        }
        format += "invokeAPI($S, $T.$L, urlVariables, parameters, $L, typeRef)";
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
            ParameterSpec param = ParameterSpec.builder(schemaMapper.getType(p.getSchema(), false), p.getName()).build();
            builder.addParameter(param);
        });
    }

    private void addMethodResponse(MethodSpec.Builder builder, Operation operation) {
        if (options.dialect == ClientDialect.WebClient) {
            if (operation.getResponseSchema() != null) {
                TypeName type = schemaMapper.getType(operation.getResponseSchema(), false);
                builder.returns(ParameterizedTypeName.get(MONO, type));
            } else {
                builder.returns(ParameterizedTypeName.get(MONO, VOID.box()));
            }
        } else {
            if (operation.getResponseSchema() != null) {
                builder.returns(schemaMapper.getType(operation.getResponseSchema(), false));
            }
        }
    }

    private ClientInfo getClient(String tag) {
        return apis.computeIfAbsent(tag, it -> new ClientInfo(pascalCase(sanitize(it) + options.clientSuffix)));
    }

    @Override
    public String getGroupName() {
        return NAME;
    }

    private class ClientInfo {
        public final TypeSpec.Builder client;

        public ClientInfo(String name) {
            ClassName clientClass = getClientType();
            String clientName = NameUtils.camelCase(clientClass.simpleName());
            client = TypeSpec.classBuilder(name)
                    .addModifiers(Modifier.PUBLIC)
                    .superclass(ClassName.get(options.clientPackage, getBaseClassName()))
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(clientClass, clientName)
                            .addStatement("super($N)", clientName)
                            .build()
                    )
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(clientClass, clientName)
                            .addParameter(STRING, "basePath")
                            .addStatement("super($N, $N)", clientName, "basePath")
                            .build()
                    )
                    .addMethod(MethodSpec.constructorBuilder()
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(clientClass, clientName)
                            .addParameter(STRING, "basePath")
                            .addParameter(STRING_MULTI_MAP, "headers")
                            .addStatement("super($N, $N, $N)", clientName, "basePath", "headers")
                            .build()
                    );
        }
    }

    @Getter
    @Setter
    public static class Options {
        @ConfigurationProperty(description = "Server classes target directory", required = true)
        String location;
        @ConfigurationProperty(description = "Client classes package name", required = true)
        String clientPackage;
        @ConfigurationProperty(description = "Models package name", required = true)
        String modelPackage;
        @ConfigurationProperty(description = "Client classes name suffix", defaultValue = "Client")
        String clientSuffix = "Client";
        @ConfigurationProperty(description = "Client implementation dialect", defaultValue = "RestTemplate")
        ClientDialect dialect = ClientDialect.RestTemplate;
    }

    public enum ClientDialect {
        RestTemplate, WebClient;
    }
}

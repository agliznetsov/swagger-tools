package org.swaggertools.core.targets.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.javapoet.*;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;

import javax.lang.model.element.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.swaggertools.core.util.JavaUtils.STRING;
import static org.swaggertools.core.util.NameUtils.*;

public class GatlingClientBuilder extends ClientBuilder {
    private static final ClassName GATLING_CLIENT = ClassName.get("io.gatling.javaapi.http", "HttpRequestActionBuilder");

    public static final TypeName OBJECT_MAPPER = TypeName.get(ObjectMapper.class);

    public GatlingClientBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientOptions options) {
        super(apiDefinition, writer, options);
    }

    @Override
    protected String getBaseClassTemplate() {
        return "GatlingClient";
    }

    @Override
    protected ClassName getClientClassName() {
        return GATLING_CLIENT;
    }

    @Override
    protected TypeName getRequestBuilderClassName() {
        return ParameterizedTypeName.get(ClassName.get(Consumer.class), GATLING_CLIENT);
    }

    @Override
    protected void processOperation(Operation operation) {
        String methodName = camelCase(javaIdentifier(operation.getOperationId()));

        MethodSpec.Builder builder1 = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);
        addMethodParameters(builder1, operation, false);
        addMethodResponse(builder1, operation);
        addMethodBody(builder1, operation);
        getClient(operation.getTag()).addMethod(builder1.build());

        Optional<Parameter> body = operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
        if(body.isPresent()) {
            MethodSpec.Builder builder2 = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);
            addMethodParameters(builder2, operation, true);
            addMethodResponse(builder2, operation);
            invokeApi(builder2, operation, true);
            getClient(operation.getTag()).addMethod(builder2.build());
        }
    }

    private void addMethodParameters(MethodSpec.Builder builder, Operation operation, boolean bodyAsFunction) {
        operation.getParameters().forEach(p -> {
            ParameterSpec param = null;
            if(bodyAsFunction && p.getKind() == ParameterKind.BODY) {
                TypeName schema = schemaMapper.getType(p.getSchema(), false);
                ClassName f = ClassName.get("java.util.function", "Function");
                ClassName session = ClassName.get("io.gatling.javaapi.core", "Session");
                TypeName typeName = ParameterizedTypeName.get(f, session, schema);
                param = ParameterSpec.builder(typeName, p.getJavaIdentifier()).build();
            } else {
                param = ParameterSpec.builder(schemaMapper.getType(p.getSchema(), false), p.getJavaIdentifier()).build();
            }
            builder.addParameter(param);
        });
    }

    @Override
    protected void addMethodBody(MethodSpec.Builder builder, Operation operation) {
        invokeApi(builder, operation, false);
    }

    @Override
    protected void addMethodResponse(MethodSpec.Builder builder, Operation operation) {
        builder.returns(GATLING_CLIENT);
    }

    @Override
    protected TypeSpec.Builder newClientBuilder(String tag) {
        String name = pascalCase(javaIdentifier(tag) + options.clientSuffix);
        return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(options.clientPackage, "BaseClient"))
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .build()
                )
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(STRING, "basePath")
                        .addStatement("super($N)", "basePath")
                        .build()
                )
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(STRING, "basePath")
                        .addParameter(OBJECT_MAPPER, "objectMapper")
                        .addStatement("super($N, $N)", "basePath", "objectMapper")
                        .build()
                )
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(STRING, "basePath")
                        .addParameter(OBJECT_MAPPER, "objectMapper")
                        .addParameter(HEADERS, "headers")
                        .addStatement("super($N, $N, $N)", "basePath", "objectMapper", "headers")
                        .build()
                );
    }

    private void invokeApi(MethodSpec.Builder builder, Operation operation, boolean bodyFunction) {
        List<Object> args = new LinkedList<>();
        String method = bodyFunction ? "invokeAPI2" : "invokeAPI";
        String format = "return " + method + "($S, $S, $S, $L, $L, $L, $L)";
        args.add(operation.getOperationId());
        args.add(operation.getPath());
        args.add(operation.getMethod().name());
        args.add(createUrlVariables(operation));
        args.add(createQueryParameters(operation));
        args.add(createHeaderParameters(operation));
        Optional<Parameter> body = operation.getParameters().stream().filter(it -> it.getKind() == ParameterKind.BODY).findFirst();
        args.add(body.isPresent() ? body.get().getName() : "null");
        builder.addStatement(format, args.toArray());
    }

}

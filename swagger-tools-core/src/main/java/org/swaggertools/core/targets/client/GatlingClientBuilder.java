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
import static org.swaggertools.core.util.NameUtils.javaIdentifier;
import static org.swaggertools.core.util.NameUtils.pascalCase;

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
    protected void addMethodBody(MethodSpec.Builder builder, Operation operation) {
        invokeApi(builder, operation);
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

    private void invokeApi(MethodSpec.Builder builder, Operation operation) {
        List<Object> args = new LinkedList<>();
        String format = "return invokeAPI($S, $S, $S, $L, $L, $L, $L)";
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

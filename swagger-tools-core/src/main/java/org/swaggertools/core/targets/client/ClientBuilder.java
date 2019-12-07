package org.swaggertools.core.targets.client;

import com.squareup.javapoet.*;
import lombok.SneakyThrows;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.targets.SchemaMapper;
import org.swaggertools.core.util.NameUtils;
import org.swaggertools.core.util.StreamUtils;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.swaggertools.core.targets.JavaFileGenerator.INDENT;
import static org.swaggertools.core.util.JavaUtils.*;
import static org.swaggertools.core.util.NameUtils.*;

abstract class ClientBuilder {

    private static final TypeName HEADERS = ParameterizedTypeName.get(MAP, STRING, ParameterizedTypeName.get(LIST, STRING));
    protected static final String EVENT_STREAM = "text/event-stream";

    protected SchemaMapper schemaMapper;
    protected Map<String, TypeSpec.Builder> clients = new HashMap<>();
    protected final ApiDefinition apiDefinition;
    protected final JavaFileWriter writer;
    protected final ClientOptions options;

    public ClientBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientOptions options) {
        this.apiDefinition = apiDefinition;
        this.writer = writer;
        this.options = options;
        this.schemaMapper = new SchemaMapper(options);
    }

    public void generate() {
        apiDefinition.getOperations().forEach(this::processOperation);
        writeBaseClient(writer);
        clients.forEach((k, v) -> writer.write(JavaFile.builder(options.clientPackage, v.build()).indent(INDENT).build()));
    }

    @SneakyThrows
    protected void writeBaseClient(JavaFileWriter writer) {
        String className = getBaseClassTemplate();
        String body = StreamUtils.copyToString(getClass().getResourceAsStream("/client/" + className + ".java"));
        body = body.replace("{{package}}", options.clientPackage);
        writer.write(options.clientPackage, "BaseClient", body);
    }

    protected void processOperation(Operation operation) {
        String methodName = camelCase(javaIdentifier(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC);

        addMethodParameters(builder, operation);
        addMethodResponse(builder, operation);
        addMethodBody(builder, operation);

        getClient(operation.getTag()).addMethod(builder.build());
    }


    protected CodeBlock createUrlVariables(Operation operation) {
        List<String> names = new LinkedList<>();
        List<Object> args = new LinkedList<>();
        operation.getParameters().forEach(p -> {
            if (p.getKind() == ParameterKind.PATH) {
                names.add("$S");
                names.add("$L");
                args.add(p.getName());
                args.add(p.getJavaIdentifier());
            }
        });
        CodeBlock cb = CodeBlock.builder().add("createUrlVariables(" + String.join(", ", names) + ")", args.toArray()).build();
        return cb;
    }

    protected CodeBlock createQueryParameters(Operation operation) {
        List<String> names = new LinkedList<>();
        List<Object> args = new LinkedList<>();
        operation.getParameters().forEach(p -> {
            if (p.getKind() == ParameterKind.QUERY) {
                names.add("$S");
                names.add("$L");
                args.add(p.getName());
                args.add(p.getJavaIdentifier());
            }
        });
        return CodeBlock.builder().add("createQueryParameters(" + String.join(", ", names) + ")", args.toArray()).build();
    }

    protected void addMethodParameters(MethodSpec.Builder builder, Operation operation) {
        operation.getParameters().forEach(p -> {
            ParameterSpec param = ParameterSpec.builder(schemaMapper.getType(p.getSchema(), false), p.getJavaIdentifier()).build();
            builder.addParameter(param);
        });
    }

    protected void addMethodResponse(MethodSpec.Builder builder, Operation operation) {
        if (operation.getResponseSchema() != null) {
            builder.returns(getReturnType(operation));
        }
    }

    protected TypeName getReturnType(Operation operation) {
        return schemaMapper.getType(operation.getResponseSchema(), false);
    }

    protected TypeSpec.Builder getClient(String tag) {
        return clients.computeIfAbsent(tag, this::newClientBuilder);
    }

    protected TypeSpec.Builder newClientBuilder(String tag) {
        String name = pascalCase(javaIdentifier(tag) + options.clientSuffix);
        ClassName clientClass = getClientClassName();
        String clientName = NameUtils.camelCase(clientClass.simpleName());
        return TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(options.clientPackage, "BaseClient"))
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
                        .addParameter(HEADERS, "headers")
                        .addStatement("super($N, $N, $N)", clientName, "basePath", "headers")
                        .build()
                );
    }


    protected abstract String getBaseClassTemplate();

    protected abstract ClassName getClientClassName();

    protected abstract TypeName getRequestBuilderClassName();

    protected abstract void addMethodBody(MethodSpec.Builder builder, Operation operation);

}

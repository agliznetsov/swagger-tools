package org.swaggertools.core.targets.server;

import com.squareup.javapoet.*;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.targets.SchemaMapper;

import javax.lang.model.element.Modifier;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static org.swaggertools.core.targets.JavaFileGenerator.INDENT;
import static org.swaggertools.core.util.NameUtils.*;

abstract class ServerBuilder {

    protected static final String EVENT_STREAM = "text/event-stream";

    private final Map<String, TypeSpec.Builder> apis = new HashMap<>();
    protected final SchemaMapper schemaMapper;
    protected final ApiDefinition apiDefinition;
    protected final JavaFileWriter writer;
    protected final ServerOptions options;

    public ServerBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ServerOptions options) {
        this.apiDefinition = apiDefinition;
        this.writer = writer;
        this.options = options;
        this.schemaMapper = new SchemaMapper(options);
    }

    public void generate() {
        apiDefinition.getOperations()
                .stream().filter(Operation::isGenerateServer)
                .sorted(Comparator.comparing(Operation::getJavaIdentifier))
                .forEach(this::processOperation);
        apis.forEach((k, v) -> writeApi(v));
    }

    protected abstract void annotateClass(TypeSpec.Builder builder);

    protected abstract void annotateMethod(MethodSpec.Builder builder, Operation operation);

    protected void annotateParameter(ParameterSpec.Builder paramBuilder, Parameter parameter) {
        if (options.isValidation()) {
            if (parameter.getKind() == ParameterKind.BODY) {
                paramBuilder.addAnnotation(AnnotationSpec.builder(Valid.class).build());
            }
        }
    }

    protected abstract void addResponse(MethodSpec.Builder builder, Operation operationInfo);

    protected void writeApi(TypeSpec.Builder builder) {
        annotateClass(builder);
        writer.write(JavaFile.builder(options.apiPackage, builder.build()).indent(INDENT).build());
    }

    protected void processOperation(Operation operation) {
        String methodName = camelCase(javaIdentifier(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        annotateMethod(builder, operation);
        addParameters(builder, operation);
        addResponse(builder, operation);

        getApi(operation.getTag()).addMethod(builder.build());
    }

    protected void addParameters(MethodSpec.Builder builder, Operation operationInfo) {
        operationInfo.getParameters().forEach(p -> {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(schemaMapper.getType(p.getSchema(), false), p.getJavaIdentifier());
            annotateParameter(paramBuilder, p);
            builder.addParameter(paramBuilder.build());
        });
    }

    private TypeSpec.Builder getApi(String tag) {
        return apis.computeIfAbsent(tag, this::createApiBuilder);
    }

    private TypeSpec.Builder createApiBuilder(String tag) {
        String name = pascalCase(javaIdentifier(tag) + options.apiSuffix);
        return TypeSpec.interfaceBuilder(name)
                .addModifiers(Modifier.PUBLIC);
    }

}

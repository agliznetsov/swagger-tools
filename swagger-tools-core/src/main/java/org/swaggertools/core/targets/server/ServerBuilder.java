package org.swaggertools.core.targets.server;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.model.Operation;
import org.swaggertools.core.model.Parameter;
import org.swaggertools.core.model.ParameterKind;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.targets.SchemaMapper;

import javax.lang.model.element.Modifier;
import javax.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import static org.swaggertools.core.targets.JavaFileGenerator.INDENT;
import static org.swaggertools.core.util.NameUtils.*;

abstract class ServerBuilder {

    private final Map<String, TypeSpec.Builder> apis = new HashMap<>();
    protected final SchemaMapper schemaMapper = new SchemaMapper();
    protected final ApiDefinition apiDefinition;
    protected final JavaFileWriter writer;
    protected final ServerOptions options;

    public ServerBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ServerOptions options) {
        this.apiDefinition = apiDefinition;
        this.writer = writer;
        this.options = options;
    }

    public void generate() {
        schemaMapper.setModelPackage(options.modelPackage);
        apiDefinition.getOperations().forEach(this::processOperation);
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

    private void writeApi(TypeSpec.Builder builder) {
        annotateClass(builder);
        writer.write(JavaFile.builder(options.apiPackage, builder.build()).indent(INDENT).build());
    }

    private void processOperation(Operation operation) {
        String methodName = camelCase(javaIdentifier(operation.getOperationId()));
        MethodSpec.Builder builder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        annotateMethod(builder, operation);
        addParameters(builder, operation);
        addResponse(builder, operation);

        getApi(operation.getTag()).addMethod(builder.build());
    }

    private void addParameters(MethodSpec.Builder builder, Operation operationInfo) {
        operationInfo.getParameters().forEach(p -> {
            ParameterSpec.Builder paramBuilder = ParameterSpec.builder(schemaMapper.getType(p.getSchema(), false), p.getName());
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

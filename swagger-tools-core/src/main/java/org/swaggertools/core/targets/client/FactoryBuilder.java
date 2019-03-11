package org.swaggertools.core.targets.client;

import com.squareup.javapoet.*;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.run.JavaFileWriter;

import javax.lang.model.element.Modifier;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static javax.lang.model.element.Modifier.*;
import static org.swaggertools.core.targets.JavaFileGenerator.INDENT;
import static org.swaggertools.core.util.JavaUtils.*;
import static org.swaggertools.core.util.NameUtils.*;

class FactoryBuilder {
    private static final TypeName HEADERS = ParameterizedTypeName.get(MAP, STRING, ParameterizedTypeName.get(LIST, STRING));
    private static final TypeName HEADERS_IMPL = ParameterizedTypeName.get(HASH_MAP, STRING, ParameterizedTypeName.get(LIST, STRING));

    private final ApiDefinition apiDefinition;
    private final JavaFileWriter writer;
    private final ClientOptions options;
    private final TypeName clientType;
    private final TypeName requestBuilderType;
    private Set<String> clientNames;
    private TypeSpec.Builder builder;

    public FactoryBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientOptions options, TypeName clientType, TypeName requestBuilderType) {
        this.apiDefinition = apiDefinition;
        this.writer = writer;
        this.options = options;
        this.clientType = clientType;
        this.requestBuilderType = requestBuilderType;
    }

    public void generate() {
        clientNames = apiDefinition.getOperations().stream().map(op -> pascalCase(javaIdentifier(op.getTag()))).collect(Collectors.toSet());
        createFactoryBuilder();
        writer.write(JavaFile.builder(options.clientPackage, builder.build()).indent(INDENT).build());
    }

    private void createFactoryBuilder() {
        builder = TypeSpec.classBuilder(options.factoryName)
                .addModifiers(PUBLIC);
        createConstructor(builder);
        addProperty(clientType, "client");
        addHeaders();
        addRequestCustomizer();
        addProperties();
    }

    private void addRequestCustomizer() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("setRequestCustomizer")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(requestBuilderType, "requestCustomizer");
        for (String name : clientNames) {
            method.addStatement("$N.setRequestCustomizer(requestCustomizer)", camelCase(name));
        }
        builder.addMethod(method.build());
    }

    private void addProperties() {
        for (String name : clientNames) {
            TypeName type = ClassName.get(options.clientPackage, name + options.clientSuffix);
            FieldSpec f = FieldSpec.builder(type, camelCase(name), FINAL, PRIVATE).build();
            builder.addField(f);
            builder.addMethod(getter(f, camelCase(name)));
        }
    }

    private void addHeaders() {
        FieldSpec field = FieldSpec.builder(HEADERS, "headers")
                .addModifiers(PRIVATE, FINAL)
                .initializer("new $T()", HEADERS_IMPL)
                .build();
        builder.addField(field);
        builder.addMethod(getter(field));
    }

    private void addProperty(TypeName type, String name) {
        FieldSpec field = FieldSpec.builder(type, name, FINAL, PRIVATE).build();
        builder.addField(field);
        builder.addMethod(getter(field));
    }

    private void createConstructor(TypeSpec.Builder builder) {
        MethodSpec.Builder mb = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(clientType, "client")
                .addParameter(STRING, "basePath")
                .addStatement("this.client = client");

        for (String name : clientNames) {
            mb.addStatement("$N = new $T(client, basePath, headers)", camelCase(name),
                    ClassName.get(options.clientPackage, name + options.clientSuffix));
        }

        builder.addMethod(mb.build());
    }

}

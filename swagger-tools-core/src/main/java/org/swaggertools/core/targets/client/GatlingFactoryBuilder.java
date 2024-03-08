package org.swaggertools.core.targets.client;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.run.JavaFileWriter;

import javax.lang.model.element.Modifier;

import static javax.lang.model.element.Modifier.PUBLIC;
import static org.swaggertools.core.util.JavaUtils.STRING;
import static org.swaggertools.core.util.NameUtils.camelCase;

class GatlingFactoryBuilder extends FactoryBuilder {

    public GatlingFactoryBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientOptions options, TypeName clientType, TypeName requestBuilderType) {
        super(apiDefinition, writer, options, clientType, requestBuilderType);
    }

    @Override
    protected void createFactoryBuilder() {
        builder = TypeSpec.classBuilder(options.factoryName).addModifiers(PUBLIC);
        createConstructor(builder);
        addHeaders();
        addRequestCustomizer();
        addProperties();
    }

    @Override
    protected void createConstructor(TypeSpec.Builder builder) {
        MethodSpec.Builder mb = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(STRING, "baseUrl")
                .addParameter(GatlingClientBuilder.OBJECT_MAPPER, "objectMapper");

        for (String name : clientNames) {
            mb.addStatement("$N = new $T(baseUrl, objectMapper, headers)", camelCase(name),
                    ClassName.get(options.clientPackage, name + options.clientSuffix));
        }

        builder.addMethod(mb.build());
    }

}

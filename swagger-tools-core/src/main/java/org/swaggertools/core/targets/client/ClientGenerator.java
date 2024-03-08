package org.swaggertools.core.targets.client;

import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.targets.JavaFileGenerator;

@Slf4j
public class ClientGenerator extends JavaFileGenerator<ClientOptions> {

    public static final String NAME = "client";

    public ClientGenerator() {
        super(new ClientOptions());
    }

    @Override
    public String getGroupName() {
        return NAME;
    }

    @Override
    public void accept(ApiDefinition apiDefinition) {
        validateConfiguration();
        setModelPackage(apiDefinition, options);

        log.info("Generating client in {}/{}", options.location, options.clientPackage);
        JavaFileWriter writer = createWriter(options.location);
        ClientBuilder clientBuilder = createClientBuilder(apiDefinition, writer);
        clientBuilder.generate();
        if (options.factoryName != null && !options.factoryName.isEmpty()) {
            FactoryBuilder factoryBuilder = createFactoryBuilder(apiDefinition, writer, clientBuilder);
            factoryBuilder.generate();
        }
    }

    private ClientBuilder createClientBuilder(ApiDefinition apiDefinition, JavaFileWriter writer) {
        switch (options.dialect) {
            case RestTemplate:
                return new RestTemplateBuilder(apiDefinition, writer, options);
            case WebClient:
                return new WebClientBuilder(apiDefinition, writer, options);
            case HttpClient:
                return new HttpClientBuilder(apiDefinition, writer, options);
            case GatlingClient:
                return new GatlingClientBuilder(apiDefinition, writer, options);
        }
        throw new IllegalArgumentException("Unknown dialect: " + options.dialect);
    }

    private FactoryBuilder createFactoryBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ClientBuilder clientBuilder) {
        switch (options.dialect) {
            case RestTemplate:
            case WebClient:
            case HttpClient:
                return new FactoryBuilder(apiDefinition, writer, options, clientBuilder.getClientClassName(), clientBuilder.getRequestBuilderClassName());
            case GatlingClient:
                return new GatlingFactoryBuilder(apiDefinition, writer, options, clientBuilder.getClientClassName(), clientBuilder.getRequestBuilderClassName());
        }
        throw new IllegalArgumentException("Unknown dialect: " + options.dialect);
    }

}

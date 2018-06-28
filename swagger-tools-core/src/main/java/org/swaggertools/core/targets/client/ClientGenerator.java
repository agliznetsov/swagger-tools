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
        log.info("Generating client in {}/{}", options.location, options.clientPackage);
        JavaFileWriter writer = createWriter(options.location);
        ClientBuilder clientBuilder = createClientBuilder(apiDefinition, writer);
        clientBuilder.generate();
        if (options.factoryName != null && !options.factoryName.isEmpty()) {
            new FactoryBuilder(apiDefinition, writer, options, clientBuilder.getClientClassName()).generate();
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
        }
        throw new IllegalArgumentException("Unknown dialect: " + options.dialect);
    }

}

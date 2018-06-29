package org.swaggertools.core.targets.server;

import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.targets.JavaFileGenerator;

@Slf4j
public class ServerGenerator extends JavaFileGenerator<ServerOptions> {
    public static final String NAME = "server";

    public ServerGenerator() {
        super(new ServerOptions());
    }

    @Override
    public String getGroupName() {
        return NAME;
    }

    @Override
    public void accept(ApiDefinition apiDefinition) {
        validateConfiguration();
        log.info("Generating server in {}/{}", options.location, options.apiPackage);
        JavaFileWriter writer = createWriter(options.location);
        ServerBuilder serverBuilder = createServerBuilder(apiDefinition, writer, options);
        serverBuilder.generate();
    }

    private ServerBuilder createServerBuilder(ApiDefinition apiDefinition, JavaFileWriter writer, ServerOptions options) {
        switch (this.options.dialect) {
            case Spring:
                return new SpringBuilder(apiDefinition, writer, options);
            case JaxRS:
                return new JaxRsBuilder(apiDefinition, writer, options);
        }
        throw new IllegalArgumentException("Unknown dialect: " + options.dialect);
    }

}

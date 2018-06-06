package org.swaggertools.cli;

import com.squareup.javapoet.JavaFile;
import lombok.SneakyThrows;
import org.apache.commons.cli.*;
import org.swaggertools.core.FileFormat;
import org.swaggertools.core.Processor;
import org.swaggertools.core.consumer.JavaFileWriterImpl;
import org.swaggertools.core.consumer.model.JacksonModelGenerator;
import org.swaggertools.core.consumer.spring.web.ClientGenerator;
import org.swaggertools.core.consumer.spring.web.ServerGenerator;
import org.swaggertools.core.supplier.ApiDefinitionSupplier;

import java.io.File;
import java.io.FileInputStream;

public class Generator {
    public static void main(String[] args) {
        new Generator().run(args);
    }

    private void run(String[] args) {
        Options options = createOptions();
        try {
            generate(new DefaultParser().parse(options, args));
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(100, "swagger-tools-cli", "", options, "", true);
            System.exit(1);
        }
    }

    private void generate(CommandLine commandLine) {
        Processor processor = new Processor();
        setSource(processor, commandLine);
        setTargets(processor, commandLine);
        if (processor.getApiConsumers().isEmpty()) {
            System.out.println("No target set");
            System.exit(1);
        } else {
            processor.process();
        }
    }

    @SneakyThrows
    private void setSource(Processor processor, CommandLine commandLine) {
        String src = commandLine.getOptionValue(SOURCE_LOCATION);
        FileFormat ff = src.toLowerCase().endsWith(".json") ? FileFormat.JSON : FileFormat.YAML;
        FileInputStream is = new FileInputStream(new File(src));
        processor.setApiSupplier(new ApiDefinitionSupplier(is, ff));
    }

    private void setTargets(Processor processor, CommandLine commandLine) {
        if (commandLine.hasOption(TARGET_MODEL_LOCATION)) {
            String target = commandLine.getOptionValue(TARGET_MODEL_LOCATION);
            String modelPackage = commandLine.getOptionValue(TARGET_MODEL_MODEL_PACKAGE, "model");

            JacksonModelGenerator generator = new JacksonModelGenerator();
            generator.setModelPackageName(modelPackage);
            generator.setWriter(getWriter(target));
            processor.getApiConsumers().add(generator);
            System.out.println("Generating model in " + target + "/" + modelPackage);
        }
        if (commandLine.hasOption(TARGET_SERVER_LOCATION)) {
            String target = commandLine.getOptionValue(TARGET_SERVER_LOCATION);
            String modelPackage = commandLine.getOptionValue(TARGET_SERVER_MODEL_PACKAGE, "model");
            String serverPackage = commandLine.getOptionValue(TARGET_SERVER_SERVER_PACKAGE, "server");

            ServerGenerator generator = new ServerGenerator();
            generator.setModelPackageName(modelPackage);
            generator.setApiPackageName(serverPackage);
            generator.setWriter(getWriter(target));
            processor.getApiConsumers().add(generator);
            System.out.println("Generating server in " + target + "/" + serverPackage);
        }
        if (commandLine.hasOption(TARGET_CLIENT_LOCATION)) {
            String target = commandLine.getOptionValue(TARGET_CLIENT_LOCATION);
            String modelPackage = commandLine.getOptionValue(TARGET_CLIENT_MODEL_PACKAGE, "model");
            String clientPackage = commandLine.getOptionValue(TARGET_CLIENT_CLIENT_PACKAGE, "client");

            ClientGenerator generator = new ClientGenerator();
            generator.setModelPackageName(modelPackage);
            generator.setClientPackageName(clientPackage);
            generator.setWriter(getWriter(target));
            processor.getApiConsumers().add(generator);
            System.out.println("Generating client in " + target + "/" + clientPackage);
        }
    }

    private JavaFileWriterImpl getWriter(String target) {
        return new JavaFileWriterImpl(new File(target)) {
            @Override
            public void write(JavaFile javaFile) {
                System.out.println("Writing " + javaFile.packageName + "." + javaFile.typeSpec.name);
                super.write(javaFile);
            }
        };
    }

    private Options createOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt(SOURCE_LOCATION).required().hasArg().desc("Source file location").build());
        
        options.addOption(Option.builder().longOpt(TARGET_MODEL_LOCATION).hasArg().desc("Model classes location").build());
        options.addOption(Option.builder().longOpt(TARGET_MODEL_MODEL_PACKAGE).hasArg().desc("Model package name").build());

        options.addOption(Option.builder().longOpt(TARGET_SERVER_LOCATION).hasArg().desc("Server classes location").build());
        options.addOption(Option.builder().longOpt(TARGET_SERVER_MODEL_PACKAGE).hasArg().desc("Model package name").build());
        options.addOption(Option.builder().longOpt(TARGET_SERVER_SERVER_PACKAGE).hasArg().desc("Server package name").build());

        options.addOption(Option.builder().longOpt(TARGET_CLIENT_LOCATION).hasArg().desc("Client classes location").build());
        options.addOption(Option.builder().longOpt(TARGET_CLIENT_MODEL_PACKAGE).hasArg().desc("Model package name").build());
        options.addOption(Option.builder().longOpt(TARGET_CLIENT_CLIENT_PACKAGE).hasArg().desc("Client package name").build());
        return options;
    }
    
    private static final String SOURCE_LOCATION = "source.location";
    
    private static final String TARGET_MODEL_LOCATION = "target.model.location";
    private static final String TARGET_MODEL_MODEL_PACKAGE = "target.model.model-package";
    
    private static final String TARGET_SERVER_LOCATION = "target.server.location";
    private static final String TARGET_SERVER_MODEL_PACKAGE = "target.server.model-package";
    private static final String TARGET_SERVER_SERVER_PACKAGE = "target.server.server-package";
    
    private static final String TARGET_CLIENT_LOCATION = "target.client.location";
    private static final String TARGET_CLIENT_MODEL_PACKAGE = "target.client.model-package";
    private static final String TARGET_CLIENT_CLIENT_PACKAGE = "target.client.client-package";
}

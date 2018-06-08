package org.swaggertools.core.run;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.consumer.model.JacksonModelGenerator;
import org.swaggertools.core.consumer.spring.web.ClientGenerator;
import org.swaggertools.core.consumer.spring.web.ServerGenerator;
import org.swaggertools.core.supplier.ApiDefinitionSupplier;
import org.swaggertools.core.util.AssertUtils;
import org.swaggertools.core.util.FileFormat;
import org.swaggertools.core.util.JavaFileWriterImpl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import static org.swaggertools.core.run.Configuration.*;

@Slf4j
public class ProcessorFactory {
    private Map<Configuration, String> options;

    public ProcessorFactory(Map<Configuration, String> options) {
        this.options = options;
    }

    public Processor create() {
        Processor processor = new Processor();
        setSource(processor);
        setTargets(processor);
        if (processor.getApiConsumers().isEmpty()) {
            throw new IllegalArgumentException("Target not set");
        }
        return processor;
    }

    @SneakyThrows
    private void setSource(Processor processor) {
        String src = getOption(SOURCE_LOCATION);
        AssertUtils.notNull(src, "Source is not set");
        FileFormat ff = src.toLowerCase().endsWith(".json") ? FileFormat.JSON : FileFormat.YAML;
        FileInputStream is = new FileInputStream(new File(src));
        processor.setApiSupplier(new ApiDefinitionSupplier(is, ff));
    }

    private void setTargets(Processor processor) {
        addModel(processor);
        addServer(processor);
        addClient(processor);
    }

    private void addClient(Processor processor) {
        String target = getOption(TARGET_CLIENT_LOCATION);
        if (target != null) {
            String modelPackage = getOption(TARGET_CLIENT_MODEL_PACKAGE);
            String clientPackage = getOption(TARGET_CLIENT_CLIENT_PACKAGE);

            ClientGenerator generator = new ClientGenerator();
            generator.setModelPackageName(modelPackage);
            generator.setClientPackageName(clientPackage);
            generator.setClientSuffix(getOption(TARGET_CLIENT_SUFFIX));
            generator.setWriter(getWriter(target));
            processor.getApiConsumers().add(generator);
            log.info("Generating client in {}/{}", target, clientPackage);
        }
    }

    private void addServer(Processor processor) {
        String target = getOption(TARGET_SERVER_LOCATION);
        if (target != null) {
            String modelPackage = getOption(TARGET_SERVER_MODEL_PACKAGE);
            String serverPackage = getOption(TARGET_SERVER_SERVER_PACKAGE);

            ServerGenerator generator = new ServerGenerator();
            generator.setModelPackageName(modelPackage);
            generator.setApiPackageName(serverPackage);
            generator.setApiSuffix(getOption(TARGET_SERVER_SUFFIX));
            generator.setWriter(getWriter(target));
            processor.getApiConsumers().add(generator);
            log.info("Generating server in {}/{}", target, serverPackage);
        }
    }

    private void addModel(Processor processor) {
        String target = getOption(TARGET_MODEL_LOCATION);
        if (target != null) {
            String modelPackage = getOption(TARGET_MODEL_MODEL_PACKAGE);
            String initCollections = getOption(TARGET_MODEL_INITIALIZE_COLLECTIONS);

            JacksonModelGenerator generator = new JacksonModelGenerator();
            generator.setModelPackageName(modelPackage);
            generator.setWriter(getWriter(target));
            if (initCollections != null) {
                generator.setInitializeCollectionFields(Boolean.parseBoolean(initCollections));
            }

            processor.getApiConsumers().add(generator);
            log.info("Generating model in {}/{}", target, modelPackage);
        }
    }

    private JavaFileWriterImpl getWriter(String target) {
        return new JavaFileWriterImpl(new File(target));
    }

    private String getOption(Configuration key) {
        return options.getOrDefault(key, key.getDefaultValue());
    }
}

package org.swaggertools.core.run;

import org.junit.Test;
import org.swaggertools.core.config.Configuration;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.source.ApiDefinitionSource;
import org.swaggertools.core.targets.JacksonModelGenerator;
import org.swaggertools.core.targets.ClientGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProcessorFactoryTest {
    @Test
    public void create_default() {
        Map<String, String> options = new HashMap<>();
        options.put("source.location", "/src/file");
        options.put("target.model.location", "/target");
        options.put("target.model.model-package", "com.example.model");
        options.put("target.model.initialize-collections", "false");
        options.put("target.client.location", "/target");
        options.put("target.client.model-package", "com.example.model");
        options.put("target.client.client-package", "com.example.client");

        ProcessorFactory factory = new ProcessorFactory();
        Processor processor = factory.create(options);
        ApiDefinitionSource source = (ApiDefinitionSource) processor.getSource();
        assertEquals("/src/file", source.getOptions().getLocation());

        assertEquals(2, processor.getTargets().size());

        JacksonModelGenerator model = (JacksonModelGenerator) processor.getTargets().get(1);
        assertEquals("/target", model.getOptions().getLocation());
        assertEquals("com.example.model", model.getOptions().getModelPackage());
        assertEquals(false, model.getOptions().isInitializeCollections());

        ClientGenerator client = (ClientGenerator) processor.getTargets().get(0);
        assertEquals("/target", client.getOptions().getLocation());
    }

    @Test
    public void create_from_class() {
        ProcessorFactory.registerTarget("abc", MyTarget::new);

        Map<String, String> options = new HashMap<>();
        options.put("source.location", "/src/file");
        options.put("target.abc.location", "/target");

        ProcessorFactory factory = new ProcessorFactory();
        Processor processor = factory.create(options);
        assertEquals(1, processor.getTargets().size());
        MyTarget myTarget = (MyTarget) processor.getTargets().get(0);
        assertEquals("/target",  myTarget.configValues.get("location"));
    }

    @Test
    public void test_invalid_property() {
        Map<String, String> options = new HashMap<>();
        options.put("source.bad-property", "");
        ProcessorFactory factory = new ProcessorFactory();
        try {
            factory.create(options);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unknown configuration property: source.bad-property", e.getMessage());
        }
    }

    @Test
    public void test_missing_property() {
        Map<String, String> options = new HashMap<>();
        options.put("source.location", "");
        ProcessorFactory factory = new ProcessorFactory();
        try {
            factory.create(options);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Required configuration property is not set: source.location", e.getMessage());
        }
    }

    public static class MyTarget implements Target {
        public Map<String, String> configValues;

        @Override
        public void accept(ApiDefinition apiDefinition) {
        }

        @Override
        public String getGroupName() {
            return "abc";
        }

        @Override
        public List<Configuration> getConfigurations() {
            return null;
        }

        @Override
        public void configure(Map<String, String> configValues) {
            this.configValues = configValues;
        }
    }
}

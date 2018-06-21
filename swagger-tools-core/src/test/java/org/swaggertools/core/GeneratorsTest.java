package org.swaggertools.core;

import com.squareup.javapoet.JavaFile;
import lombok.SneakyThrows;
import org.junit.Test;
import org.swaggertools.core.run.*;
import org.swaggertools.core.source.ApiDefinitionSource;
import org.swaggertools.core.targets.JacksonModelGenerator;
import org.swaggertools.core.targets.ClientGenerator;
import org.swaggertools.core.targets.ServerGenerator;
import org.swaggertools.core.util.StreamUtils;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class GeneratorsTest {
    MemoryWriter memoryWriter = new MemoryWriter();

    @Test
    public void test_openapi() throws Exception {
        testPetStore("/petstore/openapi.yaml");
    }

    @Test
    public void test_swagger() throws Exception {
        testPetStore("/petstore/swagger.yaml");
    }

    @Test
    public void test_server_reactive() throws Exception {
        memoryWriter.files.clear();
        Processor processor = new Processor();
        processor.setSource(createSource("/petstore/openapi.yaml"));

        ServerGenerator target = createServerGenerator();
        target.getOptions().setReactive(true);
        processor.setTargets(Collections.singletonList(target));
        processor.process();
        verifyJavaFile("/petstore/server/PetsApiReactive", memoryWriter.files.get("PetsApi"));
    }

    @Test
    public void test_client_reactive() throws Exception {
        memoryWriter.files.clear();
        Processor processor = new Processor();
        processor.setSource(createSource("/petstore/openapi.yaml"));

        ClientGenerator target = createClientGenerator();
        target.getOptions().setDialect(ClientGenerator.ClientDialect.WebClient);
        target.getOptions().setClientSuffix("WebClient");
        processor.setTargets(Collections.singletonList(target));
        processor.process();
        verifyJavaFile("/petstore/client/PetsWebClient", memoryWriter.files.get("PetsWebClient"));
    }

    public void testPetStore(String source) throws Exception {
        Processor processor = new Processor();
        processor.setSource(createSource(source));

        processor.setTargets(Collections.singletonList(createModelGenerator()));
        processor.process();
        assertEquals(10, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/model/" + k, v));

        memoryWriter.files.clear();
        processor.setTargets(Collections.singletonList(createClientGenerator()));
        processor.process();
        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/client/" + k, v));

        memoryWriter.files.clear();
        processor.setTargets(Collections.singletonList(createServerGenerator()));
        processor.process();
        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/server/" + k, v));
    }

    private JacksonModelGenerator createModelGenerator() {
        JacksonModelGenerator generator = new JacksonModelGenerator() {
            @Override
            protected JavaFileWriter createWriter(String target) {
                return memoryWriter;
            }
        };
        generator.getOptions().setModelPackage("com.example");
        generator.getOptions().setLocation("/target");
        return generator;
    }

    private ServerGenerator createServerGenerator() {
        ServerGenerator generator = new ServerGenerator() {
            @Override
            protected JavaFileWriter createWriter(String target) {
                return memoryWriter;
            }
        };
        generator.getOptions().setModelPackage("com.example.model");
        generator.getOptions().setApiPackage("com.example.web");
        generator.getOptions().setLocation("/target");
        return generator;
    }

    private ClientGenerator createClientGenerator() {
        ClientGenerator generator = new ClientGenerator() {
            @Override
            protected JavaFileWriter createWriter(String target) {
                return memoryWriter;
            }
        };
        generator.getOptions().setModelPackage("com.example.model");
        generator.getOptions().setClientPackage("com.example.client");
        generator.getOptions().setLocation("/target");
        return generator;
    }

    private Source createSource(String location) throws Exception {
        URL url = this.getClass().getResource(location);
        String path = url.getPath();
        ApiDefinitionSource source = new ApiDefinitionSource();
        source.getOptions().setLocation(path);
        return source;
    }

    @SneakyThrows
    private void verifyJavaFile(String path, String java) {
        String expected = StreamUtils.copyToString(getClass().getResourceAsStream(path + ".java"));
        assertEquals(normalize(expected), normalize(java));
    }

    String normalize(String string) {
        return string.replace("\r", "").trim();
    }

    class MemoryWriter implements JavaFileWriter {
        final Map<String, String> files = new HashMap<>();

        @Override
        @SneakyThrows
        public void write(JavaFile javaFile) {
            StringBuilder sb = new StringBuilder();
            javaFile.writeTo(sb);
            files.put(javaFile.typeSpec.name, sb.toString());
        }

        @Override
        public void write(String packageName, String className, String body) {
            //nop
        }
    }

}

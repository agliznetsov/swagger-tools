package org.swaggertools.core;

import com.squareup.javapoet.JavaFile;
import lombok.SneakyThrows;
import org.junit.Test;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.run.Processor;
import org.swaggertools.core.run.Source;
import org.swaggertools.core.source.ApiDefinitionSource;
import org.swaggertools.core.targets.client.ClientDialect;
import org.swaggertools.core.targets.client.ClientGenerator;
import org.swaggertools.core.targets.model.ModelGenerator;
import org.swaggertools.core.targets.server.ServerDialect;
import org.swaggertools.core.targets.server.ServerGenerator;
import org.swaggertools.core.util.StreamUtils;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class GeneratorsTest {
    MemoryWriter memoryWriter = new MemoryWriter();

    @Test
    public void test_openapi() throws Exception {
        testPetStore("/petstore/openapi3.yaml");
    }

    @Test
    public void test_swagger() throws Exception {
        testPetStore("/petstore/swagger.yaml");
    }

    @Test
    public void test_server_reactive() throws Exception {
        memoryWriter.files.clear();
        Processor processor = new Processor();
        processor.setSource(createSource("/petstore/openapi3.yaml"));

        ServerGenerator target = createServerGenerator(null);
        target.getOptions().setReactive(true);
        processor.setTargets(Collections.singletonList(target));
        processor.process();
        verifyJavaFile("/petstore/server/PetsApiReactive", memoryWriter.files.get("PetsApi"));
    }

    @Test
    public void test_server_jaxrs() throws Exception {
        memoryWriter.files.clear();
        Processor processor = new Processor();
        processor.setSource(createSource("/petstore/openapi3.yaml"));

        ServerGenerator target = createServerGenerator(null);
        target.getOptions().setDialect(ServerDialect.JaxRS);
        processor.setTargets(Collections.singletonList(target));
        processor.process();
        verifyJavaFile("/petstore/server/PetsApiJaxrs", memoryWriter.files.get("PetsApi"));
    }

    @Test
    public void test_client_WebClient() throws Exception {
        memoryWriter.files.clear();
        Processor processor = new Processor();
        processor.setSource(createSource("/petstore/openapi3.yaml"));

        ClientGenerator target = createClientGenerator(null);
        target.getOptions().setDialect(ClientDialect.WebClient);
        target.getOptions().setClientSuffix("WebClient");
        processor.setTargets(Collections.singletonList(target));
        processor.process();
        verifyJavaFile("/petstore/client/PetsWebClient", memoryWriter.files.get("PetsWebClient"));
    }

    @Test
    public void test_client_factory() throws Exception {
        memoryWriter.files.clear();
        Processor processor = new Processor();
        processor.setSource(createSource("/petstore/openapi2.yaml"));

        ClientGenerator target = createClientGenerator("com.example.model");
        target.getOptions().setFactoryName("Petstore");
        processor.setTargets(Collections.singletonList(target));
        processor.process();
        verifyJavaFile("/petstore/factory/Petstore", memoryWriter.files.get("Petstore"));
    }

    @Test
    public void testValidation() throws Exception {
        Processor processor = new Processor();
        processor.setSource(createSource("/validation/openapi.yaml"));

        ModelGenerator modelGenerator = createModelGenerator("com.example.model");
        modelGenerator.getOptions().setValidation(true);
        modelGenerator.getOptions().setLombokUniqueBuilder(true);
        modelGenerator.getOptions().setLombokSuperBuilder(true);
        processor.setTargets(Collections.singletonList(modelGenerator));
        processor.process();
        assertEquals(3, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/validation/model/" + k, v));

        memoryWriter.files.clear();
        ServerGenerator serverGenerator = createServerGenerator("com.example.model");
        serverGenerator.getOptions().setValidation(true);
        processor.setTargets(Collections.singletonList(serverGenerator));
        processor.process();
        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/validation/server/" + k, v));
    }


    public void testPetStore(String source) throws Exception {
        Processor processor = new Processor();
        processor.setSource(createSource(source));

        processor.setTargets(Collections.singletonList(createModelGenerator(null)));
        processor.process();
        //assertEquals(10, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/model/" + k, v));

        memoryWriter.files.clear();
        processor.setTargets(Collections.singletonList(createClientGenerator(null)));
        processor.process();
        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/client/" + k, v));

        memoryWriter.files.clear();
        processor.setTargets(Collections.singletonList(createServerGenerator(null)));
        processor.process();
        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/server/" + k, v));
    }

    private ModelGenerator createModelGenerator(String modelPackage) {
        ModelGenerator generator = new ModelGenerator() {
            @Override
            protected JavaFileWriter createWriter(String target) {
                return memoryWriter;
            }
        };
        generator.getOptions().setModelPackage(modelPackage);
        generator.getOptions().setLocation("/target");
        generator.getOptions().setLombok(true);
        return generator;
    }

    private ServerGenerator createServerGenerator(String modelPackage) {
        ServerGenerator generator = new ServerGenerator() {
            @Override
            protected JavaFileWriter createWriter(String target) {
                return memoryWriter;
            }
        };
        generator.getOptions().setModelPackage(modelPackage);
        generator.getOptions().setApiPackage("com.example.web");
        generator.getOptions().setLocation("/target");
        return generator;
    }

    private ClientGenerator createClientGenerator(String modelPackage) {
        ClientGenerator generator = new ClientGenerator() {
            @Override
            protected JavaFileWriter createWriter(String target) {
                return memoryWriter;
            }
        };
        generator.getOptions().setModelPackage(modelPackage);
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
//        String filePath = "swagger-tools-core/src/test/resources" + path + ".java";
//        FileOutputStream out = new FileOutputStream(new File(filePath));
//        out.write(java.getBytes());
//        out.close();
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

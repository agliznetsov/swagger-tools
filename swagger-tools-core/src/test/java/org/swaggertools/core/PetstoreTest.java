package org.swaggertools.core;

import com.squareup.javapoet.JavaFile;
import lombok.SneakyThrows;
import org.junit.Test;
import org.swaggertools.core.consumer.JavaFileWriter;
import org.swaggertools.core.consumer.model.JacksonModelGenerator;
import org.swaggertools.core.consumer.spring.web.ClientGenerator;
import org.swaggertools.core.consumer.spring.web.ServerGenerator;
import org.swaggertools.core.supplier.ApiDefinitionSupplier;
import org.swaggertools.core.util.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class PetstoreTest {

    @Test
    public void test_openapi() throws Exception {
        String source = "/petstore/openapi.yaml";
        testModel(source);
        testServer(source);
        testClient(source);
    }

    @Test
    public void test_swagger() throws Exception {
        String source = "/petstore/swagger.yaml";
        testModel(source);
        testServer(source);
        testClient(source);
    }

    public void testModel(String source) throws Exception {
        Processor processor = createProcessor(source);

        MemoryWriter memoryWriter = new MemoryWriter();
        JacksonModelGenerator generator = new JacksonModelGenerator();
        generator.setModelPackageName("com.example");
        generator.setWriter(memoryWriter);
        processor.getApiConsumers().add(generator);
        processor.process();

        assertEquals(8, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/model/" + k, v));
    }

    public void testServer(String source) throws Exception {
        Processor processor = createProcessor(source);

        MemoryWriter memoryWriter = new MemoryWriter();
        ServerGenerator generator = new ServerGenerator();
        generator.setModelPackageName("com.example.model");
        generator.setApiPackageName("com.example.web");
        generator.setWriter(memoryWriter);
        processor.getApiConsumers().add(generator);
        processor.process();

        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/server/" + k, v));
    }

    public void testClient(String source) throws Exception {
        Processor processor = createProcessor(source);

        MemoryWriter memoryWriter = new MemoryWriter();
        ClientGenerator generator = new ClientGenerator();
        generator.setModelPackageName("com.example.model");
        generator.setClientPackageName("com.example.client");
        generator.setWriter(memoryWriter);
        processor.getApiConsumers().add(generator);
        processor.process();

        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/client/" + k, v));
    }

    private Processor createProcessor(String source) throws FileNotFoundException, URISyntaxException {
        URL url = this.getClass().getResource(source);
        FileInputStream is = new FileInputStream(new File(url.toURI()));
        Processor processor = new Processor();
        processor.setApiSupplier(new ApiDefinitionSupplier(is, FileFormat.YAML));
        return processor;
    }

    @SneakyThrows
    private void verifyJavaFile(String path, String java) {
        String expected = StreamUtils.copyToString(getClass().getResourceAsStream(path + ".java"));
        assertEquals(normalize(expected), normalize(java));
//        if (!normalize(expected).equals(normalize(java))) {
//            System.err.println(path);
//            System.err.println(java);
//        }
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

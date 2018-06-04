package org.swaggertools.core;

import com.squareup.javapoet.JavaFile;
import lombok.SneakyThrows;
import org.junit.Test;
import org.swaggertools.core.consumer.JavaFileWriter;
import org.swaggertools.core.consumer.model.JacksonModelGenerator;
import org.swaggertools.core.consumer.spring.mvc.server.MvcServerGenerator;
import org.swaggertools.core.supplier.OpenAPIDefinition;

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
    public void test_petstore_model() throws Exception {
        Processor processor = createProcessor();

        MemoryWriter memoryWriter = new MemoryWriter();
        JacksonModelGenerator generator = new JacksonModelGenerator();
        generator.setModelPackageName("com.example");
        generator.setWriter(memoryWriter);
//        generator.setWriter(new JavaFileWriterImpl(new File("C:\\work\\misc\\swagger-tools\\swagger-tools-core\\src\\test\\java")));
        processor.getApiConsumers().add(generator);
        processor.process();

        assertEquals(8, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/model/" + k, v));
    }

    @Test
    public void test_petstore_server() throws Exception {
        Processor processor = createProcessor();

        MemoryWriter memoryWriter = new MemoryWriter();
        MvcServerGenerator generator = new MvcServerGenerator();
        generator.setModelPackageName("com.example.model");
        generator.setApiPackageName("com.example.web");
        generator.setWriter(memoryWriter);
//        generator.setWriter(new JavaFileWriterImpl(new File("C:\\work\\misc\\swagger-tools\\swagger-tools-core\\src\\test\\java")));
        processor.getApiConsumers().add(generator);
        processor.process();

        assertEquals(1, memoryWriter.files.size());
        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/server/" + k, v));
    }

    private Processor createProcessor() throws FileNotFoundException, URISyntaxException {
        URL url = this.getClass().getResource("/petstore/petstore.yaml");
        FileInputStream is = new FileInputStream(new File(url.toURI()));
        Processor processor = new Processor();
        processor.setApiSupplier(new OpenAPIDefinition(is, FileFormat.YAML));
        return processor;
    }

    @SneakyThrows
    private void verifyJavaFile(String path, String java) {
        String expected = StreamUtils.copyToString(getClass().getResourceAsStream(path + ".java"));
        assertEquals(normalize(expected), normalize(java));
    }

    String normalize(String string) {
        return string.replace("\r", "");
    }

    class MemoryWriter implements JavaFileWriter {
        final Map<String, String> files = new HashMap<>();

        @Override
        @SneakyThrows
        public void write(JavaFile javaFile) {
            StringBuilder sb = new StringBuilder();
            javaFile.writeTo(sb);
//            System.out.println(sb);
            files.put(javaFile.typeSpec.name, sb.toString());
        }
    }

}

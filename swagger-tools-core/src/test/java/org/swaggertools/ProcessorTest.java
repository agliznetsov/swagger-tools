package org.swaggertools;

import com.squareup.javapoet.JavaFile;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.swaggertools.core.FileFormat;
import org.swaggertools.core.Processor;
import org.swaggertools.core.consumer.JavaFileWriter;
import org.swaggertools.core.consumer.ModelWriter;
import org.swaggertools.core.supplier.OpenAPIDefinition;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessorTest {

    @Test
    void test_petstore_model() throws Exception {
        URL url = this.getClass().getResource("/petstore/petstore.yaml");
        FileInputStream is = new FileInputStream(new File(url.toURI()));
        Processor processor = new Processor();
        processor.setApiSupplier(new OpenAPIDefinition(is, FileFormat.YAML));

        MemoryWriter memoryWriter = new MemoryWriter();
        ModelWriter writer = new ModelWriter();
        writer.setPackageName("com.example");
        writer.setWriter(memoryWriter);
        processor.getApiConsumers().add(writer);
        processor.process();

        assertEquals(8, memoryWriter.files.size());
        memoryWriter.files.forEach((k,v) -> verifyFile(k,v));
    }

    @SneakyThrows
    private void verifyFile(String name, String java) {
        String expected = StreamUtils.copyToString(getClass().getResourceAsStream("/petstore/" + name + ".java"));
        assertEquals(expected, java);
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
    }

}

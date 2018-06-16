//package org.swaggertools.core;
//
//import com.squareup.javapoet.JavaFile;
//import lombok.SneakyThrows;
//import org.junit.Test;
//import org.swaggertools.core.run.JavaFileWriter;
//import org.swaggertools.core.run.Processor;
//import org.swaggertools.core.run.Source;
//import org.swaggertools.core.run.Target;
//import org.swaggertools.core.source.ApiDefinitionSource;
//import org.swaggertools.core.target.model.JacksonModelGenerator;
//import org.swaggertools.core.target.spring.ClientGenerator;
//import org.swaggertools.core.target.spring.ServerGenerator;
//import org.swaggertools.core.util.StreamUtils;
//
//import java.net.URL;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.Assert.assertEquals;
//
//
//public class PetstoreTest {
//    MemoryWriter memoryWriter = new MemoryWriter();
//
//    @Test
//    public void test_openapi() throws Exception {
//        test("/petstore/openapi.yaml");
//    }
//
//    @Test
//    public void test_swagger() throws Exception {
//        test("/petstore/swagger.yaml");
//    }
//
//    public void test(String source) throws Exception {
//        Processor processor = new Processor();
//        processor.setSource(createSource(source));
//
//        processor.setTargets(Collections.singletonList(createModelGenerator()));
//        processor.process();
//        assertEquals(10, memoryWriter.files.size());
//        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/model/" + k, v));
//
//        memoryWriter.files.clear();
//        processor.setTargets(Collections.singletonList(createClientGenerator()));
//        processor.process();
//        assertEquals(1, memoryWriter.files.size());
//        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/client/" + k, v));
//
//        memoryWriter.files.clear();
//        processor.setTargets(Collections.singletonList(createServerGenerator()));
//        processor.process();
//        assertEquals(1, memoryWriter.files.size());
//        memoryWriter.files.forEach((k, v) -> verifyJavaFile("/petstore/server/" + k, v));
//    }
//
//    private Target createModelGenerator() {
//        JacksonModelGenerator generator = new JacksonModelGenerator() {
//            @Override
//            protected JavaFileWriter createWriter(String target) {
//                return memoryWriter;
//            }
//        };
//        generator.getOptions().setModelPackageName("com.example");
//        return generator;
//    }
//
//    private Target createServerGenerator() {
//        ServerGenerator generator = new ServerGenerator() {
//            @Override
//            protected JavaFileWriter createWriter(String target) {
//                return memoryWriter;
//            }
//        };
//        generator.getOptions().setModelPackageName("com.example.model");
//        generator.getOptions().setApiPackageName("com.example.web");
//        return generator;
//    }
//
//    private Target createClientGenerator() {
//        ClientGenerator generator = new ClientGenerator() {
//            @Override
//            protected JavaFileWriter createWriter(String target) {
//                return memoryWriter;
//            }
//        };
//        generator.getOptions().setModelPackageName("com.example.model");
//        generator.getOptions().setClientPackageName("com.example.client");
//        return generator;
//    }
//
//    private Source createSource(String location) throws Exception {
//        URL url = this.getClass().getResource(location);
//        String path = url.getPath();
//        ApiDefinitionSource source = new ApiDefinitionSource();
//        source.getOptions().setLocation(path);
//        return source;
//    }
//
//    @SneakyThrows
//    private void verifyJavaFile(String path, String java) {
//        String expected = StreamUtils.copyToString(getClass().getResourceAsStream(path + ".java"));
//        assertEquals(normalize(expected), normalize(java));
//    }
//
//    String normalize(String string) {
//        return string.replace("\r", "").trim();
//    }
//
//    class MemoryWriter implements JavaFileWriter {
//        final Map<String, String> files = new HashMap<>();
//
//        @Override
//        @SneakyThrows
//        public void write(JavaFile javaFile) {
//            StringBuilder sb = new StringBuilder();
//            javaFile.writeTo(sb);
//            files.put(javaFile.typeSpec.name, sb.toString());
//        }
//
//        @Override
//        public void write(String packageName, String className, String body) {
//            //nop
//        }
//    }
//
//}

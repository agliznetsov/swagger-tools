package org.swaggertools.core.consumer;

import com.squareup.javapoet.JavaFile;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;

@AllArgsConstructor
public class JavaFileWriterImpl implements JavaFileWriter {
    final File dir;

    @Override
    @SneakyThrows
    public void write(JavaFile javaFile) {
        javaFile.writeTo(dir);
    }

    @Override
    @SneakyThrows
    public void write(String packageName, String className, String body) {
        String packageDir = packageName.replace('.', '/');
        File file = new File(dir, packageDir + "/" + className + ".java");
        file.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(body.getBytes());
        }
    }
}
package org.swaggertools.core.consumer;

import com.squareup.javapoet.JavaFile;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;

@AllArgsConstructor
public class FileWriter implements JavaFileWriter {
    final File dir;

    @Override
    @SneakyThrows
    public void write(JavaFile javaFile) {
        javaFile.writeTo(dir);
    }
}
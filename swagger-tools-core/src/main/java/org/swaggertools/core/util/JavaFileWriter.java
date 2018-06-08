package org.swaggertools.core.util;

import com.squareup.javapoet.JavaFile;

public interface JavaFileWriter {
    void write(JavaFile javaFile);

    void write(String packageName, String className, String body);
}

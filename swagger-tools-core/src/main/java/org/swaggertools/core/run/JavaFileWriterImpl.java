package org.swaggertools.core.run;

import com.squareup.javapoet.JavaFile;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;

@Slf4j
@AllArgsConstructor
public class JavaFileWriterImpl implements JavaFileWriter {
    final File target;

    @Override
    @SneakyThrows
    public void write(JavaFile javaFile) {
        log.info("Writing {}", javaFile.packageName + "." + javaFile.typeSpec.name);
        javaFile.writeTo(target);
    }

    @Override
    @SneakyThrows
    public void write(String packageName, String className, String body) {
        String packageDir = packageName.replace('.', '/');
        File file = new File(target, packageDir + "/" + className + ".java");
        log.info("Writing {}", packageName + '.' + className);
        file.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(body.getBytes());
        }
    }
}
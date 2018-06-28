package org.swaggertools.core.targets;

import org.swaggertools.core.config.AutoConfigurable;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.run.JavaFileWriterImpl;
import org.swaggertools.core.run.Target;

import java.io.File;

public abstract class JavaFileGenerator<T> extends AutoConfigurable<T> implements Target {

    public static final String INDENT = "    ";

    protected JavaFileGenerator(T options) {
        super(options);
    }

    protected JavaFileWriter createWriter(String target) {
        return new JavaFileWriterImpl(new File(target));
    }

}

package org.swaggertools.core.targets;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.config.AutoConfigurable;
import org.swaggertools.core.run.JavaFileWriter;
import org.swaggertools.core.run.JavaFileWriterImpl;
import org.swaggertools.core.run.Target;

import java.io.File;

public abstract class JavaFileGenerator<T> extends AutoConfigurable<T> implements Target {

    @Getter
    @Setter
    protected String indent = "    ";

    protected JavaFileGenerator(T options) {
        super(options);
    }

    protected JavaFileWriter createWriter(String target) {
        return new JavaFileWriterImpl(new File(target));
    }

}

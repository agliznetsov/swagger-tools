package org.swaggertools.core.consumer;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.model.ApiDefinition;
import org.swaggertools.core.util.JavaFileWriter;

import java.util.function.Consumer;

import static org.swaggertools.core.util.AssertUtils.notNull;

public abstract class JavaFileGenerator implements Consumer<ApiDefinition> {

    @Getter
    @Setter
    protected JavaFileWriter writer;

    @Getter
    @Setter
    protected String indent = "    ";

    @Override
    public void accept(ApiDefinition apiDefinition) {
        notNull(writer, "writer is not set");
    }

}

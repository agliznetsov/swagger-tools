package org.swaggertools.core.run;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.model.ApiDefinition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.swaggertools.core.util.AssertUtils.notNull;

@Getter
@Setter
public class Processor {
    private Supplier<ApiDefinition> apiSupplier;
    private final Collection<Consumer<ApiDefinition>> apiConsumers = new LinkedList<>();

    public void process() {
        notNull(apiSupplier, "apiSupplier is not set");
        notNull(apiConsumers, "apiConsumers is not set");
        ApiDefinition api = apiSupplier.get();
        apiConsumers.forEach(it -> it.accept(api));
    }
}

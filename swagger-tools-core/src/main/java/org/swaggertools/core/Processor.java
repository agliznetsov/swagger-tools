package org.swaggertools.core;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
@Setter
public class Processor {
    private Supplier<OpenAPI> apiSupplier;
    private final Collection<Consumer<OpenAPI>> apiConsumers = new LinkedList<>();

    public void process() {
        if (apiSupplier == null) {
            throw new IllegalArgumentException("apiSupplier is not set");
        }
        if (apiConsumers.isEmpty()) {
            throw new IllegalArgumentException("apiConsumers is not set");
        }
        OpenAPI api = apiSupplier.get();
        apiConsumers.forEach(it -> it.accept(api));
    }
}

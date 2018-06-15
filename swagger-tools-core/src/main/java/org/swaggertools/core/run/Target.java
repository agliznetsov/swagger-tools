package org.swaggertools.core.run;

import org.swaggertools.core.config.Configurable;
import org.swaggertools.core.model.ApiDefinition;

public interface Target extends Configurable {
    void accept(ApiDefinition apiDefinition);
}

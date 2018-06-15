package org.swaggertools.core.run;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.model.ApiDefinition;

import java.util.Collection;
import java.util.LinkedList;

import static org.swaggertools.core.util.AssertUtils.notEmpty;
import static org.swaggertools.core.util.AssertUtils.notNull;

@Getter
@Setter
public class Processor {
    private Source source;
    private Collection<Target> targets = new LinkedList<>();

    public void process() {
        notNull(source, "source is not set");
        notEmpty(targets, "target is not set");
        ApiDefinition api = source.getApiDefinition();
        targets.forEach(it -> it.accept(api));
    }
}

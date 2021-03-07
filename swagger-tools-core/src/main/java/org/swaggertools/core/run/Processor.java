package org.swaggertools.core.run;

import lombok.Getter;
import lombok.Setter;
import org.swaggertools.core.model.ApiDefinition;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.swaggertools.core.util.AssertUtils.notEmpty;
import static org.swaggertools.core.util.AssertUtils.notNull;

@Getter
@Setter
public class Processor {
    private String[] sources;
    private Source source;
    private List<Target> targets = new LinkedList<>();

    public Processor() {
        this.sources = null;
    }

    public Processor(String[] sources) {
        this.sources = sources;
    }

    public void process() {
        notNull(source, "source is not set");
        notEmpty(targets, "target is not set");
        ApiDefinition api = source.getApiDefinition(sources);
        targets.forEach(it -> it.accept(api));
    }
}

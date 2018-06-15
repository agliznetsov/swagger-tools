package org.swaggertools.core.run;

import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.source.ApiDefinitionSource;
import org.swaggertools.core.target.model.JacksonModelGenerator;
import org.swaggertools.core.target.spring.ClientGenerator;
import org.swaggertools.core.target.spring.ServerGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ProcessorFactory {
    private static final String SOURCE = "source.";
    private static final Pattern TARGET = Pattern.compile("target\\.(.+?)\\.(.+?)");
    private static final String MODEL = "model";
    private static final String CLIENT = "client";
    private static final String SERVER = "server";

    private Map<String, String> options;
    Processor processor;

    public Processor create(Map<String, String> options) {
        this.options = options;
        processor = new Processor();
        setSource();
        setTargets();
        return processor;
    }

    private void setSource() {
        Map<String, String> sourceOptions = findSourceOptions();
        ApiDefinitionSource source = new ApiDefinitionSource();
        source.configure(sourceOptions);
        processor.setSource(source);
    }

    private void setTargets() {
        Map<String, Map<String, String>> targets = findTargets();
        targets.forEach(this::addTarget);
    }

    private void addTarget(String name, Map<String, String> options) {
        Target target = createTarget(name, options);
        target.configure(options);
        processor.getTargets().add(target);
    }

    private Target createTarget(String name, Map<String, String> options) {
        switch (name) {
            case MODEL:
                return new JacksonModelGenerator();
            case CLIENT:
                return new ClientGenerator();
            case SERVER:
                return new ServerGenerator();
        }
        throw new IllegalArgumentException("Unknown target type: " + name);
    }

    private Map<String, Map<String, String>> findTargets() {
        Map<String, Map<String, String>> res = new HashMap<>();
        options.forEach((k, v) -> {
            Matcher m = TARGET.matcher(k);
            if (m.matches()) {
                String name = m.group(1);
                String key = m.group(2);
                res.computeIfAbsent(name, it -> new HashMap<>()).put(key, v);
            }
        });
        return res;
    }

    private Map<String, String> findSourceOptions() {
        return options.keySet().stream()
                .filter(it -> it.startsWith(SOURCE))
                .map(it -> it.substring(SOURCE.length()))
                .collect(Collectors.toMap(it -> it, it -> options.get(it)));
    }
}

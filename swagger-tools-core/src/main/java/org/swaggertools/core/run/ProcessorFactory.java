package org.swaggertools.core.run;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.Configuration;
import org.swaggertools.core.source.ApiDefinitionSource;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ProcessorFactory {
    private static final String SOURCE = "source.";
    private static final Pattern TARGET = Pattern.compile("target\\.(.+?)\\.(.+?)");

    private static final Map<String, Supplier<Target>> targets = new HashMap<>();

    public static Map<String, List<Configuration>> getTargets() {
        Map<String, List<Configuration>> res = new HashMap<>();
        targets.forEach((k, v) -> {
            res.put(k, v.get().getConfigurations());
        });
        return res;
    }

    static {
        try {
            ServiceLoader<Target> serviceLoader = ServiceLoader.load(Target.class);
            for (Target target : serviceLoader) {
                if (targets.putIfAbsent(target.getGroupName(), () -> createInstance(target.getClass())) != null) {
                    log.error("Duplicate target names found: " + target.getGroupName());
                }
            }
        } catch (ServiceConfigurationError e) {
            log.error("Configuration error", e);
        }
    }

    @SneakyThrows
    private static Target createInstance(Class<? extends Target> targetClass) {
        return targetClass.newInstance();
    }

    private Map<String, String> options;
    private Processor processor;

    public Processor create(String[] sources, Map<String, String> options) {
        this.options = options;
        this.processor = new Processor(sources);
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
        Target target = createTarget(name);
        target.configure(options);
        processor.getTargets().add(target);
    }

    private Target createTarget(String name) {
        Supplier<Target> supplier = targets.get(name);
        if (supplier == null) {
            throw new IllegalArgumentException("Unknown target name: " + name);
        }
        return supplier.get();
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
                .collect(Collectors.toMap(it -> it.substring(SOURCE.length()), it -> options.get(it)));
    }
}

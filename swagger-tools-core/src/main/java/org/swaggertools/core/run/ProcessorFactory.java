package org.swaggertools.core.run;

import lombok.extern.slf4j.Slf4j;
import org.swaggertools.core.config.Configuration;
import org.swaggertools.core.source.ApiDefinitionSource;
import org.swaggertools.core.targets.ClientGenerator;
import org.swaggertools.core.targets.JacksonModelGenerator;
import org.swaggertools.core.targets.ServerGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ProcessorFactory {
    private static final String SOURCE = "source.";
    private static final Pattern TARGET = Pattern.compile("target\\.(.+?)\\.(.+?)");

    private static final Map<String, Supplier<Target>> targets = new HashMap<>();

    public static void registerTarget(String name, Supplier<Target> supplier) {
        if (targets.putIfAbsent(name, supplier) != null) {
            throw new IllegalArgumentException("Target " + name + " is already registered");
        }
    }

    public static Map<String, List<Configuration>> getTargets() {
        Map<String, List<Configuration>> res = new HashMap<>();
        targets.forEach((k, v) -> {
            res.put(k, v.get().getConfigurations());
        });
        return res;
    }

    static {
        registerTarget(JacksonModelGenerator.NAME, JacksonModelGenerator::new);
        registerTarget(ClientGenerator.NAME, ClientGenerator::new);
        registerTarget(ServerGenerator.NAME, ServerGenerator::new);
    }

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

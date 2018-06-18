package org.swaggertools.core.config;

import lombok.SneakyThrows;
import org.swaggertools.core.util.NameUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AutoConfigurable<T> implements Configurable {
    protected final T options;
    private final List<Configuration> configurations = new LinkedList<>();
    private final Map<String, Field> fields = new HashMap<>();

    public T getOptions() {
        return options;
    }

    protected AutoConfigurable(T options) {
        this.options = options;
        discoverProperties();
    }

    private void discoverProperties() {
        for (Field f : options.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            Configuration config = createConfiguration(f);
            configurations.add(config);
            fields.put(config.getName(), f);
        }
    }

    private Configuration createConfiguration(Field field) {
        Configuration config = new Configuration();
        config.setName(NameUtils.spinalCase(field.getName()));
        ConfigurationProperty property = field.getAnnotation(ConfigurationProperty.class);
        if (property != null) {
            if (!property.name().isEmpty()) {
                config.setName(property.name());
            }
            config.setDefaultValue(property.defaultValue());
            config.setDescription(property.description());
            config.setRequired(property.required());
        }
        return config;
    }

    @Override
    public List<Configuration> getConfigurations() {
        return configurations;
    }

    @Override
    public void configure(Map<String, String> configValues) {
        configValues.forEach((k, v) -> {
            Field f = fields.get(k);
            if (f == null) {
                throw new IllegalArgumentException("Unknown configuration property: " + getGroupName() + "." + k);
            }
            setValue(f, v);
        });
        validateConfiguration();
    }

    @SneakyThrows
    private void setValue(Field field, String value) {
        Object v = value;
        if (field.getType() == Boolean.class || field.getType() == boolean.class) {
            v = Boolean.parseBoolean(value);
        } else if (field.getType() == Integer.class || field.getType() == int.class) {
            v = Integer.parseInt(value);
        }
        field.set(options, v);
    }

    @SneakyThrows
    protected void validateConfiguration() {
        for(Configuration config : configurations) {
            if (config.isRequired()) {
                Object value = fields.get(config.getName()).get(options);
                if (value == null || String.valueOf(value).isEmpty()) {
                    throw new IllegalArgumentException("Required configuration property is not set: " + getGroupName() + "." + config.getName());
                }
            }
        }
    }
}

package org.swaggertools.core.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Configuration {
    final String name;
    final String description;
    final String defaultValue;
}

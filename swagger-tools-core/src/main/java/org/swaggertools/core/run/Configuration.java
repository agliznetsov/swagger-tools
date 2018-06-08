package org.swaggertools.core.run;

public enum Configuration {
    SOURCE_LOCATION("source.location","Swagger definition file location", null),

    TARGET_MODEL_LOCATION("target.model.location", "Model classes target directory", null),
    TARGET_MODEL_MODEL_PACKAGE("target.model.model-package", "Model package name", "model"),
    TARGET_MODEL_INITIALIZE_COLLECTIONS("target.model.init-collections", "Initialize collection type (lists and maps) fields", "true"),

    TARGET_SERVER_LOCATION("target.server.location", "Server classes target directory", null),
    TARGET_SERVER_MODEL_PACKAGE("target.server.model-package", "Model package name", "model"),
    TARGET_SERVER_SERVER_PACKAGE("target.server.server-package", "Server package name", "server"),
    TARGET_SERVER_SUFFIX("target.server.suffix", "Server class name suffix", "Api"),

    TARGET_CLIENT_LOCATION("target.client.location", "Client classes target directory", null),
    TARGET_CLIENT_MODEL_PACKAGE("target.client.model-package", "Model package name", "model"),
    TARGET_CLIENT_CLIENT_PACKAGE("target.client.client-package", "Client package name", "client"),
    TARGET_CLIENT_SUFFIX("target.client.suffix", "Client class name suffix", "Client");

    private String key;
    private String description;
    private String defaultValue;

    Configuration(String key, String description, String defaultValue) {
        this.key = key;
        this.description = description;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }


    @Override
    public String toString() {
        return key;
    }
}

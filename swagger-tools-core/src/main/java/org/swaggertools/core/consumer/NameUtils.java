package org.swaggertools.core.consumer;

public class NameUtils {
    public static String sanitize(String name) {
        return name.replace("_", "");
    }

    public static String pascalCase(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}

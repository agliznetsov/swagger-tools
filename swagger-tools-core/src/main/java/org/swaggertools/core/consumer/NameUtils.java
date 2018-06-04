package org.swaggertools.core.consumer;

public class NameUtils {
    public static String sanitize(String name) {
        return name.replace("_", "").replace(" ", "");
    }

    public static String pascalCase(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static String camelCase(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}

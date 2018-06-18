package org.swaggertools.core.util;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

    public static String upperCase(String name) {
        List<String> parts = splitName(name);
        return String.join("_", parts.stream().map(String::toUpperCase).collect(Collectors.toList()));
    }

    public static String spinalCase(String name) {
        List<String> parts = splitName(name);
        return String.join("-", parts.stream().map(String::toLowerCase).collect(Collectors.toList()));
    }

    private static List<String> splitName(String name) {
        List<String> parts = new LinkedList<>();
        int start = 0;
        for (int i = 1; i < name.length(); i++) {
            if (Character.isLowerCase(name.charAt(i - 1)) && Character.isUpperCase(name.charAt(i))) {
                parts.add(name.substring(start, i));
                start = i;
            }
        }
        parts.add(name.substring(start, name.length()));
        return parts;
    }
}

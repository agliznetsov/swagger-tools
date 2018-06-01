package org.swaggertools.core.util;

public class AssertUtils {
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }
}

package org.swaggertools.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.swaggertools.core.util.NameUtils.*;

public class NameUtilsTest {
    @Test
    public void upper_case() {
        assertEquals("ONE", upperCase("one"));
        assertEquals("ONE_TWO_THREE", upperCase("oneTwoThree"));
        assertEquals("ONE_TWO_THREE_F", upperCase("oneTwoThreeF"));
        assertEquals("ONE_TWO", upperCase("OneTwo"));
    }

    @Test
    public void spinal_case() {
        assertEquals("one", spinalCase("one"));
        assertEquals("one-two", spinalCase("oneTwo"));
    }
}

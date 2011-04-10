package org.fakereplace.test.basic.reflection;

import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FinalMethodTest {
    @Test
    public void testFinalMethodModifiers() throws SecurityException, NoSuchMethodException {
        Method m = ClassWithFinalMethods.class.getMethod("method");
        assert (m.getModifiers() & Modifier.FINAL) != 0;
    }
}

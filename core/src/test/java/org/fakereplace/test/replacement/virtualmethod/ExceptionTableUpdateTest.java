package org.fakereplace.test.replacement.virtualmethod;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExceptionTableUpdateTest {
    @Test
    public void testExceptionTableCorrect() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ClassReplacer cr = new ClassReplacer();
        cr.queueClassForReplacement(VirtualMethodExceptionClass.class, VirtualMethodExceptionClass1.class);
        cr.replaceQueuedClasses();

        VirtualMethodExceptionClass i = new VirtualMethodExceptionClass();
        Method m = i.getClass().getMethod("doStuff1", int.class, int.class);
        m.invoke(i, 0, 0);
        m = i.getClass().getMethod("doStuff2", int.class, int.class);
        m.invoke(i, 0, 0);
    }

}

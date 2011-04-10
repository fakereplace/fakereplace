package org.fakereplace.test.basic.reflection;

import org.testng.annotations.Test;

import java.lang.reflect.Field;

public class FieldTest {
    @Test()
    public void testFieldAccess() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        DoStuff d = new DoStuff();
        Field f = d.getClass().getField("field");
        String s = (String) f.get(d);
        assert s.equals("hello world");
        f.set(d, "bye world");
        s = (String) f.get(d);
        assert s.equals("bye world");

    }

}

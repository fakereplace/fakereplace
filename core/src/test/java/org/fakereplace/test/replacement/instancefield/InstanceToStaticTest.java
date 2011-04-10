package org.fakereplace.test.replacement.instancefield;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

/**
 * when changing instance fields to static existing reference will still
 * reference the instance field
 *
 * @author stuart
 */
public class InstanceToStaticTest {
    @BeforeClass
    public void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(InstanceToStatic.class, InstanceToStatic1.class);
        r.replaceQueuedClasses();
    }

    @Test
    public void testInstanceToStatic() {
        InstanceToStatic f1 = new InstanceToStatic();
        InstanceToStatic f2 = new InstanceToStatic();
        f1.setField(100);
        assert f2.getField() == 100;
    }

    @Test
    public void testInstanceToStaticViaReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceToStatic f1 = new InstanceToStatic();

        Field f = f1.getClass().getDeclaredField("field");
        f.setAccessible(true);
        f.setInt(null, 200);
        assert f.getInt(null) == 200;
    }

}

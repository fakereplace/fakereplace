package org.fakereplace.test.replacement.annotated.field;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

public class AnnotatedFieldTest {

    @Test
    public void testFieldAnnotations() throws SecurityException, NoSuchFieldException {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(FieldAnnotated.class, FieldAnnotated1.class);
        r.replaceQueuedClasses();

        Field m1 = FieldAnnotated.class.getField("field1");
        Field m2 = FieldAnnotated.class.getField("field2");
        Field m3 = FieldAnnotated.class.getField("field3");
        assert m1.getAnnotation(FieldAnnotation.class).value().equals("1");
        assert !m2.isAnnotationPresent(FieldAnnotation.class);
        assert m3.getAnnotation(FieldAnnotation.class).value().equals("3");

    }

}

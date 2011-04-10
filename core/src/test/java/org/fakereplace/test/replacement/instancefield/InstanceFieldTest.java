package org.fakereplace.test.replacement.instancefield;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

public class InstanceFieldTest {
    @BeforeClass(groups = "instancefield")
    public void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(InstanceFieldClass.class, InstanceFieldClass1.class);
        rep.replaceQueuedClasses();
    }

    @Test(groups = "instancefield")
    public void testAddingInstanceField() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {

        InstanceFieldClass ns = new InstanceFieldClass();
        ns.inc();
        assert ns.get() == 1;
        ns.inclong();
        assert ns.getlong() == 2;
        assert ns.getSv().equals("aa");
    }

    @Test(groups = "instancefield")
    public void testChangingInstanceFieldType() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(ChangeFieldType.class, ChangeFieldType1.class);
        rep.replaceQueuedClasses();
        ChangeFieldType type = new ChangeFieldType();
        assert type.getValue() == 20;
    }

    @Test(groups = "instancefield")
    public void testSettingObjectInstanceField() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        InstanceFieldClass ns = new InstanceFieldClass();
        ns.setFa2(this);
        assert ns.getFa2() == this;
    }
}

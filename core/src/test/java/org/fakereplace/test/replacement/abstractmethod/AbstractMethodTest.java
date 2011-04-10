package org.fakereplace.test.replacement.abstractmethod;

import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;

public class AbstractMethodTest {
    @BeforeClass(groups = "abstractmethod")
    public void setup() {
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(AbstractClass.class, AbstractClass1.class);
        rep.queueClassForReplacement(AbstractCaller.class, AbstractCaller1.class);
        rep.queueClassForReplacement(BigChild.class, BigChild1.class);
        rep.queueClassForReplacement(SmallChild.class, SmallChild1.class);
        rep.replaceQueuedClasses();
    }

    @Test(groups = "abstractmethod")
    public void testAbstractMethod() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        AbstractCaller caller = new AbstractCaller();
        BigChild big = new BigChild();
        SmallChild small = new SmallChild();

        assert caller.getValue(big).equals("big");
        assert caller.getValue(small).equals("small");
    }

}

package org.fakereplace.test.basic.reflection;

import org.fakereplace.ConstructorArgument;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;

public class ConstructorTest {
    @Test
    public void testGetDeclaredConstrcutors() {

        DoStuff d = new DoStuff();
        Constructor<?>[] meths = d.getClass().getDeclaredConstructors();
        for (Constructor<?> m : meths) {
            if (m.getParameterTypes().length == 3 && m.getParameterTypes()[2].equals(ConstructorArgument.class)) {
                throw new RuntimeException("Added constructor delegator showing up in declared methods");
            }
        }
    }

}

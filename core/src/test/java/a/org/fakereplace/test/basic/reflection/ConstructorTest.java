/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package a.org.fakereplace.test.basic.reflection;

import java.lang.reflect.Constructor;

import org.fakereplace.core.ConstructorArgument;
import org.junit.Test;

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

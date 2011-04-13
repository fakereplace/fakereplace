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

package a.org.fakereplace.test.replacement.abstractmethod;

import a.org.fakereplace.test.util.ClassReplacer;
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

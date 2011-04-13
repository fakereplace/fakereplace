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

package a.org.fakereplace.test.replacement.instancefield;

import a.org.fakereplace.test.util.ClassReplacer;
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

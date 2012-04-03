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

import java.lang.reflect.Field;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * when changing instance fields to static existing reference will still
 * reference the instance field
 *
 * @author stuart
 */
public class InstanceToStaticTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(InstanceToStatic.class, InstanceToStatic1.class);
        r.replaceQueuedClasses();
    }

    @Test
    public void testInstanceToStatic() {
        InstanceToStatic f1 = new InstanceToStatic();
        InstanceToStatic f2 = new InstanceToStatic();
        f1.setField(100);
        Assert.assertEquals(100, f2.getField());
    }

    @Test
    public void testInstanceToStaticViaReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        InstanceToStatic f1 = new InstanceToStatic();

        Field f = f1.getClass().getDeclaredField("field");
        f.setAccessible(true);
        f.setInt(null, 200);
        Assert.assertEquals(200, f.getInt(null));
    }

}

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

package a.org.fakereplace.test.replacement.staticfield;

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
public class StaticToInstanceTest {
    @BeforeClass
    public static void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticToInstance.class, StatictoInstance1.class);
        r.replaceQueuedClasses();
    }

    @Test
    public void testStaticToInstance() {
        StaticToInstance f1 = new StaticToInstance();
        StaticToInstance f2 = new StaticToInstance();
        f1.setField(100);
        Assert.assertEquals(20,  f2.getField());
    }

    @Test
    public void testStaticToInstanceViaReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        StaticToInstance f1 = new StaticToInstance();
        StaticToInstance f2 = new StaticToInstance();
        Field f = f1.getClass().getDeclaredField("field");
        f.setAccessible(true);
        f.setInt(f1, 200);
        Assert.assertEquals(20,  f.getInt(f2));
    }

}

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

package org.fakereplace.test.replacement.staticmethod.visibility;

import org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.StaticMethodVisibilityClass;
import org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.StaticMethodVisibilityClass1;
import org.fakereplace.test.replacement.staticmethod.visibility.otherpackage.UnchangedStaticMethodCallingClass;
import org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IncreaseVisibilityTest {
    @BeforeClass
    public void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticMethodVisibilityCallingClass.class, StaticMethodVisibilityCallingClass1.class);
        r.queueClassForReplacement(StaticMethodVisibilityClass.class, StaticMethodVisibilityClass1.class);
        r.replaceQueuedClasses();
    }

    @Test
    public void testExistingMethod() {
        assert StaticMethodVisibilityClass.callingMethod().equals("helo world");
    }

    @Test
    public void testNewExternalMethod() {
        assert StaticMethodVisibilityCallingClass.callingClass().equals("helo world");
    }

    @Test
    public void testUnchangedClassCallingExternalMethod() {
        assert UnchangedStaticMethodCallingClass.callingClass().equals("helo world");
    }
}

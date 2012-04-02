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

import a.org.fakereplace.test.coverage.ChangeTestType;
import a.org.fakereplace.test.coverage.CodeChangeType;
import a.org.fakereplace.test.coverage.Coverage;
import a.org.fakereplace.test.coverage.MultipleCoverage;
import a.org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * when changing instance fields to static existing reference will still
 * reference the instance field
 *
 * @author stuart
 */
public class StaticToInstanceTest {
    @BeforeClass
    public void setup() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(StaticToInstance.class, StatictoInstance1.class);
        r.replaceQueuedClasses();
    }

    @Test
    @Coverage(change = CodeChangeType.STATIC_FIELD_TO_INSTANCE, privateMember = true, test = ChangeTestType.ACCESS_THROUGH_BYTECODE)
    public void testStaticToInstance() {
        StaticToInstance f1 = new StaticToInstance();
        StaticToInstance f2 = new StaticToInstance();
        f1.setField(100);
        assert f2.getField() != 100;
    }

    @Test
    @MultipleCoverage({
            @Coverage(change = CodeChangeType.STATIC_FIELD_TO_INSTANCE, privateMember = true, test = ChangeTestType.GET_DECLARED_BY_NAME),
            @Coverage(change = CodeChangeType.STATIC_FIELD_TO_INSTANCE, privateMember = true, test = ChangeTestType.INVOKE_BY_REFLECTION)})
    public void testStaticToInstanceViaReflection() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        StaticToInstance f1 = new StaticToInstance();
        StaticToInstance f2 = new StaticToInstance();
        Field f = f1.getClass().getDeclaredField("field");
        f.setAccessible(true);
        f.setInt(f1, 200);
        assert f.getInt(f2) != 200;
    }

}

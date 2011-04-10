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

package org.fakereplace.test.basic.reflection;

import org.fakereplace.test.coverage.ChangeTestType;
import org.fakereplace.test.coverage.CodeChangeType;
import org.fakereplace.test.coverage.Coverage;
import org.fakereplace.test.coverage.MultipleCoverage;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConstructorPermissionTest {

    public ConstructorPermissionTest() {

    }

    @Test
    @MultipleCoverage({
            @Coverage(privateMember = false, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.GET_DECLARED_BY_NAME),
            @Coverage(privateMember = false, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.INVOKE_BY_REFLECTION)})
    public void testConstructorPermissions() throws SecurityException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Constructor<? extends ConstructorPermissionTest> m = getClass().getDeclaredConstructor();
        m.newInstance();
    }

    @MultipleCoverage({
            @Coverage(privateMember = true, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.GET_DECLARED_BY_NAME),
            @Coverage(privateMember = true, change = CodeChangeType.EXISTING_CONSTRUCTOR, test = ChangeTestType.INVOKE_BY_REFLECTION)})
    @Test(expectedExceptions = IllegalAccessException.class)
    public void testConstructorPermissionsOnOtherClass() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException,
            InstantiationException {
        Constructor<ConstructorPermissionBean> m = ConstructorPermissionBean.class.getDeclaredConstructor();
        m.newInstance();
    }

}

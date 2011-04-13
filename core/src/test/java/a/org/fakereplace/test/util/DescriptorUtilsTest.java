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

package a.org.fakereplace.test.util;

import org.fakereplace.util.DescriptorUtils;
import org.testng.annotations.Test;

public class DescriptorUtilsTest {

    @Test
    public void testMethodParsing() throws ClassNotFoundException {
        Class[] test = DescriptorUtils.argumentStringToClassArray("(IJ)V", this
                .getClass());
        assert test[0] == int.class;
        assert test[1] == long.class;
        test = DescriptorUtils.argumentStringToClassArray("([I[[J)V", this
                .getClass());
        assert test[0] == int[].class;
        assert test[1] == long[][].class;
        test = DescriptorUtils.argumentStringToClassArray(
                "([I[[JLjava/lang/String;)V", this.getClass());
        assert test[0] == int[].class;
        assert test[1] == long[][].class;
        assert test[2] == String.class;
        test = DescriptorUtils.argumentStringToClassArray(
                "([I[[J[Ljava/lang/String;[[[Ljava/lang/String;)V", this
                .getClass());
        assert test[0] == int[].class;
        assert test[1] == long[][].class;
        assert test[2] == String[].class;
        assert test[3] == String[][][].class;
    }

    @Test
    public void getReturnTypeTest() {
        String ret;
        ret = DescriptorUtils.getReturnType("()C");
        assert ret.equals("C") : ret;
        ret = DescriptorUtils.getReturnType("(IJ)Ljava/lang/String;");
        assert ret.equals("Ljava/lang/String;");
    }

    @Test
    public void getArgumentTest() {
        String ret;
        ret = DescriptorUtils.getArgumentString("()C");
        assert ret.equals("()") : ret;

        ret = DescriptorUtils.getArgumentString("(IJ)Ljava/lang/String;");
        assert ret.equals("(IJ)");
    }
}

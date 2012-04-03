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
import org.junit.Assert;
import org.junit.Test;

public class DescriptorUtilsTest {

    @Test
    public void testMethodParsing() throws ClassNotFoundException {
        Class[] test = DescriptorUtils.argumentStringToClassArray("(IJ)V", this
                .getClass());
        Assert.assertEquals(int.class, test[0]);
        Assert.assertEquals(long.class, test[1]);
        test = DescriptorUtils.argumentStringToClassArray("([I[[J)V", this
                .getClass());
        Assert.assertEquals(int[].class, test[0]);
        Assert.assertEquals(long[][].class, test[1]);
        test = DescriptorUtils.argumentStringToClassArray(
                "([I[[JLjava/lang/String;)V", this.getClass());
        Assert.assertEquals(int[].class, test[0] );
        Assert.assertEquals(long[][].class, test[1] );
        Assert.assertEquals( String.class, test[2]);
        test = DescriptorUtils.argumentStringToClassArray(
                "([I[[J[Ljava/lang/String;[[[Ljava/lang/String;)V", this
                .getClass());
        Assert.assertEquals(int[].class, test[0] );
        Assert.assertEquals(long[][].class, test[1]);
        Assert.assertEquals(String[].class, test[2] );
        Assert.assertEquals(String[][][].class, test[3]);
    }

    @Test
    public void getReturnTypeTest() {
        String ret;
        ret = DescriptorUtils.getReturnType("()C");
        Assert.assertEquals("C" , ret);
        ret = DescriptorUtils.getReturnType("(IJ)Ljava/lang/String;");
        Assert.assertEquals("Ljava/lang/String;", ret);
    }

    @Test
    public void getArgumentTest() {
        String ret;
        ret = DescriptorUtils.getArgumentString("()C");
        Assert.assertEquals("()", ret);

        ret = DescriptorUtils.getArgumentString("(IJ)Ljava/lang/String;");
        Assert.assertEquals("(IJ)", ret);
    }
}

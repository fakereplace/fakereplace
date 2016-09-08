/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

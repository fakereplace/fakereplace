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

package a.org.fakereplace.test.replacement.lambda;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("This still needs some work, we need to rewrite the BootstrapMethods attribute")
public class ClassWithLambdaTest {
    @Test
    public void testReplaceClassWithLambda() throws Exception {

        Assert.assertEquals("first", LambdaClass.getMessageProducer().call());
        ClassReplacer rep = new ClassReplacer();
        rep.queueClassForReplacement(LambdaClass.class, LambdaClass1.class);
        rep.replaceQueuedClasses();
        Assert.assertEquals("second", LambdaClass.getMessageProducer().call());
    }

}

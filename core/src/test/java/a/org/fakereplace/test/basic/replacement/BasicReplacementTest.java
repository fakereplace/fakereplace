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

package a.org.fakereplace.test.basic.replacement;

import a.org.fakereplace.test.util.ClassReplacer;
import org.junit.Assert;
import org.junit.Test;

public class BasicReplacementTest {
    @Test
    public void testInstrumentationReplacement() {
        BasicTestRep re = new BasicTestRep();
        int val0 = re.value();
        Assert.assertEquals("Test setup wrong", 0 , val0);
        ClassReplacer cr = new ClassReplacer();
        cr.queueClassForReplacement(BasicTestRep.class, BasicTestRep1.class);
        cr.replaceQueuedClassesWithInstrumentation();
        re = new BasicTestRep();
        val0 = re.value();
        Assert.assertEquals("BasicTestRep was not replaced",1, val0  ) ;

    }

    @Test
    public void testFakeReplaceReplacement() {
        InstTestRep re = new InstTestRep();
        int val0 = re.value();
        Assert.assertEquals( "Test setup wrong", 0, val0);
        ClassReplacer cr = new ClassReplacer();
        cr.queueClassForReplacement(InstTestRep.class, InstTestRep1.class);
        cr.replaceQueuedClasses();
        re = new InstTestRep();
        val0 = re.value();
        Assert.assertEquals("InstTestRep was not replaced", 1,  val0 );

    }

}

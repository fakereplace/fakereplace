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

package org.fakereplace.test.basic.synchronizedtests;

import org.testng.annotations.Test;

import java.lang.reflect.Modifier;

/**
 * This class tests that syncronisation still works as it should
 * <p/>
 * this is because we are going to remove the sync attribute and replace it with
 * the equivilent sync code
 * <p/>
 * this has not actually happended yet, because it is harder than I though, but
 * these tests will be relevant eventually
 *
 * @author stuart
 */
public class SyncronisedTest {

    @Test
    public void testInstanceMethodSyncronisation() {
        InstanceRunnableClass r = new InstanceRunnableClass();
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        while (t1.isAlive() && t2.isAlive()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assert !r.failed;
    }

    @Test
    public void testStaticMethodSyncronisation() {
        StaticRunnableClass r = new StaticRunnableClass();
        Thread t1 = new Thread(r);
        Thread t2 = new Thread(r);
        t1.start();
        t2.start();
        while (t1.isAlive() && t2.isAlive()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assert !StaticRunnableClass.failed;
    }

    @Test
    public void testSyncBitSet() throws SecurityException, NoSuchMethodException {
        assert (StaticRunnableClass.class.getDeclaredMethod("doStuff").getModifiers() & Modifier.SYNCHRONIZED) != 0;
        assert (InstanceRunnableClass.class.getDeclaredMethod("doStuff").getModifiers() & Modifier.SYNCHRONIZED) != 0;
    }
}

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

package a.org.fakereplace.test.replacement.synchronizedtests;

import java.lang.reflect.Modifier;

import org.junit.Assert;
import org.junit.Test;

/**
 * This class tests that syncronisation still works as it should
 * <p>
 * this is because we are going to remove the sync attribute and replace it with
 * the equivilent sync code
 * <p>
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
        Assert.assertFalse(r.failed);
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
        Assert.assertFalse(StaticRunnableClass.failed);
    }

    @Test
    public void testSyncBitSet() throws SecurityException, NoSuchMethodException {
        Assert.assertTrue((StaticRunnableClass.class.getDeclaredMethod("doStuff").getModifiers() & Modifier.SYNCHRONIZED) != 0);
        Assert.assertTrue((InstanceRunnableClass.class.getDeclaredMethod("doStuff").getModifiers() & Modifier.SYNCHRONIZED) != 0);
    }
}

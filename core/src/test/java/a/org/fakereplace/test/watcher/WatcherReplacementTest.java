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

package a.org.fakereplace.test.watcher;

import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

public class WatcherReplacementTest {
    @Test
    public void testInstrumentationReplacement() throws Exception{
        WatcherRep re = new WatcherRep();
        int val0 = re.value();
        Assert.assertEquals("Test setup wrong", 0 , val0);
        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        CtClass nc = pool.get(WatcherRep1.class.getName());

        if (nc.isFrozen()) {
            nc.defrost();
        }
        nc.replaceClassName(WatcherRep1.class.getName(), WatcherRep.class.getName());

        nc.setName(WatcherRep.class.getName());
        byte[] data = nc.toBytecode();
        File file = new File(WatcherRep.class.getClassLoader().getResource(WatcherRep.class.getName().replace(".", "/") + ".class").getFile());
        System.out.println(file);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data);
        }
        long start = System.currentTimeMillis();
        do {
            Thread.sleep(100);
            if(re.value() == 1) {
                break;
            }
        } while (start + 5000 > System.currentTimeMillis());
        Assert.assertEquals("WatcherRep was not replaced", 1, re.value());
        file.delete();

    }
}

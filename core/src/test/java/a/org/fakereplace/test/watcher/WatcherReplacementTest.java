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

package a.org.fakereplace.test.watcher;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Assert;
import org.junit.Test;
import javassist.ClassPool;
import javassist.CtClass;

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

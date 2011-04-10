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

package org.fakereplace.manip;

import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import org.fakereplace.boot.Enviroment;
import org.fakereplace.boot.Logger;
import org.fakereplace.manip.data.StaticFieldAccessRewriteData;
import org.fakereplace.manip.util.ManipulationDataStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StaticFieldManipulator implements ClassManipulator {
    ManipulationDataStore<StaticFieldAccessRewriteData> data = new ManipulationDataStore<StaticFieldAccessRewriteData>();

    public void clearRewrites(String className, ClassLoader classLoader) {
        data.remove(className, classLoader);
    }

    /**
     * rewrites static field access to the same field on another class
     *
     * @param oldClass
     * @param newClass
     * @param fieldName
     */
    public void rewriteStaticFieldAccess(String oldClass, String newClass, String fieldName, ClassLoader classLoader) {
        data.add(oldClass, new StaticFieldAccessRewriteData(oldClass, newClass, fieldName, classLoader));
    }

    public void transformClass(ClassFile file, ClassLoader loader, Enviroment environment) {
        Map<String, Set<StaticFieldAccessRewriteData>> staticMethodData = data.getManipulationData(loader);
        if (staticMethodData.isEmpty()) {
            return;
        }
        Map<Integer, StaticFieldAccessRewriteData> fieldAccessLocations = new HashMap<Integer, StaticFieldAccessRewriteData>();
        Map<StaticFieldAccessRewriteData, Integer> newFieldClassPoolLocations = new HashMap<StaticFieldAccessRewriteData, Integer>();
        Map<StaticFieldAccessRewriteData, Integer> newFieldAccessLocations = new HashMap<StaticFieldAccessRewriteData, Integer>();
        ConstPool pool = file.getConstPool();
        for (int i = 1; i < pool.getSize(); ++i) {
            if (pool.getTag(i) == ConstPool.CONST_Fieldref) {

                String className = pool.getFieldrefClassName(i);
                if (staticMethodData.containsKey(className)) {
                    String fieldName = pool.getFieldrefName(i);
                    for (StaticFieldAccessRewriteData data : staticMethodData.get(className)) {
                        if (fieldName.equals(data.getFieldName())) {
                            fieldAccessLocations.put(i, data);
                            // we have found a field access
                            // now lets replace it
                            if (!newFieldClassPoolLocations.containsKey(data)) {
                                // we have not added the new class reference or
                                // the new call location to the class pool yet
                                int newCpLoc = pool.addClassInfo(data.getNewClass());
                                newFieldClassPoolLocations.put(data, newCpLoc);
                                // we do not need to change the name and type
                                int newNameAndType = pool.getFieldrefNameAndType(i);
                                newFieldAccessLocations.put(data, pool.addFieldrefInfo(newCpLoc, newNameAndType));
                            }
                            break;
                        }

                    }
                }
            }
        }
        // this means we found an instance of the static field access
        if (!newFieldClassPoolLocations.isEmpty()) {
            List<MethodInfo> methods = file.getMethods();
            for (MethodInfo m : methods) {
                try {
                    if (m.getCodeAttribute() == null) {
                        continue;
                    }
                    CodeIterator it = m.getCodeAttribute().iterator();
                    while (it.hasNext()) {
                        int index = it.next();
                        int op = it.byteAt(index);
                        if (op == CodeIterator.GETSTATIC || op == CodeIterator.PUTSTATIC) {
                            int val = it.s16bitAt(index + 1);
                            if (fieldAccessLocations.containsKey(val)) {
                                StaticFieldAccessRewriteData data = fieldAccessLocations.get(val);
                                it.write16bit(newFieldAccessLocations.get(data), index + 1);
                            }
                        }
                    }
                    m.getCodeAttribute().computeMaxStack();
                } catch (Exception e) {
                    Logger.log(this, "Bad byte code transforming " + file.getName());
                    e.printStackTrace();
                }
            }
        }
    }

}

/*
 *
 *  * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 *  * by the @authors tag.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.fakereplace.data;

import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.FieldInfo;

public class ClassDataBuilder {

    private final BaseClassData baseData;

    private final Set<FieldData> fakeFields = new HashSet<FieldData>();

    private final Set<MethodData> fakeMethods = new HashSet<MethodData>();

    private final Set<MethodData> removedMethods = new HashSet<MethodData>();

    private final Set<FieldData> removedFields = new HashSet<FieldData>();

    public ClassDataBuilder(BaseClassData b) {
        if (b == null) {
            throw new RuntimeException("Attempted to created ClassDataBuilder with null BaseClassData");
        }
        baseData = b;
    }

    public ClassData buildClassData() {
        return new ClassData(baseData, fakeMethods, removedMethods, fakeFields, removedFields);
    }

    public BaseClassData getBaseData() {
        return baseData;
    }

    public FieldData addFakeField(FieldInfo newField, String proxyName, int modifiers) {
        FieldData data = new FieldData(newField, MemberType.FAKE, proxyName, modifiers);
        fakeFields.add(data);
        return data;
    }

    public MethodData addFakeMethod(String name, String descriptor, String proxyName, int accessFlags) {
        MethodData data = new MethodData(name, descriptor, proxyName, MemberType.FAKE, accessFlags, false);
        fakeMethods.add(data);
        return data;
    }

    public MethodData addFakeConstructor(String name, String descriptor, String proxyName, int accessFlags, int methodCount) {
        MethodData data = new MethodData(name, descriptor, proxyName, MemberType.FAKE_CONSTRUCTOR, accessFlags, methodCount);
        fakeMethods.add(data);
        return data;
    }

    public void removeRethod(MethodData md) {
        MethodData nmd = new MethodData(md.getMethodName(), md.getDescriptor(), md.getClassName(), MemberType.REMOVED, md.getAccessFlags(), false);
        removedMethods.add(nmd);
    }

    public void removeField(FieldData md) {
        FieldData nd = new FieldData(md, MemberType.REMOVED);
        removedFields.add(nd);
    }

}

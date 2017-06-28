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

package org.fakereplace.data;

import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.FieldInfo;

public class ClassDataBuilder {

    private final BaseClassData baseData;

    private final Set<FieldData> fakeFields = new HashSet<>();

    private final Set<MethodData> fakeMethods = new HashSet<>();

    private final Set<MethodData> removedMethods = new HashSet<>();

    private final Set<FieldData> removedFields = new HashSet<>();

    public ClassDataBuilder(BaseClassData b) {
        if (b == null) {
            throw new RuntimeException("Attempted to created ClassDataBuilder with null BaseClassData");
        }
        baseData = b;
    }
    public ClassDataBuilder(ClassData b, BaseClassData base) {
        if (b == null) {
            throw new RuntimeException("Attempted to created ClassDataBuilder with null BaseClassData");
        }
        baseData = base;
        for(MethodData method : b.getMethods()) {
            if(method.getType() == MemberType.FAKE || method.getType() == MemberType.FAKE_CONSTRUCTOR) {
                fakeMethods.add(method);
            } else if(method.getType() == MemberType.REMOVED) {
                removedMethods.add(method);
            }
        }
        for(FieldData field : b.getFields()) {
            if(field.getMemberType() == MemberType.FAKE) {
                fakeFields.add(field);
            } else if(field.getMemberType() == MemberType.REMOVED) {
                removedFields.add(field);
            }
        }
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

    public void removeMethod(MethodData md) {
        MethodData nmd = new MethodData(md.getMethodName(), md.getDescriptor(), md.getClassName(), MemberType.REMOVED, md.getAccessFlags(), false);
        removedMethods.add(nmd);
    }

    public void removeField(FieldData md) {
        FieldData nd = new FieldData(md, MemberType.REMOVED);
        removedFields.add(nd);
    }

}

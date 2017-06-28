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

import java.lang.reflect.Field;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.FieldInfo;

public class FieldData {
    private final int accessFlags;
    private final boolean priv, pack, prot;
    private final String name;
    private final String type;
    private final MemberType memberType;
    private final String className;

    public FieldData(FieldInfo info, MemberType memberType, String className, int modifiers) {
        this.accessFlags = modifiers;
        this.pack = AccessFlag.isPackage(modifiers);
        this.priv = AccessFlag.isPrivate(modifiers);
        this.prot = AccessFlag.isProtected(modifiers);
        this.type = info.getDescriptor();
        this.name = info.getName();
        this.className = className;
        this.memberType = memberType;
    }

    public FieldData(Field field) {
        this.accessFlags = field.getModifiers();
        this.pack = AccessFlag.isPackage(accessFlags);
        this.priv = AccessFlag.isPrivate(accessFlags);
        this.prot = AccessFlag.isProtected(accessFlags);
        this.type = field.getType().getName();
        this.memberType = MemberType.NORMAL;
        this.className = field.getDeclaringClass().getName();
        this.name = field.getName();
    }

    public FieldData(FieldData other, MemberType type) {
        this.accessFlags = other.accessFlags;
        this.pack = other.pack;
        this.priv = other.priv;
        this.prot = other.prot;
        this.type = other.type;
        this.name = other.name;
        this.className = other.className;
        this.memberType = type;
    }

    /**
     * FieldData's are equal if they refer to the same field
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof FieldData) {
            FieldData m = (FieldData) obj;
            if (m.className.equals(className)) {
                if (m.name.equals(name)) {
                    if (m.type.equals(type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (className + name).hashCode();
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getClassName() {
        return className;
    }

    public MemberType getMemberType() {
        return memberType;
    }

    public Field getField(Class<?> actualClass) throws SecurityException, NoSuchFieldException {

        return actualClass.getDeclaredField(name);
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public boolean isPriv() {
        return priv;
    }

    public boolean isPack() {
        return pack;
    }

    public boolean isProt() {
        return prot;
    }

}

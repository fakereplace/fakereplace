package org.fakereplace.data;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.FieldInfo;

import java.lang.reflect.Field;

public class FieldData {
    final private int accessFlags;
    final private boolean priv, pack, prot;
    final private String name;
    final private String type;
    final private MemberType memberType;
    final private String className;

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

    public Field getField(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchFieldException {

        Field method = actualClass.getDeclaredField(name);
        return method;
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

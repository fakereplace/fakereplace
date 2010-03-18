package org.fakereplace.data;

import java.lang.reflect.Field;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.FieldInfo;

public class FieldData
{
   final int accessFlags;
   final boolean priv, pack, prot;
   final String name;
   final String type;
   final MemberType memberType;
   final String className;

   public FieldData(FieldInfo info, MemberType memberType, String className)
   {
      accessFlags = info.getAccessFlags();
      pack = AccessFlag.isPackage(accessFlags);
      priv = AccessFlag.isPrivate(accessFlags);
      prot = AccessFlag.isProtected(accessFlags);
      type = info.getDescriptor();
      name = info.getName();
      this.className = className;
      this.memberType = memberType;
   }

   public String getName()
   {
      return name;
   }

   public String getType()
   {
      return type;
   }

   public String getClassName()
   {
      return className;
   }

   public MemberType getMemberType()
   {
      return memberType;
   }

   public Field getField(Class<?> actualClass) throws ClassNotFoundException, SecurityException, NoSuchFieldException
   {

      Field method = actualClass.getDeclaredField(name);
      return method;
   }

   public int getAccessFlags()
   {
      return accessFlags;
   }

}

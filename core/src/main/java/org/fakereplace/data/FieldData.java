package org.fakereplace.data;

import java.lang.reflect.Field;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.FieldInfo;

public class FieldData
{
   int accessFlags;
   boolean priv, pack, prot;
   String name;
   String type;
   MemberType memberType;

   public FieldData(FieldInfo info, MemberType memberType)
   {
      accessFlags = info.getAccessFlags();
      pack = AccessFlag.isPackage(accessFlags);
      priv = AccessFlag.isPrivate(accessFlags);
      prot = AccessFlag.isProtected(accessFlags);
      type = info.getDescriptor();
      name = info.getName();
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

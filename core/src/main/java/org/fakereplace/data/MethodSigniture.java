package org.fakereplace.data;

import org.fakereplace.util.DescriptorUtils;

/**
 * This class represents the identity of a method. If can be referenced both via
 * a descriptor or a normal signature. The method signature can either be
 * ambiguous or non-ambiguous
 * 
 * Ambiguous signatures do not have return value information, they should not be
 * used as hashmap keys
 * 
 * @author Jess
 * 
 */
public class MethodSigniture
{

   String parameters;
   String returnType;
   String name;
   String ambiguousString;
   String nonAmbiguousString;

   public MethodSigniture(String name, Class returnType, Class... parameters)
   {
      this.name = name;
      this.returnType = DescriptorUtils.classToStringRepresentation(returnType);
      this.parameters = DescriptorUtils.classArrayToDescriptorString(parameters);
      this.ambiguousString = name + "+" + this.parameters;
      this.nonAmbiguousString = name + "+" + this.parameters + "+" + this.returnType;
   }

   public MethodSigniture(String name, Class... parameters)
   {
      this.name = name;

      this.parameters = DescriptorUtils.classArrayToDescriptorString(parameters);
      this.ambiguousString = name + "+" + this.parameters;
      this.nonAmbiguousString = null;
   }

   public MethodSigniture(String name, String descriptor)
   {
      int closePos = descriptor.indexOf(')');
      this.parameters = descriptor.substring(1, closePos);
      this.name = name;
      this.returnType = descriptor.substring(closePos + 1);
      this.ambiguousString = name + "+" + this.parameters;
      this.nonAmbiguousString = name + "+" + this.parameters + "+" + this.returnType;
   }

   public boolean isAmbiguous()
   {
      return nonAmbiguousString == null;
   }

   @Override
   public String toString()
   {
      if (isAmbiguous())
         return ambiguousString;
      return nonAmbiguousString;
   }

   /**
    * ambiguous and non ambiguous descriptions of the same method should have
    * the same hashcode
    */
   @Override
   public int hashCode()
   {
      return ambiguousString.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (MethodSigniture.class.isAssignableFrom(obj.getClass()))
      {
         MethodSigniture ms = (MethodSigniture) obj;
         if (isAmbiguous() || ms.isAmbiguous())
         {
            return ambiguousString.equals(ms.ambiguousString);
         }
         return nonAmbiguousString.equals(ms.nonAmbiguousString);
      }
      return false;
   }
}

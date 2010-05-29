package org.fakereplace.manip.data;

public class StaticFieldAccessRewriteData
{
   final String oldClass;
   final String newClass;
   final String fieldName;
   final ClassLoader classLoader;

   public StaticFieldAccessRewriteData(String oldClass, String newClass, String fieldName, ClassLoader classLoader)
   {
      this.oldClass = oldClass;
      this.newClass = newClass;
      this.fieldName = fieldName;
      this.classLoader = classLoader;
   }

   public String getOldClass()
   {
      return oldClass;
   }

   public String getNewClass()
   {
      return newClass;
   }

   public String getFieldName()
   {
      return fieldName;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append(oldClass);
      sb.append(" ");
      sb.append(newClass);
      sb.append(" ");
      sb.append(fieldName);

      return sb.toString();
   }

   public boolean equals(Object o)
   {
      if (o.getClass().isAssignableFrom(StaticFieldAccessRewriteData.class))
      {
         StaticFieldAccessRewriteData i = (StaticFieldAccessRewriteData) o;
         return oldClass.equals(i.oldClass) && newClass.equals(i.newClass) && fieldName.equals(i.fieldName);
      }
      return false;
   }

   public int hashCode()
   {
      return toString().hashCode();
   }
}
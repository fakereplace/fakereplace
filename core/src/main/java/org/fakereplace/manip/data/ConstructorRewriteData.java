/**
 * 
 */
package org.fakereplace.manip.data;

import org.fakereplace.util.DescriptorUtils;

public class ConstructorRewriteData
{
   final private String clazz;
   final private String methodDesc;
   final private String[] parameters;
   final private int methodNo;
   final private ClassLoader classLoader;

   public ConstructorRewriteData(String clazz, String methodDesc, int methodNo, ClassLoader classLoader)
   {
      this.clazz = clazz;
      this.methodDesc = methodDesc;
      this.methodNo = methodNo;
      parameters = DescriptorUtils.descriptorStringToParameterArray(methodDesc);
      this.classLoader = classLoader;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder(ConstructorRewriteData.class.getName() + " ");
      sb.append(clazz);
      sb.append(" ");
      sb.append(methodDesc);
      sb.append(" ");
      sb.append(methodNo);

      return sb.toString();
   }

   public int hashCode()
   {
      return toString().hashCode();
   }

   public String getClazz()
   {
      return clazz;
   }

   public String getMethodDesc()
   {
      return methodDesc;
   }

   public String[] getParameters()
   {
      return parameters;
   }

   public int getMethodNo()
   {
      return methodNo;
   }

   public ClassLoader getClassLoader()
   {
      return classLoader;
   }
}
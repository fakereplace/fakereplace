package org.fakereplace.manip;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;

import org.fakereplace.boot.Logger;

public class StaticFieldManipulator
{

   Map<String, Set<StaticFieldAccessRewriteData>> staticMethodData = Collections.synchronizedMap(new HashMap<String, Set<StaticFieldAccessRewriteData>>());

   public void clearRewrite(String className)
   {
      staticMethodData.remove(className);
   }

   /**
    * rewrites static field access to the same field on another class
    * 
    * @param oldClass
    * @param newClass
    * @param fieldName
    */
   public void rewriteStaticFieldAccess(String oldClass, String newClass, String fieldName)
   {
      Set<StaticFieldAccessRewriteData> d = staticMethodData.get(oldClass);
      if (d == null)
      {
         d = new HashSet<StaticFieldAccessRewriteData>();
         staticMethodData.put(oldClass, d);
      }
      d.add(new StaticFieldAccessRewriteData(oldClass, newClass, fieldName));
   }

   public void transformClass(ClassFile file)
   {
      Map<Integer, StaticFieldAccessRewriteData> fieldAccessLocations = new HashMap<Integer, StaticFieldAccessRewriteData>();
      Map<StaticFieldAccessRewriteData, Integer> newFieldClassPoolLocations = new HashMap<StaticFieldAccessRewriteData, Integer>();
      Map<StaticFieldAccessRewriteData, Integer> newFieldAccessLocations = new HashMap<StaticFieldAccessRewriteData, Integer>();
      ConstPool pool = file.getConstPool();
      for (int i = 1; i < pool.getSize(); ++i)
      {
         if (pool.getTag(i) == ConstPool.CONST_Fieldref)
         {

            String className = pool.getFieldrefClassName(i);
            if (staticMethodData.containsKey(className))
            {
               String fieldName = pool.getFieldrefName(i);
               for (StaticFieldAccessRewriteData data : staticMethodData.get(className))
               {
                  if (fieldName.equals(data.fieldName))
                  {
                     fieldAccessLocations.put(i, data);
                     // we have found a field access
                     // now lets replace it
                     if (!newFieldClassPoolLocations.containsKey(data))
                     {
                        // we have not added the new class reference or
                        // the new call location to the class pool yet
                        int newCpLoc = pool.addClassInfo(data.newClass);
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
      if (!newFieldClassPoolLocations.isEmpty())
      {
         List<MethodInfo> methods = file.getMethods();
         for (MethodInfo m : methods)
         {
            try
            {
               if (m.getCodeAttribute() == null)
               {
                  continue;
               }
               CodeIterator it = m.getCodeAttribute().iterator();
               while (it.hasNext())
               {
                  int index = it.next();
                  int op = it.byteAt(index);
                  if (op == CodeIterator.GETSTATIC || op == CodeIterator.PUTSTATIC)
                  {
                     int val = it.s16bitAt(index + 1);
                     if (fieldAccessLocations.containsKey(val))
                     {
                        StaticFieldAccessRewriteData data = fieldAccessLocations.get(val);
                        it.write16bit(newFieldAccessLocations.get(data), index + 1);
                     }
                  }
               }
               m.getCodeAttribute().computeMaxStack();
            }
            catch (Exception e)
            {
               Logger.log(this, "Bad byte code transforming " + file.getName());
               e.printStackTrace();
            }
         }
      }
   }

   static private class StaticFieldAccessRewriteData
   {
      final String oldClass;
      final String newClass;
      final String fieldName;

      public StaticFieldAccessRewriteData(String oldClass, String newClass, String fieldName)
      {
         this.oldClass = oldClass;
         this.newClass = newClass;
         this.fieldName = fieldName;
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

}

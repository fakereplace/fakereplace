package org.fakereplace.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javassist.bytecode.FieldInfo;

/**
 * This class holds everything there is to know about a class that has been seen
 * by the transformer
 * 
 * @author stuart
 * 
 */
public class ClassData
{

   String className;
   String internalName;
   Map<String, Map<String, Set<MethodData>>> methods = new ConcurrentHashMap<String, Map<String, Set<MethodData>>>();
   Map<String, FieldData> fields = new ConcurrentHashMap<String, FieldData>();
   ClassLoader loader;
   String superClassName;
   boolean superClassInfoInit = false;
   ClassData superClassInformation;

   boolean signitureModified = false;

   public boolean isSignitureModified()
   {
      return signitureModified;
   }

   public void setSignitureModified(boolean structuralModification)
   {
      this.signitureModified = structuralModification;
   }

   /**
    * Searches through parent classloaders of the classes class loader to find
    * the ClassData structure for the super class
    * 
    * @return
    */
   public ClassData getSuperClassInformation()
   {
      if (!superClassInfoInit)
      {
         superClassInformation = ClassDataStore.getClassData(loader, superClassName);
         ClassLoader l = loader;
         while (superClassInformation == null && l != null)
         {
            l = l.getParent();
            superClassInformation = ClassDataStore.getClassData(l, superClassName);
         }

         superClassInfoInit = true;
      }

      return superClassInformation;
   }

   public FieldData getField(String field)
   {
      return fields.get(field);
   }

   public String getSuperClassName()
   {
      return superClassName;
   }

   public void setSuperClassName(String superClassName)
   {
      this.superClassName = superClassName;
   }

   public ClassLoader getLoader()
   {
      return loader;
   }

   public void setLoader(ClassLoader loader)
   {
      this.loader = loader;
   }

   public String getClassName()
   {
      return className;
   }

   public void setClassName(String className)
   {
      this.className = className;
   }

   public String getInternalName()
   {
      return internalName;
   }

   public void setInternalName(String internalName)
   {
      this.internalName = internalName;
   }

   public void addMethod(MethodData data)
   {

      if (!methods.containsKey(data.getMethodName()))
      {
         methods.put(data.getMethodName(), new HashMap<String, Set<MethodData>>());
      }
      Map<String, Set<MethodData>> mts = methods.get(data.getMethodName());
      if (!mts.containsKey(data.getArgumentDescriptor()))
      {
         mts.put(data.getArgumentDescriptor(), new HashSet<MethodData>());
      }
      Set<MethodData> rr = mts.get(data.getArgumentDescriptor());
      rr.add(data);

   }

   /**
    * replaces a method if it already exists
    * @param data
    */
   public void replaceMethod(MethodData data)
   {
      if (!methods.containsKey(data.getMethodName()))
      {
         methods.put(data.getMethodName(), new HashMap<String, Set<MethodData>>());
      }
      Map<String, Set<MethodData>> mts = methods.get(data.getMethodName());
      Set<MethodData> rr = new HashSet<MethodData>();
      mts.put(data.getArgumentDescriptor(), rr);
      rr.add(data);

   }

   public void addField(FieldInfo field, MemberType type, String className)
   {
      fields.put(field.getName(), new FieldData(field, type, className));
   }

   public Collection<MethodData> getMethods()
   {

      Set<MethodData> results = new HashSet<MethodData>();
      for (String nm : methods.keySet())
      {

         for (String i : methods.get(nm).keySet())
         {
            results.addAll(methods.get(nm).get(i));
         }
      }
      return results;
   }

   public Collection<FieldData> getFields()
   {
      return fields.values();
   }

   /**
    * gets the method data based on name and signiture. If there is multiple
    * methods with the same name and signiture it is undefined which one will be
    * returened
    * 
    * @param name
    * @param arguments
    * @return
    */
   public MethodData getMethodData(String name, String arguments)
   {
      Map<String, Set<MethodData>> r = methods.get(name);
      if (r == null)
      {
         return null;
      }
      Set<MethodData> ms = r.get(arguments);

      if (ms == null)
      {
         return null;
      }
      return ms.iterator().next();
   }

   public void clearReplacements()
   {
      for (Map<String, Set<MethodData>> i : methods.values())
      {
         for (Set<MethodData> j : i.values())
         {
            Iterator<MethodData> it = j.iterator();
            while (it.hasNext())
            {
               MethodData m = it.next();
               if (m.getType() == MemberType.FAKE)
               {
                  it.remove();
               }
            }
         }
      }
      Iterator<Entry<String, FieldData>> it = fields.entrySet().iterator();
      while (it.hasNext())
      {
         Entry<String, FieldData> e = it.next();
         if (e.getValue().getMemberType() == MemberType.FAKE)
         {
            it.remove();
         }
      }
   }

}

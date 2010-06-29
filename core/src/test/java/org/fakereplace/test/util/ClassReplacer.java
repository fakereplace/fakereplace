package org.fakereplace.test.util;

import java.lang.instrument.ClassDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javassist.ClassPool;
import javassist.CtClass;

import org.fakereplace.Agent;
import org.fakereplace.replacement.AddedClass;

public class ClassReplacer
{
   Map<String, String> nameReplacements = new HashMap<String, String>();

   Map<Class<?>, Class<?>> queuedClassReplacements = new HashMap<Class<?>, Class<?>>();

   Map<Class<?>, String> addedClasses = new HashMap<Class<?>, String>();

   ClassPool pool = new ClassPool();

   public ClassReplacer()
   {
      pool.appendSystemPath();
   }

   public void queueClassForReplacement(Class<?> oldClass, Class<?> newClass)
   {
      queuedClassReplacements.put(oldClass, newClass);
   }

   public void addNewClass(Class<?> definition, String name)
   {
      addedClasses.put(definition, name);
   }

   public void replaceQueuedClasses()
   {
      replaceQueuedClasses(true);
   }

   public void replaceQueuedClassesWithInstrumentation()
   {
      replaceQueuedClasses(false);
   }

   public void replaceQueuedClasses(boolean useFakereplace)
   {
      try
      {
         ClassDefinition[] definitions = new ClassDefinition[queuedClassReplacements.size()];
         AddedClass[] newClasses = new AddedClass[addedClasses.size()];
         for (Class<?> o : queuedClassReplacements.keySet())
         {
            Class<?> n = queuedClassReplacements.get(o);
            String newName = o.getName();
            String oldName = n.getName();
            nameReplacements.put(oldName, newName);
         }

         for (Entry<Class<?>, String> o : addedClasses.entrySet())
         {
            nameReplacements.put(o.getKey().getName(), o.getValue());
         }
         int count = 0;
         for (Class<?> o : queuedClassReplacements.keySet())
         {
            Class<?> n = queuedClassReplacements.get(o);
            CtClass nc = pool.get(n.getName());

            if (nc.isFrozen())
            {
               nc.defrost();
            }

            for (String oldName : nameReplacements.keySet())
            {
               String newName = nameReplacements.get(oldName);
               nc.replaceClassName(oldName, newName);
            }
            nc.setName(o.getName());
            ClassDefinition cd = new ClassDefinition(o, nc.toBytecode());
            definitions[count++] = cd;
         }
         count = 0;
         for (Entry<Class<?>, String> o : addedClasses.entrySet())
         {
            CtClass nc = pool.get(o.getKey().getName());

            if (nc.isFrozen())
            {
               nc.defrost();
            }

            for (String newName : nameReplacements.keySet())
            {
               String oldName = nameReplacements.get(newName);
               nc.replaceClassName(newName, oldName);
            }
            AddedClass ncd = new AddedClass(o.getValue(), nc.toBytecode(), o.getKey().getClassLoader());
            newClasses[count++] = ncd;
         }

         if (useFakereplace)
         {
            Agent.redefine(definitions, newClasses);
         }
         else
         {
            Agent.getInstrumentation().redefineClasses(definitions);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}

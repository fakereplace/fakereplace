package org.fakereplace.test.util;

import java.lang.instrument.ClassDefinition;
import java.util.HashMap;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;

import org.fakereplace.Agent;

public class ClassReplacer
{
   Map<String, String> nameReplacements = new HashMap<String, String>();

   Map<Class, Class> queuedClassReplacements = new HashMap<Class, Class>();

   ClassPool pool = ClassPool.getDefault();

   public void queueClassForReplacement(Class oldClass, Class newClass)
   {
      queuedClassReplacements.put(oldClass, newClass);
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
         for (Class<?> o : queuedClassReplacements.keySet())
         {
            Class<?> n = queuedClassReplacements.get(o);
            String oldName = o.getName();
            String newName = n.getName();
            nameReplacements.put(newName, oldName);
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

            for (String newName : nameReplacements.keySet())
            {
               String oldName = nameReplacements.get(newName);
               nc.replaceClassName(newName, oldName);
            }
            nc.setName(o.getName());
            ClassDefinition cd = new ClassDefinition(o, nc.toBytecode());
            definitions[count++] = cd;
         }
         if (useFakereplace)
         {
            Agent.redefine(definitions);
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

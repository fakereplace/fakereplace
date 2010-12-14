package org.fakereplace;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Set;

import javassist.bytecode.ClassFile;

import org.fakereplace.api.ClassChangeNotifier;
import org.fakereplace.api.IntegrationInfo;
import org.fakereplace.boot.Enviroment;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.classloading.ClassLookupManager;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.replacement.AddedClass;
import org.fakereplace.replacement.ClassRedefiner;

/**
 * The agent entry point.
 * 
 * @author stuart
 * 
 */
public class Agent
{

   static Instrumentation inst;

   static Enviroment environment;

   public static void premain(java.lang.String s, java.lang.instrument.Instrumentation i)
   {
      Set<IntegrationInfo> integrationInfo = IntegrationLoader.getIntegrationInfo(ClassLoader.getSystemClassLoader());
      inst = i;
      environment = new Enviroment();
      inst.addTransformer(new Transformer(i, integrationInfo, environment));
   }

   static public void redefine(ClassDefinition[] classes, AddedClass[] addedData) throws UnmodifiableClassException, ClassNotFoundException
   {
      final ClassIdentifier[] addedClass = new ClassIdentifier[addedData.length];
      int count = 0;
      for (AddedClass i : addedData)
      {
         addedClass[count++] = i.getClassIdentifier();
      }

      final Class<?>[] changedClasses = new Class<?>[classes.length];
      count = 0;
      for (ClassDefinition i : classes)
      {
         changedClasses[count++] = i.getDefinitionClass();
         ClassDataStore.markClassReplaced(i.getClass());
      }
      // notify the integration classes that stuff is about to change
      ClassChangeNotifier.beforeChange(changedClasses, addedClass);
      // re-write the classes so their field
      ClassDefinition[] modifiedClasses = ClassRedefiner.rewriteLoadedClasses(classes);
      try
      {
         for (AddedClass c : addedData)
         {
            ClassLookupManager.addClassInfo(c.getClassName(), c.getLoader(), c.getData());
         }
         inst.redefineClasses(modifiedClasses);

         Introspector.flushCaches();

         ClassChangeNotifier.notify(changedClasses, addedClass);
      }
      catch (Throwable e)
      {
         try
         {
            // dump the classes to /tmp so we can look at them
            for (ClassDefinition d : modifiedClasses)
            {
               try
               {
                  ByteArrayInputStream bin = new ByteArrayInputStream(d.getDefinitionClassFile());
                  DataInputStream dis = new DataInputStream(bin);
                  ClassFile file = new ClassFile(dis);

                  Transformer.getManipulator().transformClass(file, d.getDefinitionClass().getClassLoader(), environment);
                  ByteArrayOutputStream bos = new ByteArrayOutputStream();
                  DataOutputStream dos = new DataOutputStream(bos);
                  file.write(dos);
                  dos.close();

                  String dumpDir = environment.getDumpDirectory();
                  if (dumpDir == null)
                  {
                     dumpDir = "/tmp";
                  }
                  FileOutputStream s = new FileOutputStream(dumpDir + '/' + d.getDefinitionClass().getName() + "1.class");
                  dos = new DataOutputStream(s);
                  file.write(dos);
                  dos.flush();
                  dos.close();
                  // s.write(d.getDefinitionClassFile());
                  s.close();
               }
               catch (IOException a)
               {
                  a.printStackTrace();
               }
            }
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
         }
         throw (new RuntimeException(e));
      }
   }

   public static Instrumentation getInstrumentation()
   {
      return inst;
   }

}

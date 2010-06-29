package org.fakereplace;

import java.io.ByteArrayInputStream;
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

      ClassIdentifier[] addedClass = new ClassIdentifier[addedData.length];
      int count = 0;
      for (AddedClass i : addedData)
      {
         addedClass[count++] = i.getClassIdentifier();
      }
      Class<?>[] changedClasses = new Class<?>[classes.length];
      count = 0;
      for (ClassDefinition i : classes)
      {
         changedClasses[count++] = i.getDefinitionClass();
      }

      ClassChangeNotifier.beforeChange(changedClasses, addedClass);

      ClassDefinition[] modifiedClasses = ClassRedefiner.rewriteLoadedClasses(classes);
      try
      {
         for (AddedClass c : addedData)
         {
            ClassLookupManager.addClassInfo(c.getClassName(), c.getLoader(), c.getData());
         }
         inst.redefineClasses(modifiedClasses);

         ClassChangeNotifier.notify(changedClasses, addedClass);
      }
      catch (Throwable e)
      {
         // dump the classes to /tmp so we can look at them
         for (ClassDefinition d : modifiedClasses)
         {
            try
            {
               DataInputStream ds = new DataInputStream(new ByteArrayInputStream(d.getDefinitionClassFile()));
               ClassFile file = new ClassFile(ds);
               Transformer.getManipulator().transformClass(file, d.getDefinitionClass().getClassLoader());
               String dumpDir = environment.getDumpDirectory();
               if (dumpDir == null)
               {
                  dumpDir = "/tmp";
               }
               FileOutputStream s = new FileOutputStream(dumpDir + '/' + d.getDefinitionClass().getName() + "1.class");
               DataOutputStream dos = new DataOutputStream(s);
               file.write(dos);
               s.write(d.getDefinitionClassFile());
               s.close();
            }
            catch (IOException a)
            {
               a.printStackTrace();
            }
         }
         throw (new RuntimeException(e));
      }
   }

   public static Instrumentation getInstrumentation()
   {
      return inst;
   }

}

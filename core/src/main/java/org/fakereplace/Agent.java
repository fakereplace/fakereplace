package org.fakereplace;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.ClassFile;

import org.fakereplace.replacement.ClassRedefiner;

/**
 * The agent entry point
 * 
 * @author stuart
 * 
 */
public class Agent
{

   static Instrumentation inst;

   static Set<Class> classes = new HashSet<Class>();

   static public Map<ClassLoader, List<byte[]>> classesToLoad = new HashMap<ClassLoader, List<byte[]>>();

   public static void premain(java.lang.String s, java.lang.instrument.Instrumentation i)
   {
      inst = i;
      inst.addTransformer(new Transformer(i));
   }

   static public void redefine(ClassDefinition... classes) throws UnmodifiableClassException, ClassNotFoundException
   {
      ClassDefinition[] modifiedClasses = ClassRedefiner.rewriteLoadedClasses(classes);
      try
      {
         inst.redefineClasses(modifiedClasses);
      }
      catch (VerifyError e)
      {
         // dump the classes to /tmp so we can look at them
         for (ClassDefinition d : modifiedClasses)
         {
            try
            {
               DataInputStream ds = new DataInputStream(new ByteArrayInputStream(d.getDefinitionClassFile()));
               ClassFile file = new ClassFile(ds);
               Transformer.getManipulator().transformClass(file);
               FileOutputStream s = new FileOutputStream("/tmp/" + d.getDefinitionClass().getName() + ".class");
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
         throw (e);
      }
   }

   public static Instrumentation getInstrumentation()
   {
      return inst;
   }

}

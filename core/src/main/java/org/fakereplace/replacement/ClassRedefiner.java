package org.fakereplace.replacement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;

import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;

import org.fakereplace.boot.Logger;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;

public class ClassRedefiner
{
   static public ClassDefinition[] rewriteLoadedClasses(ClassDefinition... classDefinitions)
   {
      ClassDefinition[] ret = new ClassDefinition[classDefinitions.length];
      for (int i = 0; i < classDefinitions.length; ++i)
      {
         try
         {
            ClassDefinition d = classDefinitions[i];
            ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(d.getDefinitionClassFile())));
            modifyReloadedClass(file, d.getDefinitionClass().getClassLoader(), d.getDefinitionClass());
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            file.write(new DataOutputStream(bs));

            ClassDefinition n = new ClassDefinition(d.getDefinitionClass(), bs.toByteArray());
            ret[i] = n;
         }
         catch (IOException e)
         {
            Logger.log(ClassRedefiner.class, "IO Error");
         }
      }
      return ret;

   }

   public static void modifyReloadedClass(ClassFile file, ClassLoader loader, Class oldClass)
   {
      ClassData data = ClassDataStore.getClassData(loader, Descriptor.toJvmName(file.getName()));
      data.clearReplacements();
      // deal with class level annotations
      AnnotationReplacer.processAnnotations(file, oldClass);

      FieldReplacer.handleFieldReplacement(file, loader, oldClass);
      MethodReplacer.handleMethodReplacement(file, loader, oldClass);

   }

}

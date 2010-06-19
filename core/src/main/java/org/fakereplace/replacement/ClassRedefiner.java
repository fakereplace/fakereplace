package org.fakereplace.replacement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;

import org.fakereplace.boot.Logger;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataBuilder;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.util.FileReader;

public class ClassRedefiner
{
   static public ClassDefinition[] rewriteLoadedClasses(ClassDefinition... classDefinitions)
   {
      Set<ClassDefinition> defs = new HashSet<ClassDefinition>();
      Set<Class<?>> classesToReload = new HashSet<Class<?>>();
      for (int i = 0; i < classDefinitions.length; ++i)
      {
         try
         {
            ClassDefinition d = classDefinitions[i];
            ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(d.getDefinitionClassFile())));
            modifyReloadedClass(file, d.getDefinitionClass().getClassLoader(), d.getDefinitionClass(), classesToReload);
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            file.write(new DataOutputStream(bs));

            ClassDefinition n = new ClassDefinition(d.getDefinitionClass(), bs.toByteArray());
            defs.add(n);
         }
         catch (IOException e)
         {
            Logger.log(ClassRedefiner.class, "IO Error");
         }
      }
      for (Class<?> c : classesToReload)
      {
         try
         {
            // TODO: do this properly, test if they are availible on load and if
            // not store the bytes
            InputStream res = c.getClassLoader().getResourceAsStream(c.getName().replace('.', '/') + ".class");
            byte[] data = FileReader.readFileBytes(res);
            ClassDefinition n = new ClassDefinition(c, data);
            defs.add(n);
         }
         catch (Exception e)
         {

            System.out.println("COULD NOT LOAD CLASS DEF: " + c.getName());
            e.printStackTrace();
         }
      }
      ClassDefinition[] ret = new ClassDefinition[defs.size()];
      int count = 0;
      for (ClassDefinition c : defs)
      {
         ret[count++] = c;
      }
      return ret;

   }

   public static void modifyReloadedClass(ClassFile file, ClassLoader loader, Class<?> oldClass, Set<Class<?>> classToReload)
   {
      BaseClassData b = ClassDataStore.getBaseClassData(loader, Descriptor.toJvmName(file.getName()));
      if (b == null)
      {
         throw new RuntimeException("BaseData is null for " + file.getName());
      }
      ClassDataBuilder builder = new ClassDataBuilder(b);
      AnnotationReplacer.processAnnotations(file, oldClass, builder);

      FieldReplacer.handleFieldReplacement(file, loader, oldClass, builder);
      MethodReplacer.handleMethodReplacement(file, loader, oldClass, builder, classToReload);
      ClassDataStore.saveClassData(loader, file.getName(), builder);
   }

}

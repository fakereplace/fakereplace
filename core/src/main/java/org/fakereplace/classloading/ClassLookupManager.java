package org.fakereplace.classloading;

import java.util.Map;

import org.fakereplace.Transformer;
import org.fakereplace.boot.Constants;
import org.fakereplace.boot.ProxyDefinitionStore;

import com.google.common.collect.MapMaker;

/**
 * this class is resposible for serving up classes to instrumented ClassLoaders
 * 
 * @author stuart
 * 
 */
public class ClassLookupManager
{
   private static Map<ClassInfo, byte[]> classData = new MapMaker().makeMap();

   public static byte[] getClassData(String className, ClassLoader loader)
   {
      if (className.startsWith(Constants.GENERATED_CLASS_PACKAGE))
      {
         return ProxyDefinitionStore.getProxyDefinition(loader, className);
      }
      if (className.startsWith("org.fakereplace.integration"))
      {
         return Transformer.getIntegrationClass(loader, className);
      }
      return classData.get(new ClassInfo(className, loader));
   }

   public static void addClassInfo(String className, ClassLoader loader, byte[] data)
   {
      classData.put(new ClassInfo(className, loader), data);
   }

   private static class ClassInfo
   {
      private final String className;
      private final ClassLoader loader;

      public ClassInfo(String className, ClassLoader loader)
      {
         this.className = className;
         this.loader = loader;
      }

      public String getClassName()
      {
         return className;
      }

      public ClassLoader getLoader()
      {
         return loader;
      }

      @Override
      public int hashCode()
      {
         return className.hashCode();
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof ClassInfo)
         {
            ClassInfo c = (ClassInfo) obj;
            return c.loader == loader && c.className.equals(className);
         }
         return false;
      }

   }
}

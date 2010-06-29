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
   private static Map<ClassIdentifier, byte[]> classData = new MapMaker().makeMap();

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
      return classData.get(new ClassIdentifier(className, loader));
   }

   public static void addClassInfo(String className, ClassLoader loader, byte[] data)
   {
      classData.put(new ClassIdentifier(className, loader), data);
   }
}

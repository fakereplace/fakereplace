package org.fakereplace.manip;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javassist.bytecode.ClassFile;

/**
 * Class that maintains a set of manipulations to apply to classes
 * 
 * @author stuart
 * 
 */
public class Manipulator
{

   Map<String, String> renames = Collections.synchronizedMap(new HashMap<String, String>());
   MethodInvokationManipulator methodInvokationManipulator = new MethodInvokationManipulator();
   StaticFieldManipulator staticFieldManipulator = new StaticFieldManipulator();

   public void removeRewrites(String className)
   {
      methodInvokationManipulator.removeMethodRewrites(className);
   }

   public void renameClass(String oldName, String newName)
   {
      renames.put(oldName, newName);
   }

   /**
    * rewrites static field access to the same field on another class
    * 
    * @param oldClass
    * @param newClass
    * @param fieldName
    */
   public void rewriteStaticFieldAccess(String oldClass, String newClass, String fieldName)
   {
      staticFieldManipulator.rewriteStaticFieldAccess(oldClass, newClass, fieldName);
   }

   /**
    * This can also be used to replace a static invokation with another static
    * invokation
    * 
    * @param oldClass
    * @param newClass
    * @param methodName
    * @param methodDesc
    * @param newStaticMethodDesc
    */
   public void replaceVirtualMethodInvokationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc)
   {
      methodInvokationManipulator.replaceVirtualMethodInvokationWithStatic(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc);
   }

   public void transformClass(ClassFile file)
   {
      // first we are going to transform virtual method calls to static ones
      methodInvokationManipulator.transformClass(file);
      staticFieldManipulator.transformClass(file);

      file.renameClass(renames);

   }

}

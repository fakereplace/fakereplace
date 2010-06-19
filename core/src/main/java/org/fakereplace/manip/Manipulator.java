package org.fakereplace.manip;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javassist.bytecode.ClassFile;

import org.fakereplace.manip.data.AddedFieldData;

/**
 * Class that maintains a set of manipulations to apply to classes
 * 
 * @author stuart
 * 
 */
public class Manipulator
{

   final private MethodInvokationManipulator methodInvokationManipulator = new MethodInvokationManipulator();
   final private StaticFieldManipulator staticFieldManipulator = new StaticFieldManipulator();
   final private InstanceFieldManipulator instanceFieldManapulator = new InstanceFieldManipulator();
   final private ConstructorInvocationManipulator constructorInvocationManipulator = new ConstructorInvocationManipulator();
   final private SubclassVirtualCallManipulator subclassVirtualCallManilulator = new SubclassVirtualCallManipulator();

   final private Set<ClassManipulator> manipulators = new CopyOnWriteArraySet<ClassManipulator>();

   public Manipulator()
   {
      manipulators.add(methodInvokationManipulator);
      manipulators.add(staticFieldManipulator);
      manipulators.add(instanceFieldManapulator);
      manipulators.add(constructorInvocationManipulator);
      manipulators.add(subclassVirtualCallManilulator);
   }

   public void removeRewrites(String className, ClassLoader classLoader)
   {
      for (ClassManipulator m : manipulators)
      {
         m.clearRewrites(className, classLoader);
      }
   }

   /**
    * rewrites static field access to the same field on another class
    * 
    * @param oldClass
    * @param newClass
    * @param fieldName
    */
   public void rewriteStaticFieldAccess(String oldClass, String newClass, String fieldName, ClassLoader classLoader)
   {
      staticFieldManipulator.rewriteStaticFieldAccess(oldClass, newClass, fieldName, classLoader);
   }

   public void rewriteConstructorAccess(String clazz, String descriptor, int methodNo, ClassLoader classLoader)
   {
      constructorInvocationManipulator.rewriteConstructorCalls(clazz, descriptor, methodNo, classLoader);
   }

   public void rewriteInstanceFieldAccess(AddedFieldData data)
   {
      instanceFieldManapulator.addField(data);
   }

   public void rewriteSubclassCalls(String className, ClassLoader classLoader, String methodName, String methodDesc)
   {
      subclassVirtualCallManilulator.addClassData(className, classLoader, methodName, methodDesc);
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
   public void replaceVirtualMethodInvokationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader)
   {
      methodInvokationManipulator.replaceVirtualMethodInvokationWithStatic(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc, classLoader);
   }

   public void replaceVirtualMethodInvokationWithLocal(String oldClass, String methodName, String newMethodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader)
   {
      methodInvokationManipulator.replaceVirtualMethodInvokationWithLocal(oldClass, methodName, newMethodName, methodDesc, newStaticMethodDesc, classLoader);
   }

   public void transformClass(ClassFile file, ClassLoader classLoader)
   {
      // first we are going to transform virtual method calls to static ones
      for (ClassManipulator m : manipulators)
      {
         m.transformClass(file, classLoader);
      }
   }

}

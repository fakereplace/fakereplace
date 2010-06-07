package org.fakereplace.manip;

import javassist.bytecode.ClassFile;

/**
 * this manipulator adds code that looks like:
 * if(this instanceof SubClass)
 * {
 *   return SomeProxy.methodCall
 * }
 * 
 * to a class
 * 
 * @author stuart
 *
 */
public class SubclassVirtualCallManipulator implements ClassManipulator
{

   public void clearRewrites(String className, ClassLoader classLoader)
   {

   }

   public void transformClass(ClassFile file, ClassLoader loader)
   {

   }

}

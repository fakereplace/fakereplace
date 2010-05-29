package org.fakereplace.manip;

import javassist.bytecode.ClassFile;

public interface ClassManipulator
{
   public void clearRewrites(String className, ClassLoader classLoader);

   public void transformClass(ClassFile file, ClassLoader loader);
}

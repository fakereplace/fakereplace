package org.fakereplace.replacement;

import java.lang.instrument.ClassDefinition;
import java.util.Set;

public class ReplacementResult
{
   private final ClassDefinition[] classes;
   private final Set<Class<?>> classesToRetransform;

   public ReplacementResult(ClassDefinition[] classes, Set<Class<?>> classesToRetransform)
   {
      this.classes = classes;
      this.classesToRetransform = classesToRetransform;
   }

   public ClassDefinition[] getClasses()
   {
      return classes;
   }

   public Set<Class<?>> getClassesToRetransform()
   {
      return classesToRetransform;
   }
}

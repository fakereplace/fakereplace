package org.fakereplace.replacement;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

import org.fakereplace.data.AnnotationBuilder;
import org.fakereplace.data.AnnotationDataStore;

public class AnnotationReplacer
{

   /**
    * Stores class file annotations changes and reverts the file annotations to
    * the old annotations
    * 
    * @param file
    * @param old
    */
   public static void processAnnotations(ClassFile file, Class old)
   {

      AnnotationsAttribute newAns = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.visibleTag);
      AnnotationDataStore.recordClassAnnotations(old, newAns);
      file.addAttribute(duplicateAnnotationsAttribute(file.getConstPool(), old));
   }

   public static AnnotationsAttribute duplicateAnnotationsAttribute(ConstPool cp, AnnotatedElement element)
   {
      AnnotationsAttribute oldAns = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);
      for (Annotation a : element.getAnnotations())
      {
         oldAns.addAnnotation(AnnotationBuilder.createJavassistAnnotation(a, cp));
      }
      return oldAns;
   }
}

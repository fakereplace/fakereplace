package org.fakereplace.manip;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;

import org.fakereplace.boot.Constants;
import org.fakereplace.data.ModifiedMethod;

/**
 * manipulator that removes the final attribute from methods
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class FinalMethodManipulator implements ClassManipulator
{

   public void clearRewrites(String className, ClassLoader classLoader)
   {

   }

   private static final Set<String> classLoaders = new CopyOnWriteArraySet<String>();

   public static void addClassLoader(String nm)
   {
      classLoaders.add(nm);
   }

   public void transformClass(ClassFile file, ClassLoader loader)
   {
      if (classLoaders.contains(file.getName()))
      {
         return;
      }
      for (Object i : file.getMethods())
      {
         MethodInfo m = (MethodInfo) i;
         if ((m.getAccessFlags() & AccessFlag.FINAL) != 0)
         {
            m.setAccessFlags(m.getAccessFlags() & ~AccessFlag.FINAL);
            // ClassDataStore.addFinalMethod(file.getName(), m.getName(),
            // m.getDescriptor());
            AnnotationsAttribute at = (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag);
            if (at == null)
            {
               at = new AnnotationsAttribute(file.getConstPool(), AnnotationsAttribute.visibleTag);
               m.addAttribute(at);
            }
            at.addAnnotation(new Annotation(ModifiedMethod.class.getName(), file.getConstPool()));
            m.addAttribute(new AttributeInfo(file.getConstPool(), Constants.FINAL_METHOD_ATTRIBUTE, new byte[0]));
         }
      }
   }

}

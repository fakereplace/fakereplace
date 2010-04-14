package org.fakereplace;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.fakereplace.boot.Constants;
import org.fakereplace.boot.GlobalClassDefinitionData;
import org.fakereplace.boot.Logger;

public class ClassLoaderInstrumentation
{

   public ClassLoaderInstrumentation(Instrumentation instrumentation)
   {
      this.instrumentation = instrumentation;
   }

   final Instrumentation instrumentation;

   Set<Class<?>> trasformedClassLoaders = new CopyOnWriteArraySet<Class<?>>();

   Set<Class<?>> failedTransforms = new CopyOnWriteArraySet<Class<?>>();

   /**
    * This method instruments class loaders so that they can load our helper
    * classes.
    * 
    * @param cl
    */
   public void redefineClassLoader(Class<?> cl)
   {
      try
      {

         // we are using the high level javassist bytecode here because we
         // have access to the class object
         if (cl.getClassLoader() != null)
         {

            URL resource = cl.getClassLoader().getResource(cl.getName().replace('.', '/') + ".class");
            InputStream in = null;
            try
            {
               in = resource.openStream();
               ClassPool.getDefault().makeClass(in);
            }
            catch (Exception e)
            {
               throw new RuntimeException(e);
            }
            finally
            {
               try
               {
                  in.close();
               }
               catch (IOException e)
               {
               }
            }
         }

         CtClass cls = ClassPool.getDefault().getCtClass(cl.getName());
         CtClass str = ClassPool.getDefault().getCtClass("java.lang.String");
         CtClass[] arg = new CtClass[2];
         arg[0] = str;
         arg[1] = CtClass.booleanType;
         // now we instrument the loadClass
         // if the system requests a class from the generated class package
         // then
         // we check to see if it is already loaded.
         // if not we try and get the class definition from GlobalData
         // we do not need to delegate as GlobalData will only
         // return the data to the correct classloader.
         // if the data is not null then we define the class, link
         // it if requested and return it.
         CtMethod method = cls.getDeclaredMethod("loadClass", arg);
         method.insertBefore("if($1.startsWith(\"" + Constants.GENERATED_CLASS_PACKAGE + "\")){ try{ Class find = findLoadedClass($1); if(find != null) return find; byte[] cd = " + GlobalClassDefinitionData.class.getName() + ".getProxyDefinition(this,$1); if(cd != null){ Class c = defineClass($1,cd,0,cd.length); if($2) resolveClass(c); return c; } }catch(Throwable e) {e.printStackTrace(); return null;}} if($1.startsWith(\"org.fakereplace.integration\")){ try{ byte[] cd = " + Transformer.class.getName() + ".getIntegrationClass(this,$1); if(cd != null){ Class c = defineClass($1,cd,0,cd.length); if($2) resolveClass(c); return c; } }catch(Throwable e) {e.printStackTrace(); return null;}}");

         // now reload the instrumented class loader
         ClassDefinition cd = new ClassDefinition(cl, cls.toBytecode());
         ClassDefinition[] ar = new ClassDefinition[1];
         ar[0] = cd;
         instrumentation.redefineClasses(ar);
         // make a note of the fact that we have transformed this class
         // loader
         trasformedClassLoaders.add(cl);
      }
      catch (NotFoundException e)
      {
         if (cl.getSuperclass() != ClassLoader.class && !trasformedClassLoaders.contains(cl.getSuperclass()) && !failedTransforms.contains(cl))
         {
            redefineClassLoader(cl.getSuperclass());
         }
         trasformedClassLoaders.add(cl);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         failedTransforms.add(cl);
      }
      finally
      {
      }
   }

   /**
    * Checks to see if a class loader has already been instrumented and if not
    * instriments it
    * 
    * @param loader
    */
   void instrumentClassLoaderIfNessesary(ClassLoader loader, String className)
   {
      if (loader != null)
      {
         if (!trasformedClassLoaders.contains(loader.getClass()) && !failedTransforms.contains(loader.getClass()))
         {
            redefineClassLoader(loader.getClass());
         }
         if (failedTransforms.contains(loader.getClass()))
         {
            Logger.log(this, "The class loader for " + className + " could not be instrumented");
         }
      }
   }
}

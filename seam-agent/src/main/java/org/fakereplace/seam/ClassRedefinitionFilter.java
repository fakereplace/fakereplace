package org.fakereplace.seam;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;
import javax.faces.event.PhaseEvent;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.annotations.web.Filter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.web.AbstractFilter;

import sun.awt.AppContext;

@Startup
@Scope(ScopeType.APPLICATION)
@Name("org.fakereplace.classRedefinitionFilter")
@BypassInterceptors
@Filter
public class ClassRedefinitionFilter extends AbstractFilter
{

   static class FileData
   {
      Long lastModified;
      File file;
      String className;
   }

   static final String[] fl = { "seam.properties", "META-INF/components.xml" };

   List<FileData> files = null;

   ReentrantLock lock = new ReentrantLock();

   Set<File> directories = new HashSet<File>();

   Method replaceMethod = null;

   String AGENT_CLASS = "org.fakereplace.Agent";

   boolean enabled = true;

   boolean changed = false;

   /**
    * gets a reference to the replaceClass method. If this fails
    * because the agent has not been installed then the filter is disabled
    * If the method suceed then the doInit method is called which scans 
    * the classpath for exploded jars that can have classes redefined
    */
   public ClassRedefinitionFilter()
   {
      try
      {
         Class agent = Class.forName(AGENT_CLASS);
         replaceMethod = agent.getMethod("redefine", ClassDefinition[].class);
         doInit();

      }
      catch (Exception e)
      {
         System.out.println("------------------------------------------------------------------------");
         System.out.println("------ Fakereplace agent not availbile, hot deployment is disabled -----");
         System.out.println("------------------------------------------------------------------------");
         enabled = false;
      }
   }

   public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2) throws IOException, ServletException
   {
      if (enabled)
      {

         if (lock.tryLock())
         {
            try
            {
               doReplace();
            }
            finally
            {
               lock.unlock();
            }
         }

      }
      arg2.doFilter(arg0, arg1);
   }

   void doReplace()
   {
      try
      {
         Lifecycle.beginCall();
         List<ClassDefinition> classesToReplace = new ArrayList<ClassDefinition>();

         for (FileData d : files)
         {
            if (d.file.lastModified() > d.lastModified)
            {
               System.out.println("File " + d.className + " has been modified, replacing");

               Class<?> ctr = Class.forName(d.className);
               byte[] fileData = readFile(d.file);
               ClassDefinition cd = new ClassDefinition(ctr, fileData);
               classesToReplace.add(cd);
               d.lastModified = d.file.lastModified();

            }
         }
         if (!classesToReplace.isEmpty())
         {
            Seam.clearComponentNameCache();

            ClassDefinition[] data = new ClassDefinition[classesToReplace.size()];
            for (int i = 0; i < classesToReplace.size(); ++i)
            {
               ClassDefinition d = classesToReplace.get(i);
               data[i] = d;

               // if the class is a seam component
               if (d.getDefinitionClass().isAnnotationPresent(Name.class))
               {
                  String name = d.getDefinitionClass().getAnnotation(Name.class).value();
                  Component component = Component.forName(name);
                  if (component != null)
                  {
                     ScopeType scope = component.getScope();
                     if (scope != ScopeType.STATELESS && scope.isContextActive())
                     {
                        scope.getContext().remove(name);
                     }
                     Init.instance().removeObserverMethods(component);
                  }
                  Contexts.getApplicationContext().remove(name + Initialization.COMPONENT_SUFFIX);
               }

            }
            // do the replacement
            replaceMethod.invoke(null, (Object) data);

            changed = true;
            // clear reflection caches
            Field field = Introspector.class.getDeclaredField("declaredMethodCache");
            field.setAccessible(true);
            Map map = (Map) field.get(null);
            map.clear();

            field = Introspector.class.getDeclaredField("BEANINFO_CACHE");
            field.setAccessible(true);
            Object beaninfoCache = field.get(null);

            map = (Map<Class<?>, BeanInfo>) AppContext.getAppContext().get(beaninfoCache);
            if (map != null)
            {
               map.clear();
            }

            // redeploy the components

            Initialization init = new Initialization(getServletContext());

            Method redeploy = Initialization.class.getDeclaredMethod("installScannedComponentAndRoles", Class.class);
            redeploy.setAccessible(true);
            for (int i = 0; i < data.length; ++i)
            {
               redeploy.invoke(init, data[i].getDefinitionClass());
            }
            redeploy = Initialization.class.getDeclaredMethod("installComponents", Init.class);
            redeploy.setAccessible(true);
            redeploy.invoke(init, Init.instance());

         }

      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         Lifecycle.endCall();
      }
   }

   public void doInit()
   {

      try
      {
         for (String resource : fl)
         {
            Enumeration<URL> urls = getClass().getClassLoader().getResources(resource);

            while (urls.hasMoreElements())
            {
               URL i = urls.nextElement();
               String path = i.getPath();
               path = path.substring(0, path.length() - resource.length() - 1);
               File f = new File(path);
               if (f.isDirectory() && path.endsWith(".jar"))
               {
                  directories.add(f);
               }
            }

         }
         initialScan();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }

   }

   @Observer("org.jboss.seam.afterPhase")
   public void phaseInvalidator(PhaseEvent event)
   {
      if (changed)
      {
         changed = false;
         try
         {
            javax.faces.context.FacesContext context = (javax.faces.context.FacesContext) Component.getInstance("org.jboss.seam.faces.facesContext");
            ELResolver resolver = context.getELContext().getELResolver();
            if (resolver instanceof CompositeELResolver)
            {
               CompositeELResolver c = (CompositeELResolver) resolver;

               Field resolvers = getField(c.getClass(), "resolvers");
               resolvers.setAccessible(true);
               ELResolver[] resAr = (ELResolver[]) resolvers.get(c);
               for (ELResolver r : resAr)
               {
                  if (r instanceof BeanELResolver)
                  {
                     Field cacheField = getField(r.getClass(), "cache");
                     cacheField.setAccessible(true);
                     Object cache = cacheField.get(r);
                     Method m = cache.getClass().getMethod("clear");
                     m.invoke(cache);
                  }
               }

            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   public void initialScan()
   {
      files = new ArrayList<FileData>();
      for (File f : directories)
      {
         handleDirectory(f, f);
      }
   }

   private void handleDirectory(File dir, File rootDir)
   {
      if (!dir.isDirectory())
         return;
      String dirPath = rootDir.getAbsolutePath();
      for (File f : dir.listFiles())
      {
         if (!f.isDirectory())
         {
            if (f.getName().endsWith(".class"))
            {
               FileData fd = new FileData();
               fd.file = f;
               fd.lastModified = f.lastModified();
               String d = f.getAbsolutePath().substring(dirPath.length() + 1);
               d = d.replace('/', '.');
               d = d.replace('\\', '.');
               d = d.substring(0, d.length() - ".class".length());
               fd.className = d;
               files.add(fd);
            }
         }
         else
         {
            handleDirectory(f, rootDir);
         }
      }
   }

   byte[] readFile(File file) throws IOException
   {
      InputStream is = new FileInputStream(file);

      long length = file.length();

      byte[] bytes = new byte[(int) length];

      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
      {
         offset += numRead;
      }

      is.close();
      return bytes;
   }

   Set<Field> getFields(Class<?> clazz)
   {
      Set<Field> fields = new HashSet<Field>();
      getFields(fields, clazz);
      return fields;
   }

   void getFields(Set<Field> fields, Class<?> clazz)
   {
      if (clazz == Object.class)
         return;
      for (Field f : clazz.getDeclaredFields())
      {
         fields.add(f);
      }
      getFields(fields, clazz);

   }

   Field getField(Class clazz, String name) throws NoSuchFieldException
   {
      if (clazz == Object.class)
         throw new NoSuchFieldException();
      try
      {
         return clazz.getDeclaredField(name);
      }
      catch (Exception e)
      {
         // TODO: handle exception
      }
      return getField(clazz.getSuperclass(), name);
   }

}

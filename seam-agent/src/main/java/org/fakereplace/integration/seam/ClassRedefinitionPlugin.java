package org.fakereplace.integration.seam;

import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELResolver;

import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.ClassChangeNotifier;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.el.EL;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.util.ProxyFactory;

import sun.awt.AppContext;

public class ClassRedefinitionPlugin implements ClassChangeAware
{
   public ClassRedefinitionPlugin()
   {
      try
      {
         Class<?> proxyFactory = getClass().getClassLoader().loadClass("org.jboss.seam.util.ProxyFactory");
         Field f = proxyFactory.getField("userCache");
         f.setBoolean(null, false);
      }
      catch (Throwable t)
      {
         System.out.println("Could not set org.jboss.seam.util.ProxyFactory.useCache to false: " + t.getMessage());
      }
      ClassChangeNotifier.add(this);

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

   Field getField(Class<?> clazz, String name) throws NoSuchFieldException
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

   public void beforeChange(Class<?>[] changed, Class<?>[] added)
   {
      if (!Lifecycle.isApplicationInitialized())
      {
         return;
      }
      Lifecycle.beginCall();
      Seam.clearComponentNameCache();
      for (int i = 0; i < changed.length; ++i)
      {
         Class<?> d = changed[i];

         // if the class is a seam component
         if (d.isAnnotationPresent(Name.class))
         {
            String name = d.getAnnotation(Name.class).value();
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
   }

   public void notify(Class<?>[] changed, Class<?>[] added)
   {
      if (!Lifecycle.isApplicationInitialized())
      {
         return;
      }
      try
      {
         // clear reflection caches
         Field field = Introspector.class.getDeclaredField("declaredMethodCache");
         field.setAccessible(true);
         Map<?, ?> map = (Map<?, ?>) field.get(null);
         map.clear();

         field = Introspector.class.getDeclaredField("BEANINFO_CACHE");
         field.setAccessible(true);
         Object beaninfoCache = field.get(null);

         // clear proxy factory caches
         field = ProxyFactory.class.getDeclaredField("proxyCache");
         field.setAccessible(true);
         map = (Map<?, ?>) field.get(null);
         map.clear();

         map = (Map<?, ?>) AppContext.getAppContext().get(beaninfoCache);
         if (map != null)
         {
            map.clear();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      // redeploy the components
      try
      {
         Initialization init = new Initialization(ServletLifecycle.getServletContext());

         Method redeploy = Initialization.class.getDeclaredMethod("installScannedComponentAndRoles", Class.class);
         redeploy.setAccessible(true);
         for (int i = 0; i < changed.length; ++i)
         {
            redeploy.invoke(init, changed[i]);
         }
         redeploy = Initialization.class.getDeclaredMethod("installComponents", Init.class);
         redeploy.setAccessible(true);
         redeploy.invoke(init, Init.instance());
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      try
      {
         // javax.faces.context.FacesContext context = (javax.faces.context.FacesContext)
         // Component.getInstance("org.jboss.seam.faces.facesContext");
         ELResolver resolver = EL.EL_RESOLVER;
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
                  try
                  {
                     Method m = cache.getClass().getMethod("clear");
                     m.invoke(cache);
                  }
                  catch (NoSuchMethodException e)
                  {
                     // different version of jboss el
                     Class<?> cacheClass = getClass().getClassLoader().loadClass("javax.el.BeanELResolver$ConcurrentCache");
                     Constructor<?> con = cacheClass.getConstructor(int.class);
                     con.setAccessible(true);
                     Object cacheInstance = con.newInstance(100);
                     cacheField.set(r, cacheInstance);
                  }

               }
            }

         }
      }
      catch (Exception e)
      {
         System.out.println("Could not clear EL cache:" + e.getMessage());
      }
      Lifecycle.endCall();
   }
}

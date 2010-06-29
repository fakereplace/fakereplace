package org.fakereplace.integration.seam;

import java.beans.Introspector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.ClassChangeNotifier;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.data.InstanceTracker;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.contexts.Lifecycle;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.core.Init;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.servlet.SeamFilter;
import org.jboss.seam.util.ProxyFactory;
import org.jboss.seam.web.AbstractFilter;
import org.jboss.seam.web.HotDeployFilter;

public class ClassRedefinitionPlugin implements ClassChangeAware
{
   public ClassRedefinitionPlugin()
   {
      try
      {
         Class<?> proxyFactory = getClass().getClassLoader().loadClass("org.jboss.seam.util.ProxyFactory");
         Field f = proxyFactory.getField("useCache");
         f.setAccessible(true);
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

   public void beforeChange(Class<?>[] changed, ClassIdentifier[] added)
   {
      disableHotDeployFilter();
      if (!Lifecycle.isApplicationInitialized())
      {
         return;
      }
      Lifecycle.beginCall();
      try
      {
         // fakereplace does not play nice with the hot deployment filter
         AbstractFilter filter = (AbstractFilter) Component.getInstance("org.jboss.seam.web.hotDeployFilter");
         filter.setDisabled(true);
      }
      catch (Exception e)
      {

      }
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

   public void notify(Class<?>[] changed, ClassIdentifier[] added)
   {
      if (!Lifecycle.isApplicationInitialized())
      {
         return;
      }
      try
      {
         Introspector.flushCaches();

         // clear proxy factory caches
         Field field = ProxyFactory.class.getDeclaredField("proxyCache");
         field.setAccessible(true);
         Map<?, ?> map = (Map<?, ?>) field.get(null);
         map.clear();

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
      Lifecycle.endCall();
   }

   /**
    * Fakereplace does not play nice with the hot deploy filter
    */
   public void disableHotDeployFilter()
   {
      try
      {
         Set<Object> data = InstanceTracker.get(SeamFilter.class.getName());
         for (Object i : data)
         {
            Field filters = i.getClass().getDeclaredField("filters");
            filters.setAccessible(true);
            List<?> filterList = (List<?>) filters.get(i);
            ListIterator<?> it = filterList.listIterator();
            while (it.hasNext())
            {
               Object val = it.next();
               if (val instanceof HotDeployFilter)
               {
                  System.out.println("Disabling seam hot deployment filter, it does not play nicely with fakereplace");
                  it.remove();
               }
            }
         }
      }
      catch (Exception e)
      {
         System.out.println("Unable to disable hot deploy filter");
         e.printStackTrace();
      }
   }

}

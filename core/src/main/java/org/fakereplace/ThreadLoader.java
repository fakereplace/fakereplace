/**
 * 
 */
package org.fakereplace;

class ThreadLoader implements Runnable
{
   final String className;
   final ClassLoader classLoader;
   final boolean createClass;
   volatile boolean finished = false;

   private ThreadLoader(String className, ClassLoader classLoader, boolean createClass)
   {
      this.className = className;
      this.classLoader = classLoader;
      this.createClass = createClass;
   }

   public void run()
   {
      try
      {
         Class<?> c = classLoader.loadClass(className);
         if (createClass)
         {
            c.newInstance();
         }
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (InstantiationException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IllegalAccessException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      finally
      {
         finished = true;
      }
   }

   public boolean isFinished()
   {
      return finished;
   }

   static public void loadAsync(String className, ClassLoader classLoader, boolean create)
   {
      new Thread(new ThreadLoader(className, classLoader, create)).start();
   }

}
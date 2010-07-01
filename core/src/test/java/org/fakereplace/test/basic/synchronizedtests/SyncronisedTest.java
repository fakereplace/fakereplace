package org.fakereplace.test.basic.synchronizedtests;

import java.lang.reflect.Modifier;

import org.testng.annotations.Test;

/**
 * This class tests that syncronisation still works as it should
 * 
 * this is because we are going to remove the sync attribute and replace it with
 * the equivilent sync code
 * 
 * this has not actually happended yet, because it is harder than I though, but
 * these tests will be relevant eventually
 * 
 * @author stuart
 * 
 */
public class SyncronisedTest
{

   @Test
   public void testInstanceMethodSyncronisation()
   {
      InstanceRunnableClass r = new InstanceRunnableClass();
      Thread t1 = new Thread(r);
      Thread t2 = new Thread(r);
      t1.start();
      t2.start();
      while (t1.isAlive() && t2.isAlive())
      {
         try
         {
            Thread.sleep(200);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }
      assert !r.failed;
   }

   @Test
   public void testStaticMethodSyncronisation()
   {
      StaticRunnableClass r = new StaticRunnableClass();
      Thread t1 = new Thread(r);
      Thread t2 = new Thread(r);
      t1.start();
      t2.start();
      while (t1.isAlive() && t2.isAlive())
      {
         try
         {
            Thread.sleep(200);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
      }
      assert !StaticRunnableClass.failed;
   }

   @Test
   public void testSyncBitSet() throws SecurityException, NoSuchMethodException
   {
      assert (StaticRunnableClass.class.getDeclaredMethod("doStuff").getModifiers() & Modifier.SYNCHRONIZED) != 0;
      assert (InstanceRunnableClass.class.getDeclaredMethod("doStuff").getModifiers() & Modifier.SYNCHRONIZED) != 0;
   }
}

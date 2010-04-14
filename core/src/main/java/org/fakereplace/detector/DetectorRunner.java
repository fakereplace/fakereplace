package org.fakereplace.detector;

public class DetectorRunner implements Runnable
{

   public void run()
   {
      // no need to do anything for the first 10 seconds
      sleep(10000);
      while (true)
      {
         // wait 2 seconds
         sleep(2000);

      }
   }

   public void sleep(int millis)
   {
      try
      {
         Thread.sleep(millis);
      }
      catch (InterruptedException e)
      {

      }
   }

}

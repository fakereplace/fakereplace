package org.fakereplace.detector;

/**
 * 
 * Calls the ClassChangeDetector every POLL_TIME milliseconds
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class ClassChangeDetectorRunner implements Runnable
{

   static final int POLL_TIME = 2000;

   public void run()
   {
      String detect = System.getProperty("org.fakereplace.detector");
      if (detect == null || !detect.equals("true"))
      {
         return;
      }
      // no need to do anything for the first 5 seconds
      sleep(5000);
      while (true)
      {
         // wait 2 seconds
         sleep(POLL_TIME);
         try
         {
            ClassChangeDetector.runDefault();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

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

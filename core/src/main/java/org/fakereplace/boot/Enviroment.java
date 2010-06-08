package org.fakereplace.boot;

import java.io.File;

public class Enviroment
{

   final String dumpDirectory;

   final String[] replacablePackages;

   public Enviroment()
   {
      String dump = System.getProperty(Constants.DUMP_DIRECTORY_KEY);
      if (dump != null)
      {
         File f = new File(dump);
         if (!f.exists())
         {
            System.out.println("dump directory  " + dump + " does not exist ");
            dumpDirectory = null;
         }
         else
         {
            dumpDirectory = dump;
            System.out.println("dumping class definitions to " + dump);
         }
      }
      else
      {
         dumpDirectory = null;
      }
      String plist = System.getProperty(Constants.REPLACABLE_PACKAGES_KEY);
      if (plist == null || plist.length() == 0)
      {
         System.out.println("-----------------------------------------------------------------");
         System.out.println("System property " + Constants.REPLACABLE_PACKAGES_KEY + " was not specified, fakereplace is diabled");
         System.out.println("-----------------------------------------------------------------");
         replacablePackages = new String[0];
      }
      else
      {
         replacablePackages = plist.split(",");
      }
   }

   public boolean isClassReplacable(String className)
   {
      for (String i : replacablePackages)
      {
         if (className.startsWith(i))
         {
            return true;
         }
      }
      return false;
   }

   public String getDumpDirectory()
   {
      return dumpDirectory;
   }

   public String[] getReplacablePackages()
   {
      return replacablePackages;
   }

}

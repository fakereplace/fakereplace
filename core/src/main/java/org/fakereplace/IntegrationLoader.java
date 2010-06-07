package org.fakereplace;

import java.util.HashSet;
import java.util.Set;

import org.fakereplace.api.IntegrationInfo;
import org.fakereplace.boot.Constants;

/**
 * 
 * class that is responsible for loading any bundled integrations from 
 * the fakereplace archive
 * 
 * @author stuart
 *
 */
public class IntegrationLoader
{
   public static Set<IntegrationInfo> getIntegrationInfo(ClassLoader clr)
   {
      Set<IntegrationInfo> integrations = new HashSet<IntegrationInfo>();
      for (String s : Constants.INTEGRATIONS)
      {
         String integrationClass = "org.fakereplace.integration." + s + "." + s.substring(0, 1).toUpperCase() + s.substring(1) + "IntegrationInfo";
         try
         {
            Class<?> cls = Class.forName(integrationClass);
            IntegrationInfo info = (IntegrationInfo) cls.newInstance();
            integrations.add(info);
         }
         catch (ClassNotFoundException e)
         {
            System.out.println("COULD NOT LOAD INTEGRATION CLASS: " + integrationClass);
         }
         catch (InstantiationException e)
         {
            System.out.println("COULD NOT INSTANCIATE INTEGRATION CLASS: " + integrationClass);
         }
         catch (IllegalAccessException e)
         {
            System.out.println("IllegalAccessException CREATING INTEGRATION CLASS: " + integrationClass);
         }
         catch (ClassCastException e)
         {
            System.out.println(integrationClass + " WAS NOT AN INSTANCE OF  IntegrationInfo");
         }

      }
      return integrations;
   }
}

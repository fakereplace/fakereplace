package org.fakereplace.integration.jsf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fakereplace.api.ClassTransformer;
import org.fakereplace.api.IntegrationInfo;

public class JsfIntegrationInfo implements IntegrationInfo
{

   public String getClassChangeAwareName()
   {
      return "org.fakereplace.integration.jsf.ClassRedefinitionPlugin";
   }

   public Set<String> getIntegrationTriggerClassNames()
   {
      return Collections.singleton("javax.faces.webapp.FacesServlet");
   }

   public Set<String> getTrackedInstanceClassNames()
   {
      Set<String> ret = new HashSet<String>();
      ret.add("javax.el.BeanELResolver");
      return ret;
   }

   public ClassTransformer getTransformer()
   {
      return null;
   }

   public byte[] loadClass(String className)
   {
      return null;
   }

}

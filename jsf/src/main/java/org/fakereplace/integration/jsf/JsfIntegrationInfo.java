package org.fakereplace.integration.jsf;

import java.lang.instrument.ClassFileTransformer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fakereplace.api.IntegrationInfo;

public class JsfIntegrationInfo implements IntegrationInfo
{

   public String getClassChangeAwareName()
   {
      return "org.fakereplace.integration.seam.ClassRedefinitionPlugin";
   }

   public Set<String> getIntegrationTriggerClassNames()
   {
      return Collections.singleton("org.jboss.seam.servlet.SeamFilter");
   }

   public Set<String> getTrackedInstanceClassNames()
   {
      Set<String> ret= new HashSet<String>();
      ret.add("javax.el.BeanELResolver");
      ret.add("org.jboss.seam.servlet.SeamFilter");
      return ret;
   }

   public ClassFileTransformer getTransformer()
   {
      return null;
   }

   public byte[] loadClass(String className)
   {
      return null;
   }

}

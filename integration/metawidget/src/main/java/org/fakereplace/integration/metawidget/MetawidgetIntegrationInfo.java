package org.fakereplace.integration.metawidget;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fakereplace.api.ClassTransformer;
import org.fakereplace.api.IntegrationInfo;

public class MetawidgetIntegrationInfo implements IntegrationInfo
{
   
   public final static String BASE_PROPERTY_STYLE = "org.metawidget.inspector.impl.propertystyle.BasePropertyStyle";
   public final static String BASE_ACTION_STYLE = "org.metawidget.inspector.impl.actionstyle.BaseActionStyle";
   

   public String getClassChangeAwareName()
   {
      return "org.fakereplace.integration.metawidget.ClassRedefinitionPlugin";
   }

   public Set<String> getIntegrationTriggerClassNames()
   {
      return Collections.singleton(BASE_ACTION_STYLE);
   }

   public Set<String> getTrackedInstanceClassNames()
   {
      Set<String> ret = new HashSet<String>();
      ret.add(BASE_ACTION_STYLE);
      ret.add(BASE_PROPERTY_STYLE);
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

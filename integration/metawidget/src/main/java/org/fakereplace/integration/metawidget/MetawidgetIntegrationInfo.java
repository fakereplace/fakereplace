package org.fakereplace.integration.metawidget;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fakereplace.api.ClassTransformer;
import org.fakereplace.api.IntegrationInfo;

public class MetawidgetIntegrationInfo implements IntegrationInfo
{

   public String getClassChangeAwareName()
   {
      return "org.fakereplace.integration.metawidget.ClassRedefinitionPlugin";
   }

   public Set<String> getIntegrationTriggerClassNames()
   {
      return Collections.singleton("org.metawidget.inspector.impl.actionstyle.BaseActionStyle");
   }

   public Set<String> getTrackedInstanceClassNames()
   {
      Set<String> ret = new HashSet<String>();
      ret.add("org.metawidget.inspector.impl.propertystyle.BasePropertyStyle");
      ret.add("org.metawidget.inspector.impl.actionstyle.BaseActionStyle");
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

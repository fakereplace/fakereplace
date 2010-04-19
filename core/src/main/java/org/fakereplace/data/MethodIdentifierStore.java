package org.fakereplace.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns a method number for a generated method. Methods with the same name
 * and descriptor are assigned the same number to make emulating virtual calls
 * easier
 * 
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 * 
 */
public class MethodIdentifierStore
{
   static private Map<String, Map<String, Integer>> data = new HashMap<String, Map<String, Integer>>();

   static private int methodNo = 0;

   public static synchronized int getMethodNumber(String name, String descriptor)
   {
      if (!data.containsKey(name))
      {
         data.put(name, new HashMap<String, Integer>());
      }
      Map<String, Integer> im = data.get(name);
      if (!im.containsKey(descriptor))
      {
         im.put(descriptor, methodNo++);
      }
      return im.get(descriptor);
   }
}

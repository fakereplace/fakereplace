package org.fakereplace.util;

/**
 * 
 * @author stuart
 * 
 */
public class InvocationUtil
{
   /**
    * appends object to the start of the array
    * 
    */
   static public Object[] prepare(Object object, Object[] array)
   {
      int length = 0;
      if (array != null)
      {
         length = array.length;
      }
      Object[] ret = new Object[length + 1];
      ret[0] = object;
      for (int i = 0; i < length; ++i)
      {
         ret[i + 1] = array[i];
      }
      return ret;
   }

}

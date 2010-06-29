package org.fakereplace.integration.weld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import bsh.ClassIdentifier;

public class ClassRedefinitionPlugin implements ClassChangeAware
{
   public ClassRedefinitionPlugin()
   {

   }

   byte[] readFile(File file) throws IOException
   {
      InputStream is = new FileInputStream(file);

      long length = file.length();

      byte[] bytes = new byte[(int) length];

      int offset = 0;
      int numRead = 0;
      while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
      {
         offset += numRead;
      }

      is.close();
      return bytes;
   }

   Field getField(Class<?> clazz, String name) throws NoSuchFieldException
   {
      if (clazz == Object.class)
         throw new NoSuchFieldException();
      try
      {
         return clazz.getDeclaredField(name);
      }
      catch (Exception e)
      {
         // TODO: handle exception
      }
      return getField(clazz.getSuperclass(), name);
   }

   public void beforeChange(Class<?>[] changed, ClassIdentifier[] added)
   {

   }

   public void notify(Class<?>[] changed, ClassIdentifier[] added)
   {

   }

}

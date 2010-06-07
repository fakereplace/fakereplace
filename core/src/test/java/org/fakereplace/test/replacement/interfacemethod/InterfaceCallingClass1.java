package org.fakereplace.test.replacement.interfacemethod;

public class InterfaceCallingClass1
{

   public String call(SomeInterface1 interfaceClass)
   {
      return interfaceClass.added();
   }
}

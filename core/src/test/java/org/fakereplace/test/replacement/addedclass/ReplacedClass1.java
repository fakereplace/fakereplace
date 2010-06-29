package org.fakereplace.test.replacement.addedclass;

public class ReplacedClass1
{
   public String getValue()
   {
      return new AddedClass1().getValue(this);
   }

   public String getName()
   {
      return "Bob";
   }
}

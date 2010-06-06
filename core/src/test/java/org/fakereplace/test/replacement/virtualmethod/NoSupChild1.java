package org.fakereplace.test.replacement.virtualmethod;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class NoSupChild1 extends NoSupClass
{
   public String getStuff()
   {
      return "NoSupChild";
   }
}

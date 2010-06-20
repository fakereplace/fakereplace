package org.fakereplace.test.replacement.virtualmethod;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class NoSupChild1 extends NoSupClass
{
   public String getStuff(long i4, int f1, String str, float fl, double dl)
   {
      return "NoSupChild";
   }
}

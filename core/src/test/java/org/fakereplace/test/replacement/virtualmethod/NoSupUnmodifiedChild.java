package org.fakereplace.test.replacement.virtualmethod;

import org.fakereplace.util.DoNotAddSuperDelegatingMethods;

@DoNotAddSuperDelegatingMethods
public class NoSupUnmodifiedChild extends NoSupClass
{
   @Override
   public String getStuff(long i4, int f1, String str, float fl, double dl)
   {
      return "NoSupUnmodifiedChild";
   }
}

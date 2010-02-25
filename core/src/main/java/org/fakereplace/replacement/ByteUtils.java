package org.fakereplace.replacement;

import javassist.bytecode.Bytecode;

public class ByteUtils
{
   public static void add16bit(Bytecode b, int value)
   {
      value = value % 65536;
      b.add(value >> 8);
      b.add(value % 256);
   }
}

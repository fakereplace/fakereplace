package org.fakereplace.util;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;

/**
 * Utility class that creates the System.out.println bytecode
 * usefull for debugging
 * @author Stuart Douglas <stuart@baileyroberts.com.au>
 *
 */
public class PrintLn
{
   public static Bytecode println(ConstPool cp, String message)
   {
      Bytecode proxyBytecode = new Bytecode(cp);
      proxyBytecode.addGetstatic("java/lang/System", "out", "Ljava/io/PrintStream;");
      proxyBytecode.addLdc(message);
      proxyBytecode.addInvokevirtual("java.io.PrintStream", "println", "(Ljava/lang/String;)V");
      return proxyBytecode;
   }

   public static void println(Bytecode proxyBytecode, String message)
   {
      proxyBytecode.addGetstatic("java/lang/System", "out", "Ljava/io/PrintStream;");
      proxyBytecode.addLdc(message);
      proxyBytecode.addInvokevirtual("java.io.PrintStream", "println", "(Ljava/lang/String;)V");
   }
}

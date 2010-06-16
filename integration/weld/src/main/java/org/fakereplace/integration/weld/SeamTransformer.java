package org.fakereplace.integration.weld;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

import org.fakereplace.api.ClassTransformer;

public class SeamTransformer implements ClassTransformer
{

   public byte[] transform(byte[] data, String className)
   {
      try
      {
         if (className.equals("org/jboss/seam/servlet/SeamFilter"))
         {
            ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(data)));

            MethodInfo method = file.getMethod("doFilter");
            Bytecode b = new Bytecode(file.getConstPool());
            b.add(Opcode.ALOAD_0); // load this
            b.addInvokestatic("org.fakereplace.integration.seam.SeamDetector", "run", "(Ljava/lang/Object;)V");
            method.getCodeAttribute().iterator().insert(b.get());

            method = file.getMethod("<init>");
            b = new Bytecode(file.getConstPool());
            b.add(Opcode.ALOAD_0); // load this
            b.addInvokestatic("org.fakereplace.integration.seam.SeamDetector", "init", "(Ljava/lang/Object;)V");
            CodeIterator it = method.getCodeAttribute().iterator();
            it.skipConstructor();
            it.insert(b.get());

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            file.write(new DataOutputStream(bs));
            return bs.toByteArray();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return null;
   }

}

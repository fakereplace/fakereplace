/*
 * Copyright 2011, Stuart Douglas
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.integration.seam;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import org.fakereplace.api.ClassTransformer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class SeamTransformer implements ClassTransformer {

    public byte[] transform(byte[] data, String className) {
        try {
            if (className.equals("org/jboss/seam/servlet/SeamFilter")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.util;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ConstPool;

/**
 * Utility class that creates the System.out.println bytecode
 * usefull for debugging
 *
 * @author Stuart Douglas <stuart.w.douglas@gmail.com>
 */
public class PrintLn {
    public static Bytecode println(ConstPool cp, String message) {
        Bytecode proxyBytecode = new Bytecode(cp);
        proxyBytecode.addGetstatic("java/lang/System", "out", "Ljava/io/PrintStream;");
        proxyBytecode.addLdc(message);
        proxyBytecode.addInvokevirtual("java.io.PrintStream", "println", "(Ljava/lang/String;)V");
        return proxyBytecode;
    }

    public static void println(Bytecode proxyBytecode, String message) {
        proxyBytecode.addGetstatic("java/lang/System", "out", "Ljava/io/PrintStream;");
        proxyBytecode.addLdc(message);
        proxyBytecode.addInvokevirtual("java.io.PrintStream", "println", "(Ljava/lang/String;)V");
    }
}

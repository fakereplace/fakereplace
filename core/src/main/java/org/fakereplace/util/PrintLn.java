/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

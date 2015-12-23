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

package org.fakereplace.core;

public class BuiltinClassData {

    private static final String[] doNotInstrument = {"org/fakereplace", "java/math", "java/lang", "java/util/concurrent", "java/util/Currency", "java/util/Random", "java/util",};

    private static final String[] exceptions = {"java/lang/reflect/Proxy",};

    public static boolean skipInstrumentation(String className) {
        if(className == null) {
            return false;
        }
        className = className.replace('.', '/');
        for (String s : exceptions) {
            if (className.startsWith(s)) {
                return false;
            }
        }
        for (String s : doNotInstrument) {
            if (className.startsWith(s)) {
                return true;
            }
        }
        return false;
    }
}

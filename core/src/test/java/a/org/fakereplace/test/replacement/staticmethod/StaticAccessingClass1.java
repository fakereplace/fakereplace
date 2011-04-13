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

package a.org.fakereplace.test.replacement.staticmethod;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class StaticAccessingClass1 {

    public static int getInt() {
        return StaticClass1.getInt();
    }

    public static long getLong() {
        return StaticClass1.getLong();
    }

    public static Integer integerAdd(Integer val) {
        return StaticClass1.integerAdd(val);
    }

    public static int intAdd(int val) {
        return StaticClass1.intAdd(val);
    }

    public static short shortAdd(short val) {
        return StaticClass1.shortAdd(val);
    }

    public static byte byteAdd(byte val) {
        return StaticClass1.byteAdd(val);
    }

    public static float floatAdd(float val) {
        return StaticClass1.floatAdd(val);
    }

    public static char charAdd(char c) {
        return StaticClass1.charAdd(c);
    }

    public static boolean negate(boolean bool) {
        return StaticClass1.negate(bool);
    }

    public static double doubleAdd(double val) {
        return StaticClass1.doubleAdd(val);
    }

    public static long longAdd(long val) {
        return StaticClass1.longAdd(val);
    }

}

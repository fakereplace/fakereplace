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
public class StaticClass1 {
    public static Integer method2() {
        return 1;
    }

    public static Integer add() {
        return value++;
    }

    private static Integer value = 0;

    public static Integer getValue() {
        return value;
    }

    private static void privateMethod() {

    }

    public static int getInt() {
        return 10;
    }

    public static long getLong() {
        return 11;
    }

    public static Integer integerAdd(Integer val) {
        return val + 1;
    }

    public static int intAdd(int val) {
        return val + 1;
    }

    public static short shortAdd(short val) {
        return (short) (val + 1);
    }

    public static byte byteAdd(byte val) {
        return (byte) (val + 1);
    }

    public static float floatAdd(float val) {
        return val + 1.0f;
    }

    public static char charAdd(char c) {
        return (char) (c + 1);
    }

    public static boolean negate(boolean bool) {
        return !bool;
    }

    public static double doubleAdd(double val) {
        return val + 1.0;
    }

    public static long longAdd(long val) {
        return val + 1;
    }

    public static int[] arrayMethod(int[] aray) {
        int[] ret = new int[aray.length];
        for (int i = 0; i < aray.length; ++i) {
            ret[i] = aray[i] + 1;
        }
        return ret;
    }

    public static String getString() {
        return "hello";
    }
}

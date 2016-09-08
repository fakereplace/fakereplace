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

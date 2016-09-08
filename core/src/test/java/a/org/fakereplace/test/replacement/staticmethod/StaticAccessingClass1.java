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
public class StaticAccessingClass1 {

    String getString() {
        String s = StaticClass1.getString();
        System.out.println(s.length());
        return s;
    }

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

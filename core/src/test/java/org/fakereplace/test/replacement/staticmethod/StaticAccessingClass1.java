package org.fakereplace.test.replacement.staticmethod;

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

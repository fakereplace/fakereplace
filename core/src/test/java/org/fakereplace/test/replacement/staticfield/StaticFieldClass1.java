package org.fakereplace.test.replacement.staticfield;

import org.fakereplace.util.NoInstrument;

import java.util.List;

@NoInstrument
public class StaticFieldClass1 {
    public static long longField = 0;

    static List<String> list = null;

    public static long incAndGet() {
        longField++;
        return longField;
    }

}

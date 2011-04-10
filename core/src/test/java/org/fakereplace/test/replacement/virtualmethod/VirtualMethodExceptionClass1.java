package org.fakereplace.test.replacement.virtualmethod;

public class VirtualMethodExceptionClass1 {
    int a = 0;

    public int doStuff1(int param1, int param2) {
        try {
            if (a == 0) {
                throw new Exception();
            }
            return 1;
        } catch (Exception e) {

        }
        return 1;
    }

    public int doStuff2(int param1, int param2) {
        try {
            if (a == 0) {
                throw new Exception();
            }
            return 1;
        } catch (Exception e) {

        }
        return 1;
    }
}

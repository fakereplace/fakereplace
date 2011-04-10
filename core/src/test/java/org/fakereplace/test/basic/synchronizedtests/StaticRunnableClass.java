package org.fakereplace.test.basic.synchronizedtests;

public class StaticRunnableClass implements Runnable {
    private static volatile int count = 0;

    public static boolean failed = false;

    public synchronized static void doStuff() {
        int c = ++count;
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (count != c) {
            failed = true;
        }
    }

    public void run() {
        doStuff();
    }

}

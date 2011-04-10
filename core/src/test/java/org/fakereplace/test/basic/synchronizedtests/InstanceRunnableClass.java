package org.fakereplace.test.basic.synchronizedtests;

public class InstanceRunnableClass implements Runnable {
    private volatile int count = 0;

    public boolean failed = false;

    public synchronized void doStuff() {
        int c = ++count;
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // make sure another thread has not changed the variable while we have
        // been asleep
        if (count != c) {
            failed = true;
        }
    }

    public void run() {
        doStuff();
    }

}

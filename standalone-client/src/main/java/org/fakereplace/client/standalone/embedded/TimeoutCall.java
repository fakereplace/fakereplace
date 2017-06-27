package org.fakereplace.client.standalone.embedded;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Invokes the given code, only after a certain time has passed.
 */
public class TimeoutCall<T> {
    private static volatile Set<TimeoutCall> timeoutCalls = new HashSet<>();

    private final T data;
    private final Consumer<T> code;

    private long expirationTime;

    static {
        TimeoutCall.startNotificationThread();
    }

    public TimeoutCall(Consumer<T> code, T data, long delay) {
        this.expirationTime = System.currentTimeMillis() + delay;
        this.data = data;
        this.code = code;

        synchronized (TimeoutCall.class) {
            timeoutCalls.add(this);
            TimeoutCall.class.notify();
        }
    }

    /**
     * Set the expiration time, as the current time + delay
     * @param delay
     * @return
     */
    public TimeoutCall<T> reschedule(long delay) {
        this.expirationTime = System.currentTimeMillis() + delay;

        return this;
    }

    /**
     * Cancel the current timeout.
     */
    public void cancel() {
        synchronized (TimeoutCall.class) {
            timeoutCalls.remove(this);
        }
    }

    /**
     * Run the notifications thread.
     */
    public static void startNotificationThread() {
        List<TimeoutCall> timeoutCallsToRun = new LinkedList<>();

        new Thread(() -> {
            while (true) {
                synchronized (TimeoutCall.class) {
                    do {
                        try {
                            TimeoutCall.class.wait(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (timeoutCalls.isEmpty());

                    long currentTime = System.currentTimeMillis();

                    Iterator<TimeoutCall> timeoutCallIterator = timeoutCalls.iterator();
                    while (timeoutCallIterator.hasNext()) {
                        TimeoutCall timeoutCall = timeoutCallIterator.next();

                        if (timeoutCall.isActive(currentTime)) {
                            continue;
                        }

                        timeoutCallsToRun.add(timeoutCall);
                        timeoutCallIterator.remove();
                    }
                }

                timeoutCallsToRun.forEach(TimeoutCall::executeCode);
                timeoutCallsToRun.clear();
            }
        }, "FakeReplace-Timeouts").start();
    }

    private boolean isActive(long currentTime) {
        return this.expirationTime > currentTime;
    }

    private void executeCode() {
        code.accept(data);
    }
}

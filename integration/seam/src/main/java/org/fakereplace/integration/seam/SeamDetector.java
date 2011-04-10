package org.fakereplace.integration.seam;

import org.fakereplace.detector.ClassChangeDetector;

/**
 * class that is loaded by the same ClassLoader that loads seam and runs a filter based detector
 *
 * @author stuart
 */
public class SeamDetector {
    public static void init(Object key) {
        ClassChangeDetector.claimClassLoader(key, key.getClass().getClassLoader());
    }

    public static void run(Object key) {
        ClassChangeDetector.run(key);
    }
}

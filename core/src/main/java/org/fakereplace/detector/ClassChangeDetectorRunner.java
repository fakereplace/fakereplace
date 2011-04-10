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

package org.fakereplace.detector;

/**
 * Calls the ClassChangeDetector every POLL_TIME milliseconds
 *
 * @author Stuart Douglas <stuart.w.douglas@gmail.com>
 */
public class ClassChangeDetectorRunner implements Runnable {

    static final int POLL_TIME = 2000;

    public void run() {
        String detect = System.getProperty("org.fakereplace.detector");
        if (detect == null || !detect.equals("true")) {
            return;
        }
        // no need to do anything for the first 5 seconds
        sleep(5000);
        while (true) {
            // wait 2 seconds
            sleep(POLL_TIME);
            try {
                ClassChangeDetector.runDefault();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }

}

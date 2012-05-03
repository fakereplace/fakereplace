/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.logging;

/**
 * Logging class, at the moment just writes to stdout. Using java.util.logging is problematic from a javaagent,
 * and as fakereplace should never be used in production this is all that is required.
 *
 * @author stuart
 */
public class Logger {

    private static final String TRACE = "trace";

    private static volatile LogManager logManager = new DefaultLogManager();

    public Logger(final Class<?> clazz) {
        this.clazz = clazz;
    }

    public static Logger getLogger(final Class<?> clazz) {
        return new Logger(clazz);
    }

    private final Class<?> clazz;

    public void error(final String message) {
        logManager.error(clazz, message);
    }

    public void error(final String message, Throwable cause) {
        logManager.error(clazz, message, cause);
    }

    public void info(final String message) {
        logManager.info(clazz, message);
    }

    public void info(final String message, Throwable cause) {
        logManager.info(clazz, message, cause);
    }

    public void debug(final String message) {
        logManager.debug(clazz, message);
    }

    public void debug(final String message, Throwable cause) {
        logManager.debug(clazz, message, cause);
    }

    public void trace(final String message) {
        logManager.trace(clazz, message);
    }

    public void trace(final String message, Throwable cause) {
        logManager.trace(clazz, message, cause);
    }
}

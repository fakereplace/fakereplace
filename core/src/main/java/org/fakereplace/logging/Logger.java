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

package org.fakereplace.logging;

/**
 * Logging class, at the moment just writes to stdout. Using java.util.logging is problematic from a javaagent,
 * and as fakereplace should never be used in production this is all that is required.
 *
 * @author stuart
 */
public class Logger {

    private static final String TRACE = "trace";

    private static final LogManager logManager = new DefaultLogManager();

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

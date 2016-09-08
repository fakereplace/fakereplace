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

import java.util.Arrays;
import java.util.Locale;

import org.fakereplace.core.AgentOption;
import org.fakereplace.core.AgentOptions;

/**
 * The default logger, just uses System.out and System.err
 *
 * @author Stuart Douglas
 */
class DefaultLogManager implements LogManager {

    private final Level level;

    DefaultLogManager() {
        final String levelString = AgentOptions.getOption(AgentOption.LOG);
        Level level = Level.INFO;
        if(levelString != null) {
            try {
                level = Level.valueOf(levelString.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                System.err.println("Could not find level " + levelString + " options are " + Arrays.asList(Level.values()));
            }
        }
        this.level = level;
    }

    @Override
    public void error(final Class<?> category, final String message) {
        System.out.println("[" + category.getName() + "] ERROR " + message);
    }

    @Override
    public void error(final Class<?> category, final String message, final Throwable cause) {
        System.out.println("[" + category.getName() + "] ERROR " + message);
        cause.printStackTrace(System.err);
    }

    @Override
    public void info(final Class<?> category, final String message) {
        if(level.ordinal() <= Level.INFO.ordinal()) {
            System.out.println("[" + category.getName() + "] INFO " + message);
        }
    }

    @Override
    public void info(final Class<?> category, final String message, final Throwable cause) {
        if(level.ordinal() <= Level.INFO.ordinal()) {
            System.out.println("[" + category.getName() + "] INFO " + message);
            cause.printStackTrace();
        }
    }

    @Override
    public void debug(final Class<?> category, final String message) {
        if(level.ordinal() <= Level.DEBUG.ordinal()) {
            System.out.println("[" + category.getName() + "] DEBUG " + message);
        }
    }

    @Override
    public void debug(final Class<?> category, final String message, final Throwable cause) {
        if(level.ordinal() <= Level.DEBUG.ordinal()) {
            System.out.println("[" + category.getName() + "] DEBUG " + message);
            cause.printStackTrace();
        }
    }

    @Override
    public void trace(final Class<?> category, final String message) {
        if(level.ordinal() <= Level.TRACE.ordinal()) {
            System.out.println("[" + category.getName() + "] TRACE " + message);
        }
    }

    @Override
    public void trace(final Class<?> category, final String message, final Throwable cause) {
        if(level.ordinal() <= Level.TRACE.ordinal()) {
            System.out.println("[" + category.getName() + "] TRACE " + message);
            cause.printStackTrace();
        }
    }
}

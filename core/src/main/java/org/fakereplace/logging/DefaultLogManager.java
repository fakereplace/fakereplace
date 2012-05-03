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

import java.util.Arrays;
import java.util.Locale;

import org.fakereplace.AgentOption;
import org.fakereplace.AgentOptions;

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

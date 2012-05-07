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

package org.fakereplace.api;

import org.fakereplace.core.DefaultEnvironment;

/**
 * Holds the current environment.
 *
 * @author Stuart Douglas
 */
public class CurrentEnvironment {
    protected static volatile Environment environment = new DefaultEnvironment();

    /**
     *
     * @return The current environment
     */
    public static Environment getEnvironment() {
        return environment;
    }

    public static void setEnvironment(final Environment environment) {
        CurrentEnvironment.environment = environment;
    }
}

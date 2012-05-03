/*
 *
 *  * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 *  * by the @authors tag.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.fakereplace;

/**
 * Entry point when running the core tests.
 *
 * This is used to prevent confusion, so people do not use the core jar without the integrations by mistake.
 *
 * @author Stuart Douglas
 */
public class TestAgent  {

    public static void premain(java.lang.String s, java.lang.instrument.Instrumentation i) {
        if(s == null || !s.contains("testRun")) {
            throw new IllegalStateException("You should not use Fakereplace core directly, you should use the fakereplace.jar found in the dist directory.");
        }
        Agent.premain(s, i);
    }
}
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

package org.fakereplace.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns a method number for a generated method. Methods with the same name
 * and descriptor are assigned the same number to make emulating virtual calls
 * easier. The redifined method can call super.REDEFINED_METHOD with the same
 * method number and if the method exists on the superclass then it is handled
 * automatically
 *
 * @author Stuart Douglas <stuart.w.douglas@gmail.com>
 */
public class MethodIdentifierStore {

    private static final MethodIdentifierStore INSTANCE = new MethodIdentifierStore();

    private final Map<String, Map<String, Integer>> data = new HashMap<String, Map<String, Integer>>();

    private int methodNo = 0;

    private MethodIdentifierStore() {

    }

    public synchronized int getMethodNumber(String name, String descriptor) {
        if (!data.containsKey(name)) {
            data.put(name, new HashMap<String, Integer>());
        }
        Map<String, Integer> im = data.get(name);
        if (!im.containsKey(descriptor)) {
            im.put(descriptor, methodNo++);
        }
        return im.get(descriptor);
    }

    public static MethodIdentifierStore instance() {
        return INSTANCE;
    }

}

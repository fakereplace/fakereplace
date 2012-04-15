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

import java.util.Map;
import java.util.Set;

import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.manip.util.MapFunction;

/**
 * This class is responsible for tracking instances of certain classes as they
 * are loaded
 *
 * @author stuart
 */
public class InstanceTracker {

    private static Object TEMP = new Object();

    private static Map<String, Map<Object, Object>> data = new MapMaker().weakKeys().initialCapacity(100).makeComputingMap(new MapFunction<Object, Object, Object>(true));

    public static void add(String type, Object object) {
        Map<Object, Object> set = data.get(type);
        set.put(object, TEMP);
    }

    public static Set<?> get(String type) {
        return data.get(type).keySet();
    }
}

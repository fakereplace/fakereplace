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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fakereplace.com.google.common.collect.MapMaker;

/**
 * This class is responsible for tracking instances of certain classes as they
 * are loaded
 *
 * @author stuart
 */
public class InstanceTracker {

    private static ConcurrentMap<String, Set<Object>> data = new ConcurrentHashMap<String, Set<Object>>();

    public static void add(String type, Object object) {
        Set<Object> set = data.get(type);
        if(set == null) {
            set = Collections.newSetFromMap(new MapMaker().weakKeys().<Object, Boolean>makeMap());
            Set<Object> existing = data.putIfAbsent(type, set);
            if(existing != null) {
                set = existing;
            }
        }
        set.add(object);
    }

    public static Set<?> get(String type) {
        final Set<Object> result =  data.get(type);
        if(result != null) {
            return result;
        }
        return Collections.emptySet();
    }
}

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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that provides access to arguments that were passed to the java agent.
 *
 * @author Stuart Douglas
 */
public class AgentOptions {

    private static volatile AgentOptions instance;
    private final Map<String, String> options;

    public AgentOptions(final Map<String, String> options) {
        this.options = Collections.unmodifiableMap(options);
    }

    static void setup(final String options) {
        final Map<String, String> map = new HashMap<String, String>();
        if (options != null) {
            final String[] parts = options.split(",");
            for (final String part : parts) {
                int index = part.indexOf("=");
                if (index == -1) {
                    map.put(part, null);
                } else {
                    map.put(part.substring(0, index), part.substring(index + 1));
                }
            }
        }
        instance = new AgentOptions(map);
    }

    public static boolean set(AgentOption option) {
        return instance.options.containsKey(option.getKey());
    }

    public static String getOption(AgentOption option) {
        if (instance == null) {
            throw new IllegalStateException("setup() not called");
        }
        return instance.options.get(option.getKey());
    }
}

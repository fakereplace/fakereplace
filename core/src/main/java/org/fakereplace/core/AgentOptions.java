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

package org.fakereplace.core;

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
        if(!instance.options.containsKey(option.getKey())) {
            return option.getDefaultValue();
        }
        return instance.options.get(option.getKey());
    }
}

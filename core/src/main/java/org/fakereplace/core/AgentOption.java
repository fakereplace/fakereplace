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

package org.fakereplace.core;

/**
 * Options that can be passed to the javaagent
 *
 * @author Stuart Douglas
 */
public enum AgentOption {
    INDEX_FILE("index-file", "fakereplace.index"),
    DUMP_DIR("dump-dir"),
    PACKAGES("packages"),
    LOG("log"),
    PORT("port", "6555"),
    ;

    private final String key;
    private final String defaultValue;

    private AgentOption(final String key) {
        this.key = key;
        this.defaultValue = null;
    }

    private AgentOption(final String key, final String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}

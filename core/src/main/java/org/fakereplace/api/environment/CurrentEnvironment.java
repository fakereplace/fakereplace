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

package org.fakereplace.api.environment;

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

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
        Fakereplace.premain(s, i);
    }
}

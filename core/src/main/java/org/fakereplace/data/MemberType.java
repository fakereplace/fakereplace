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

package org.fakereplace.data;

public enum MemberType {
    /**
     * normal methods are methods from the java source code
     */
    NORMAL,
    /**
     * fake methods are methods that we are pretending exist
     */
    FAKE,

    FAKE_CONSTRUCTOR,
    /**
     * This is a method that we have to implement with a noop as it was removed
     * from the source
     */
    REMOVED,
    /**
     * This is a method that has been added that should not be visible to the
     * user
     */
    ADDED_SYSTEM;
}

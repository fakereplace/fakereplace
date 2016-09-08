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

package org.fakereplace.util;

import javassist.bytecode.AccessFlag;

public class AccessFlagUtils {
    private AccessFlagUtils() {
    }

    public static boolean downgradeVisibility(int n, int o) {
        if (AccessFlag.isPrivate(n) && !AccessFlag.isPrivate(o)) {
            return true;
        }
        if (AccessFlag.isPublic(o) && !AccessFlag.isPublic(n)) {
            return true;
        }
        if (AccessFlag.isProtected(o) != AccessFlag.isProtected(n)) {
            return true;
        }
        if (AccessFlag.isPackage(o) != AccessFlag.isPackage(n)) {
            return true;
        }
        return false;
    }

    public static boolean upgradeVisibility(int n, int o) {
        if (AccessFlag.isPrivate(o) && !AccessFlag.isPrivate(n)) {
            return true;
        }
        if (AccessFlag.isPublic(n) && !AccessFlag.isPublic(o)) {
            return true;
        }
        return false;
    }

    static final int INVERSE_ACCESS_FLAGS = ~(AccessFlag.PRIVATE | AccessFlag.PUBLIC | AccessFlag.PROTECTED);
}

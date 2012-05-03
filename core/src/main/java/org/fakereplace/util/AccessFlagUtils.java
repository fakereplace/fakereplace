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

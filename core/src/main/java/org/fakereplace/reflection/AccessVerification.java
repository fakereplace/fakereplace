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

package org.fakereplace.reflection;

import java.lang.reflect.Modifier;

import sun.reflect.Reflection;

/**
 * @author Stuart Douglas
 */
class AccessVerification {

    static void ensureMemberAccess(Class<?> caller, Class<?> declaring, int modifiers) throws IllegalAccessException {
        if (caller != null && declaring != null) {
            if (!verifyMemberAccess(caller, declaring, modifiers)) {
                throw new IllegalAccessException("Class " + caller.getName() + " can not access a member of class " + declaring.getName() + " with modifiers \"" + Modifier.toString(modifiers) + "\"");
            }
        } else {
            throw new InternalError();
        }
    }

    private static boolean verifyMemberAccess(Class<?> caller, Class<?> declaring, int modifiers) {
        Boolean samePackage = null;

        if (caller == declaring) {
            return true;
        }

        if (!Modifier.isPublic(declaring.getModifiers())) {
            samePackage = samePackage(caller, declaring);
            if (!samePackage) {
                return false;
            }
        }
        if (Modifier.isPublic(modifiers)) {
            return true;
        } else if (Modifier.isPrivate(modifiers)) {
            return false;
        } else if (Modifier.isProtected(modifiers)) {
            if (samePackage == null) {
                samePackage = samePackage(caller, declaring);
            }
            if (samePackage) {
                return true;
            }
            if (isSubclass(declaring, caller)) {
                return true;
            }
            return false;
        } else {
            if (samePackage == null) {
                samePackage = samePackage(caller, declaring);
            }
            return samePackage;
        }
    }

    private static boolean samePackage(Class<?> c1, Class<?> c2) {
        if (c1.getClassLoader() != c2.getClassLoader()) {
            return false;
        } else {
            String name1 = c1.getName();
            String name2 = c2.getName();
            int dotPos1 = name1.lastIndexOf('.');
            int dotPos2 = name2.lastIndexOf('.');
            if (dotPos1 == -1 && dotPos2 == -1) {
                return true; //both have no package
            } else if (dotPos1 == -1 || dotPos2 == -1) {
                return false; //one has no package
            } else {
                int start1 = 0;
                int start2 = 0;
                while (name1.charAt(start1) == '[') {
                    ++start1;
                }
                while (name2.charAt(start2) == '[') {
                    ++start2;
                }
                int l1 = dotPos1 - start1;
                int l2 = dotPos1 - start1;
                if (l1 != l2) {
                    return false;
                }
                return name1.regionMatches(false, start1, name2, start2, l2);
            }
        }
    }

    private static boolean isSubclass(Class<?> candidate, Class<?> target) {
        while (candidate != null) {
            if (candidate == target) {
                return true;
            }

            candidate = candidate.getSuperclass();
        }

        return false;
    }

    static Class<?> getCallerClass(int pos) {
        return Reflection.getCallerClass(pos);
    }
}

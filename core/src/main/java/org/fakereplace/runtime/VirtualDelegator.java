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

package org.fakereplace.runtime;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.fakereplace.boot.Constants;
import org.fakereplace.boot.DefaultEnvironment;

public class VirtualDelegator {

    /**
     * stores information about which methods need to be delegated to. This data
     * is not needed to actually call the new method, as we can just look up the
     * method no from the MethodIdentifierStore
     */
    private static final Set<VirtualDelegatorData> delegatingMethods = new CopyOnWriteArraySet<VirtualDelegatorData>();

    public static void add(ClassLoader loader, String className, String methodName, String methodDesc) {
        delegatingMethods.add(new VirtualDelegatorData(loader, className, methodName, methodDesc));
    }

    public static void clear(ClassLoader classLoader, String className) {
        Iterator<VirtualDelegatorData> it = delegatingMethods.iterator();
        while (it.hasNext()) {
            VirtualDelegatorData i = it.next();
            if (i.getLoader() == classLoader && className.equals(i.getClassName())) {
                it.remove();
            }
        }
    }

    public static boolean contains(Object val, String callingClassName, String methodName, String methodDesc) {
        if (!DefaultEnvironment.getEnvironment().isClassReplaceable(val.getClass().getName(), val.getClass().getClassLoader())) {
            return false;
        }
        Class<?> c = val.getClass();
        while (true) {
            if (c.getName().equals(callingClassName)) {
                return false;
            }
            VirtualDelegatorData i = new VirtualDelegatorData(c.getClassLoader(), c.getName(), methodName, methodDesc);
            if (delegatingMethods.contains(i)) {
                return true;
            }
            c = c.getSuperclass();
        }
    }

    public static Object run(Object val, String methodName, String methodDesc, Object[] params) {
        try {
            Method meth = val.getClass().getMethod(Constants.ADDED_METHOD_NAME, int.class, Object[].class);
            int methodIdentifier = MethodIdentifierStore.instance().getMethodNumber(methodName, methodDesc);
            return meth.invoke(val, methodIdentifier, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class VirtualDelegatorData {
        private final ClassLoader loader;
        private final String className;
        private final String methodName;
        private final String methodDesc;

        public ClassLoader getLoader() {
            return loader;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getMethodDesc() {
            return methodDesc;
        }

        public VirtualDelegatorData(ClassLoader loader, String className, String methodName, String methodDesc) {
            super();
            this.loader = loader;
            this.className = className;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((className == null) ? 0 : className.hashCode());
            result = prime * result + ((methodDesc == null) ? 0 : methodDesc.hashCode());
            result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            VirtualDelegatorData other = (VirtualDelegatorData) obj;
            if (className == null) {
                if (other.className != null)
                    return false;
            } else if (!className.equals(other.className))
                return false;
            if (loader == null) {
                if (other.loader != null)
                    return false;
            } else if (!loader.equals(other.loader))
                return false;
            if (methodDesc == null) {
                if (other.methodDesc != null)
                    return false;
            } else if (!methodDesc.equals(other.methodDesc))
                return false;
            if (methodName == null) {
                if (other.methodName != null)
                    return false;
            } else if (!methodName.equals(other.methodName))
                return false;
            return true;
        }

    }
}

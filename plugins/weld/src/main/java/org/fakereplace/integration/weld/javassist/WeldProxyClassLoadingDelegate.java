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

package org.fakereplace.integration.weld.javassist;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.instrument.ClassDefinition;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.bytecode.ClassFile;
import org.fakereplace.Agent;
import org.fakereplace.replacement.AddedClass;
import org.jboss.weld.util.bytecode.ClassFileUtils;

/**
 * The CDI proxyFactory has its class loading tasks delegated to this class, which can then have some magic applied
 * to make weld think that the class has not been loaded yet.
 *
 * @author Stuart Douglas
 */
public class WeldProxyClassLoadingDelegate {

    private static final ThreadLocal<Boolean> MAGIC_IN_PROGRESS = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    public static final void beginProxyRegeneration() {
        MAGIC_IN_PROGRESS.set(true);
    }

    public static final void endProxyRegeneration() {
        MAGIC_IN_PROGRESS.remove();
    }

    public static Class<?> loadClass(final ClassLoader classLoader, final String className) throws ClassNotFoundException {
        //pretend that the proxy does not exist
        if (MAGIC_IN_PROGRESS.get()) {
            throw new ClassNotFoundException("fakereplace");
        }
        return classLoader.loadClass(className);
    }

    public static Class toClass(ClassFile ct, ClassLoader loader, ProtectionDomain domain) throws CannotCompileException {
        if (MAGIC_IN_PROGRESS.get()) {
            try {
                final Class<?> originalProxyClass = loader.loadClass(ct.getName());
                try {
                    ByteArrayOutputStream bs = new ByteArrayOutputStream();
                    ct.write(new DataOutputStream(bs));
                    Agent.redefine(new ClassDefinition[]{new ClassDefinition(originalProxyClass, bs.toByteArray())}, new AddedClass[0]);
                    return originalProxyClass;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (ClassNotFoundException e) {
                //it has not actually been loaded yet
                return ClassFileUtils.toClass(ct, loader, domain);
            }
        }
        return ClassFileUtils.toClass(ct, loader, domain);
    }

}

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

package org.fakereplace.integration.weld.javassist;

import java.lang.instrument.ClassDefinition;
import java.security.ProtectionDomain;

import org.fakereplace.core.Agent;
import org.fakereplace.replacement.AddedClass;
import org.jboss.classfilewriter.ClassFile;
import org.jboss.classfilewriter.util.ByteArrayDataOutputStream;
import org.jboss.weld.util.bytecode.ClassFileUtils;
import javassist.CannotCompileException;

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

    public static void beginProxyRegeneration() {
        MAGIC_IN_PROGRESS.set(true);
    }

    public static void endProxyRegeneration() {
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
                    ByteArrayDataOutputStream bs = new ByteArrayDataOutputStream();
                    ct.write(bs);
                    Agent.redefine(new ClassDefinition[]{new ClassDefinition(originalProxyClass, bs.getBytes())}, new AddedClass[0], false);
                    return originalProxyClass;
                } catch (Exception e) {
                    throw new RuntimeException("Failed " + ct.getName(), e);
                }
            } catch (ClassNotFoundException e) {
                //it has not actually been loaded yet
                return ClassFileUtils.toClass(ct, loader, domain);
            }
        }
        return ClassFileUtils.toClass(ct, loader, domain);
    }

}

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

package org.fakereplace.integration.weld;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;

import org.fakereplace.integration.weld.javassist.WeldProxyClassLoadingDelegate;
import org.fakereplace.logging.Logger;
import org.fakereplace.manip.VirtualToStaticManipulator;
import org.fakereplace.replacement.notification.ChangedClassImpl;
import org.fakereplace.core.FakereplaceTransformer;
import org.fakereplace.util.DescriptorUtils;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;

/**
 * @author Stuart Douglas
 */
public class WeldClassTransformer implements FakereplaceTransformer {

    private static final Logger log = Logger.getLogger(WeldClassTransformer.class);
    public static final String ORG_JBOSS_WELD_BEAN_PROXY_PROXY_FACTORY = "org.jboss.weld.bean.proxy.ProxyFactory";

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods) throws IllegalClassFormatException, BadBytecode {

        /**
         * Hack up the proxy factory so it stores the proxy ClassFile. We need this to regenerate proxies.
         */
        if (file.getName().equals(ORG_JBOSS_WELD_BEAN_PROXY_PROXY_FACTORY)) {
            for (final MethodInfo method : (List<MethodInfo>) file.getMethods()) {
                if (method.getName().equals("createProxyClass")) {

                    modifiedMethods.add(method);
                    final VirtualToStaticManipulator virtualToStaticManipulator = new VirtualToStaticManipulator();
                    virtualToStaticManipulator.replaceVirtualMethodInvokationWithStatic(ClassLoader.class.getName(), WeldProxyClassLoadingDelegate.class.getName(), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;", loader);
                    virtualToStaticManipulator.replaceVirtualMethodInvokationWithStatic("org.jboss.weld.util.bytecode.ClassFileUtils", WeldProxyClassLoadingDelegate.class.getName(), "toClass", "(Lorg/jboss/classfilewriter/ClassFile;Ljava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", "(Lorg/jboss/classfilewriter/ClassFile;Ljava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", loader);
                    virtualToStaticManipulator.transformClass(file, loader, true, modifiedMethods);
                    return true;
                } else if (method.getName().equals("<init>")) {

                    modifiedMethods.add(method);
                    Integer beanArgument = null;
                    int count = 1;
                    for (final String paramType : DescriptorUtils.descriptorStringToParameterArray(method.getDescriptor())) {
                        if (paramType.equals("Ljavax/enterprise/inject/spi/Bean")) {
                            beanArgument = count;
                            break;
                        } else if (paramType.equals("D") || paramType.equals("J")) {
                            count += 2;
                        } else {
                            count++;
                        }
                    }
                    if (beanArgument == null) {
                        log.error("Constructor org.jboss.weld.bean.proxy.ProxyFactory.<init>" + method.getDescriptor() + " does not have a bean parameter, proxies produced by this factory will not be reloadable");
                        continue;
                    }

                    //similar to other tracked instances
                    //but we need a strong ref
                    Bytecode code = new Bytecode(file.getConstPool());
                    code.addAload(0);
                    code.addAload(beanArgument);
                    code.addInvokestatic(WeldClassChangeAware.class.getName(), "addProxyFactory", "(Ljava/lang/Object;Ljava/lang/Object;)V");
                    CodeIterator it = method.getCodeAttribute().iterator();
                    it.skipConstructor();
                    it.insert(code.get());
                }
            }
        }
        return false;
    }
}

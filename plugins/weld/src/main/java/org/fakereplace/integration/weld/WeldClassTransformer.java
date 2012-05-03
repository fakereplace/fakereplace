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

package org.fakereplace.integration.weld;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import org.fakereplace.integration.weld.javassist.WeldProxyClassLoadingDelegate;
import org.fakereplace.logging.Logger;
import org.fakereplace.manip.MethodInvokationManipulator;
import org.fakereplace.transformation.FakereplaceTransformer;
import org.fakereplace.util.DescriptorUtils;

/**
 * @author Stuart Douglas
 */
public class WeldClassTransformer implements FakereplaceTransformer {

    private static final Logger log = Logger.getLogger(WeldClassTransformer.class);

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file) throws IllegalClassFormatException {

        /**
         * Hack up the proxy factory so it stores the proxy ClassFile. We need this to regenerate proxies.
         */
        if (file.getName().equals("org.jboss.weld.bean.proxy.ProxyFactory")) {
            for (final MethodInfo method : (List<MethodInfo>) file.getMethods()) {
                if (method.getName().equals("createProxyClass")) {
                    final MethodInvokationManipulator methodInvokationManipulator = new MethodInvokationManipulator();
                    methodInvokationManipulator.replaceVirtualMethodInvokationWithStatic(ClassLoader.class.getName(), WeldProxyClassLoadingDelegate.class.getName(), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;", loader);
                    methodInvokationManipulator.replaceVirtualMethodInvokationWithStatic("org.jboss.weld.util.bytecode.ClassFileUtils", WeldProxyClassLoadingDelegate.class.getName(), "toClass", "(Ljavassist/bytecode/ClassFile;Ljava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", "(Ljavassist/bytecode/ClassFile;Ljava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", loader);
                    methodInvokationManipulator.transformClass(file, loader, true);
                    return true;
                } else if (method.getName().equals("<init>")) {

                    Integer beanArgument = null;
                    int count = 0;
                    for(final String paramType  : DescriptorUtils.descriptorStringToParameterArray(method.getDescriptor())) {
                        if(paramType.equals("javax/enterprise/inject/spi/Bean")) {
                            beanArgument = count;
                            break;
                        } else if(paramType.equals("D") || paramType.equals("J")) {
                            count += 2;
                        } else {
                            count++;
                        }
                    }
                    if(beanArgument == null) {
                        log.error("Constructor org.jboss.weld.bean.proxy.ProxyFactory.<init>" + method.getDescriptor() + " does not have a bean parameter, proxies produced by this factory will not be reloadable");
                        continue;
                    }

                    //similar to other tracked instances
                    //but we need a strong ref
                    Bytecode code = new Bytecode(file.getConstPool());
                    code.addAload(0);
                    code.addAload(beanArgument);
                    code.addInvokestatic(WeldClassChangeAware.class.getName(), "addProxyFactory", "(Lorg/jboss/weld/bean/proxy/ProxyFactory;)V");
                    CodeIterator it = method.getCodeAttribute().iterator();
                    try {
                        it.skipConstructor();
                        it.insert(code.get());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return false;
    }
}

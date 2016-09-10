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

package org.fakereplace.integration.wildfly;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import org.fakereplace.replacement.notification.ChangedClassImpl;
import org.fakereplace.transformation.FakereplaceTransformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;

/**
 * @author Stuart Douglas
 */
public class WildflyClassTransformer implements FakereplaceTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if(!file.getName().equals("org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService")) {
            return false;
        }
        for(MethodInfo method : (List<MethodInfo>)file.getMethods()) {
            if(method.getName().equals("createServletConfig")) {
                CodeAttribute code = method.getCodeAttribute();
                code.setMaxStack(code.getMaxStack() + 1);
                CodeIterator it = code.iterator();
                modifiedMethods.add(method);
                while (it.hasNext()) {
                    int pos = it.next();
                    int inst = it.byteAt(pos);
                    if(inst == CodeAttribute.ARETURN) {
                        Bytecode b = new Bytecode(method.getConstPool());
                        b.addGetstatic("org.fakereplace.integration.wildfly.autoupdate.WebUpdateHandlerWrapper", "INSTANCE", "Lio/undertow/server/HandlerWrapper;");
                        b.addInvokevirtual("io.undertow.servlet.api.DeploymentInfo", "addInnerHandlerChainWrapper", "(Lio/undertow/server/HandlerWrapper;)Lio/undertow/servlet/api/DeploymentInfo;");
                        it.insert(pos, b.get());
                    }
                }
            }
        }

        return true;
    }
}

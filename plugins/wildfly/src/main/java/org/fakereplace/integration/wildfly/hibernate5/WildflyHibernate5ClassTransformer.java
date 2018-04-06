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

package org.fakereplace.integration.wildfly.hibernate5;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;

import org.fakereplace.replacement.notification.ChangedClassImpl;
import org.fakereplace.core.FakereplaceTransformer;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

/**
 * Class transformer that intercepts invocations of the HibernatePersistence create* methods.
 *
 * @author Stuart Douglas
 */
public class WildflyHibernate5ClassTransformer implements FakereplaceTransformer {

    private static final String PROXY_NAME = "org.fakereplace.integration.wildfly.hibernate5.WildflyEntityManagerFactoryProxy";

    @Override
    public boolean transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods, boolean replaceable) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if (file.getName().equals("org.jboss.as.jpa.service.PersistenceUnitServiceImpl")) {
            for (MethodInfo method : (List<MethodInfo>) file.getMethods()) {
                if (method.getName().equals("getEntityManagerFactory")) {
                    modifiedMethods.add(method);

                    //need to save the method params so we can re-use them when we re-create our EMF
                    Bytecode s = new Bytecode(file.getConstPool());

                    //we need to interceptor the return value
                    //and add in our own bytecode fragment.
                    //first lets create our proxy creation code
                    final Bytecode b = new Bytecode(file.getConstPool());
                    b.addNew(PROXY_NAME);
                    b.add(Opcode.DUP);
                    b.addAload(0);
                    b.addInvokespecial(PROXY_NAME, "<init>", "(Lorg/jipijapa/plugin/spi/PersistenceUnitService;)V");
                    insertBeforeReturn(method, s, b);
                }
            }
            file.addInterface(HackPersistenceUnitService.class.getName());
            Bytecode bc = new Bytecode(file.getConstPool(), 1, 1);
            bc.addAload(0);
            bc.addGetfield("org.jboss.as.jpa.service.PersistenceUnitServiceImpl", "entityManagerFactory", "Ljavax/persistence/EntityManagerFactory;");
            bc.addOpcode(Bytecode.ARETURN);
            MethodInfo methodInfo = new MethodInfo(file.getConstPool(), "emf", "()Ljavax/persistence/EntityManagerFactory;");
            methodInfo.setAccessFlags(AccessFlag.PUBLIC);
            methodInfo.setCodeAttribute(bc.toCodeAttribute());
            file.addMethod(methodInfo);
            modifiedMethods.add(methodInfo);
            return true;
        } else {
            return false;
        }
    }

    private void insertBeforeReturn(final MethodInfo method, final Bytecode s, final Bytecode b) throws BadBytecode {
        final CodeIterator itr = method.getCodeAttribute().iterator();
        itr.insert(s.get());
        while (itr.hasNext()) {
            final int pos = itr.next();
            int opcode = itr.byteAt(pos);
            if (opcode == Opcode.ARETURN) {
                itr.insert(pos, b.get());
            }
        }
        method.getCodeAttribute().computeMaxStack();
    }
}

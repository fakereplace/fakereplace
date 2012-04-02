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

package org.fakereplace.manip.staticfield;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.SignatureAttribute;
import org.fakereplace.classloading.ProxyDefinitionStore;
import org.fakereplace.com.google.common.base.Function;
import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.data.ClassDataStore;

/**
 * Factory that generated classes to hold added static field instances. If a
 * request comes for a field with the same name, type and signiture
 * then the same (existing) class is returned, so that static fields can hold
 * their values across replacements
 * <p/>
 * Modifiers and annotations are ignored, these are handled by instrumentation
 * of the reflection API
 *
 * @author stuart
 */
public class StaticFieldClassFactory {
    private static final Map<StaticFieldDescriptor, String> fieldClasses = new MapMaker().makeComputingMap(new Function<StaticFieldDescriptor, String>() {

        public String apply(StaticFieldDescriptor from) {
            // this is quite simple. First we create a proxy
            String proxyName = ProxyDefinitionStore.getProxyName();
            ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
            ClassDataStore.instance().registerProxyName(from.getClazz(), proxyName);
            proxy.setAccessFlags(AccessFlag.PUBLIC);
            FieldInfo newField = new FieldInfo(proxy.getConstPool(), from.getName(), from.getDescriptor());
            newField.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
            if (from.getSigniture() != null) {
                SignatureAttribute sig = new SignatureAttribute(proxy.getConstPool(), from.getSigniture());
                newField.addAttribute(sig);
            }
            try {
                proxy.addField(newField);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bytes);
                try {
                    proxy.write(dos);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ProxyDefinitionStore.saveProxyDefinition(from.getClazz().getClassLoader(), proxyName, bytes.toByteArray());
            } catch (DuplicateMemberException e) {
                // can't happen
            }
            return proxyName;
        }
    });

    public static String getStaticFieldClass(Class<?> clazz, String fieldName, String fieldDescriptor, String signiture) {
        StaticFieldDescriptor d = new StaticFieldDescriptor(clazz, fieldDescriptor, fieldName, signiture);
        return fieldClasses.get(d);
    }

}

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

package org.fakereplace.integration.resteasy;

import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Set;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import org.fakereplace.replacement.notification.ChangedClassImpl;
import org.fakereplace.transformation.FakereplaceTransformer;

/**
 * @author Stuart Douglas
 */
public class ResteasyTransformer implements FakereplaceTransformer {

    public static final String FIELD_NAME = "__CONFIG";
    public static final String PARAMETER_FIELD_NAME = "__PARAMS";
    public static final String FILTER_FIELD_TYPE = "Ljavax/servlet/FilterConfig;";
    public static final String SERVLET_FIELD_TYPE = "Ljavax/servlet/ServletConfig;";
    public static final String CONTEXT_PARAMS = "org.fakereplace.integration.resteasy.ResteasyContextParams";
    public static final String SET_TYPE = "Ljava/util/Set;";
    public static final String INIT_METHOD_DESC = "(Ljavax/servlet/ServletContext;Ljava/util/Set;)Ljava/util/Set;";
    public static final String RESTEASY_FILTER_CONFIG = "org.fakereplace.integration.resteasy.ResteasyFilterConfig";
    public static final String RESTEASY_SERVLET_CONFIG = "org.fakereplace.integration.resteasy.ResteasyServletConfig";

    @Override
    public boolean transform(final ClassLoader loader, final String className, final Class<?> classBeingRedefined, final ProtectionDomain protectionDomain, final ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods) throws IllegalClassFormatException, BadBytecode {

        //we need to change the filter and servlet dispatchers to
        //capture the config they are initalized with.

        //we can then re-initalize them with this same info

        try {
            if (file.getName().equals(ResteasyExtension.FILTER_DISPATCHER)) {
                FieldInfo field = new FieldInfo(file.getConstPool(), FIELD_NAME, FILTER_FIELD_TYPE);
                field.setAccessFlags(Modifier.PUBLIC);
                file.addField(field);
                field = new FieldInfo(file.getConstPool(), PARAMETER_FIELD_NAME, SET_TYPE);
                field.setAccessFlags(Modifier.PUBLIC);
                file.addField(field);
                for (final MethodInfo method : (List<MethodInfo>) file.getMethods()) {
                    if (method.getName().equals("init") &&
                            method.getDescriptor().equals("(Ljavax/servlet/FilterConfig;)V")) {

                        modifiedMethods.add(method);
                        final Bytecode b = new Bytecode(file.getConstPool());
                        b.addAload(0);
                        b.addNew(RESTEASY_FILTER_CONFIG);
                        b.add(Opcode.DUP);
                        b.addAload(1);
                        b.addInvokespecial(RESTEASY_FILTER_CONFIG, "<init>", "(Ljavax/servlet/FilterConfig;)V");
                        b.addPutfield(ResteasyExtension.FILTER_DISPATCHER, FIELD_NAME, FILTER_FIELD_TYPE);
                        b.addAload(1);
                        b.addInvokeinterface("javax/servlet/FilterConfig", "getServletContext", "()Ljavax/servlet/ServletContext;", 1);
                        b.addAload(0);
                        b.addGetfield(ResteasyExtension.FILTER_DISPATCHER, PARAMETER_FIELD_NAME, SET_TYPE);
                        b.addInvokestatic(CONTEXT_PARAMS, "init", INIT_METHOD_DESC);
                        b.addAload(0);
                        b.add(Opcode.SWAP);
                        b.addPutfield(ResteasyExtension.FILTER_DISPATCHER, PARAMETER_FIELD_NAME, SET_TYPE);
                        method.getCodeAttribute().iterator().insert(b.get());
                        method.getCodeAttribute().computeMaxStack();
                    } else if(method.getName().equals("<init>")) {
                        //no idea why this is needed
                        method.getCodeAttribute().setMaxStack(1);
                    }
                }
                return true;
            } else if (file.getName().equals(ResteasyExtension.SERVLET_DISPATCHER)) {
                FieldInfo field = new FieldInfo(file.getConstPool(), FIELD_NAME, SERVLET_FIELD_TYPE);
                field.setAccessFlags(Modifier.PUBLIC);
                file.addField(field);
                field = new FieldInfo(file.getConstPool(), PARAMETER_FIELD_NAME, SET_TYPE);
                field.setAccessFlags(Modifier.PUBLIC);
                file.addField(field);
                for (final MethodInfo method : (List<MethodInfo>) file.getMethods()) {
                    if (method.getName().equals("init") &&
                            method.getDescriptor().equals("(Ljavax/servlet/ServletConfig;)V")) {

                        modifiedMethods.add(method);
                        final Bytecode b = new Bytecode(file.getConstPool());
                        b.addAload(0);
                        b.addNew(RESTEASY_SERVLET_CONFIG);
                        b.add(Opcode.DUP);
                        b.addAload(1);
                        b.addInvokespecial(RESTEASY_SERVLET_CONFIG, "<init>", "(Ljavax/servlet/ServletConfig;)V");
                        b.addPutfield(ResteasyExtension.SERVLET_DISPATCHER, FIELD_NAME, SERVLET_FIELD_TYPE);
                        b.addAload(1);
                        b.addInvokeinterface("javax/servlet/ServletConfig", "getServletContext", "()Ljavax/servlet/ServletContext;", 1);
                        b.addAload(0);
                        b.addGetfield(ResteasyExtension.SERVLET_DISPATCHER, PARAMETER_FIELD_NAME, SET_TYPE);
                        b.addInvokestatic(CONTEXT_PARAMS, "init", INIT_METHOD_DESC);
                        b.addAload(0);
                        b.add(Opcode.SWAP);
                        b.addPutfield(ResteasyExtension.SERVLET_DISPATCHER, PARAMETER_FIELD_NAME, SET_TYPE);
                        method.getCodeAttribute().iterator().insert(b.get());
                        method.getCodeAttribute().computeMaxStack();
                    } else if(method.getName().equals("<init>")) {
                        method.getCodeAttribute().setMaxStack(1);
                    }
                }
                return true;
            }
        } catch (DuplicateMemberException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}

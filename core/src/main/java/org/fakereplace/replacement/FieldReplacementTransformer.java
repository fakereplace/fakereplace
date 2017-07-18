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

package org.fakereplace.replacement;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Set;

import org.fakereplace.core.ProxyDefinitionStore;
import org.fakereplace.core.Transformer;
import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.FieldData;
import org.fakereplace.data.MemberType;
import org.fakereplace.reflection.FieldAccessor;
import org.fakereplace.replacement.notification.ChangedClassImpl;
import org.fakereplace.runtime.FieldReferenceDataStore;
import org.fakereplace.core.FakereplaceTransformer;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;

public class FieldReplacementTransformer implements FakereplaceTransformer {


    /**
     * This will create a proxy with a non static field. This field does not
     * store anything, it merely provides a Field object for reflection. Attempts
     * to change and read it's value are redirected to the actual array based
     * store
     *
     */
    private static int addField(ClassLoader loader, FieldInfo m, Set<FieldProxyInfo> builder, Class<?> oldClass) {
        int fieldNo = FieldReferenceDataStore.instance().getFieldNo(m.getName(), m.getDescriptor());
        String proxyName = ProxyDefinitionStore.getProxyName();
        ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
        ClassDataStore.instance().registerProxyName(oldClass, proxyName);
        FieldAccessor accessor = new FieldAccessor(oldClass, fieldNo, (m.getAccessFlags() & AccessFlag.STATIC) != 0);
        ClassDataStore.instance().registerFieldAccessor(proxyName, accessor);
        proxy.setAccessFlags(AccessFlag.PUBLIC);
        FieldInfo newField = new FieldInfo(proxy.getConstPool(), m.getName(), m.getDescriptor());
        newField.setAccessFlags(m.getAccessFlags());

        copyFieldAttributes(m, newField);

        try {
            proxy.addField(newField);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bytes);
            try {
                proxy.write(dos);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ProxyDefinitionStore.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
            builder.add(new FieldProxyInfo(newField, proxyName, m.getAccessFlags()));
        } catch (DuplicateMemberException e) {
            // can't happen
        }
        return fieldNo;
    }


    private static void copyFieldAttributes(FieldInfo oldField, FieldInfo newField) {
        AnnotationsAttribute annotations = (AnnotationsAttribute) oldField.getAttribute(AnnotationsAttribute.visibleTag);
        SignatureAttribute sigAt = (SignatureAttribute) oldField.getAttribute(SignatureAttribute.tag);

        if (annotations != null) {
            AttributeInfo newAnnotations = annotations.copy(newField.getConstPool(), Collections.EMPTY_MAP);
            newField.addAttribute(newAnnotations);
        }
        if (sigAt != null) {
            AttributeInfo newAnnotations = sigAt.copy(newField.getConstPool(), Collections.EMPTY_MAP);
            newField.addAttribute(newAnnotations);
        }

    }

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> oldClass, ProtectionDomain protectionDomain, ClassFile file, Set<Class<?>> classesToRetransform, ChangedClassImpl changedClass, Set<MethodInfo> modifiedMethods) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if (oldClass == null || className == null) {
            return false;
        }

        BaseClassData data = ClassDataStore.instance().getBaseClassData(loader, Descriptor.toJvmName(file.getName()));

        Set<FieldData> fields = new LinkedHashSet<>();
        fields.addAll(data.getFields());

        ListIterator<?> it = file.getFields().listIterator();


        final Set<FieldData> toRemove = new HashSet<>();
        final Set<FieldProxyInfo> toAdd = new HashSet<>();

        // now we iterator through all fields
        // in the process we modify the new class so that is's signature
        // is exactly compatible with the old class, otherwise an
        // IncompatibleClassChange exception will be thrown

        while (it.hasNext()) {
            FieldInfo m = (FieldInfo) it.next();
            FieldData md = null;
            for (FieldData i : fields) {
                if (i.getName().equals(m.getName()) && i.getType().equals(m.getDescriptor()) && i.getAccessFlags() == m.getAccessFlags()) {
                    try {
                        Field field = i.getField(oldClass);
                        AnnotationDataStore.recordFieldAnnotations(field, (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag));
                        // now revert the annotations:
                        m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), field));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    md = i;
                    break;
                }
            }
            // This is a newly added field.
            if (md == null) {
                int fieldNo = addField(loader, m, toAdd, oldClass);
                Transformer.getManipulator().rewriteInstanceFieldAccess(fieldNo, m.getName(), m.getDescriptor(), file.getName(), loader);
                it.remove();
            } else {
                fields.remove(md);
            }
        }
        // these fields have been removed,
        // TODO: rewrite classes that access them to throw a NoSuchFieldError
        for (FieldData md : fields) {
            if (md.getMemberType() == MemberType.NORMAL) {
                FieldInfo old = new FieldInfo(file.getConstPool(), md.getName(), md.getType());
                old.setAccessFlags(md.getAccessFlags());
                toRemove.add(md);
            }
        }
        //clear all the fields and re-add them in the correct order
        //turns out order is important
        file.getFields().clear();
        for (FieldData md : data.getFields()) {
            if (md.getMemberType() == MemberType.NORMAL) {
                try {
                    Field field = md.getField(oldClass);
                    FieldInfo old = new FieldInfo(file.getConstPool(), md.getName(), md.getType());
                    old.setAccessFlags(md.getAccessFlags());
                    file.addField(old);
                    old.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), field));
                } catch (DuplicateMemberException | SecurityException | NoSuchFieldException e) {
                    // this should not happen
                    throw new RuntimeException(e);
                }
            }
        }
        ClassDataStore.instance().modifyCurrentData(loader, file.getName(), (builder) -> {
            for (FieldProxyInfo field : toAdd) {
                builder.addFakeField(field.fieldData, field.proxyName, field.modifiers);
            }
            for (FieldData field : toRemove) {
                builder.removeField(field);
            }
        });
        return true;
    }

    private static class FieldProxyInfo {
        final FieldInfo fieldData;
        final String proxyName;
        final int modifiers;

        private FieldProxyInfo(FieldInfo fieldData, String proxyName, int modifiers) {
            this.fieldData = fieldData;
            this.proxyName = proxyName;
            this.modifiers = modifiers;
        }
    }

}

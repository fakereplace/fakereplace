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

package org.fakereplace.replacement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.Descriptor;
import org.fakereplace.boot.Logger;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataBuilder;
import org.fakereplace.data.ClassDataStore;

public class ClassRedefiner {
    static public ReplacementResult rewriteLoadedClasses(ClassDefinition... classDefinitions) {
        Set<ClassDefinition> defs = new HashSet<ClassDefinition>();
        Set<Class<?>> changedClasses = new HashSet<Class<?>>();
        Set<Class<?>> classesToReload = new HashSet<Class<?>>();
        for (int i = 0; i < classDefinitions.length; ++i) {
            try {
                ClassDefinition d = classDefinitions[i];
                ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(d.getDefinitionClassFile())));
                modifyReloadedClass(file, d.getDefinitionClass().getClassLoader(), d.getDefinitionClass(), classesToReload);
                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                file.write(new DataOutputStream(bs));
                changedClasses.add(d.getDefinitionClass());
                ClassDefinition n = new ClassDefinition(d.getDefinitionClass(), bs.toByteArray());
                defs.add(n);
            } catch (IOException e) {
                Logger.log(ClassRedefiner.class, "IO Error");
            }
        }
        classesToReload.removeAll(changedClasses);
        ClassDefinition[] ret = new ClassDefinition[defs.size()];
        int count = 0;
        for (ClassDefinition c : defs) {
            ret[count++] = c;
        }
        return new ReplacementResult(ret, classesToReload);

    }

    public static void modifyReloadedClass(ClassFile file, ClassLoader loader, Class<?> oldClass, Set<Class<?>> classToReload) {
        BaseClassData b = ClassDataStore.getBaseClassData(loader, Descriptor.toJvmName(file.getName()));
        if (b == null) {
            throw new RuntimeException("Could not find BaseClassData for " + file.getName());
        }

        if (!file.getSuperclass().equals(b.getSuperClassName())) {
            System.out.println("Superclass changed from " + b.getSuperClassName() + " to " + file.getSuperclass() + " in class " + file.getName());
        }

        ClassDataBuilder builder = new ClassDataBuilder(b);
        AnnotationReplacer.processAnnotations(file, oldClass, builder);
        FieldReplacer.handleFieldReplacement(file, loader, oldClass, builder);
        MethodReplacer.handleMethodReplacement(file, loader, oldClass, builder, classToReload);
        ClassDataStore.saveClassData(loader, file.getName(), builder);
    }

}

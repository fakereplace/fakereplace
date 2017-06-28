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

package org.fakereplace.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.data.ClassLoaderData;

public class ClassChangeNotifier {

    private static final ClassChangeNotifier INSTANCE = new ClassChangeNotifier();

    private static final ThreadLocal<Boolean> NOTIFICATION_IN_PROGRESS = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    private final ClassLoaderData.AttachmentKey<Set<ClassChangeAware>> classChangeAwares = new ClassLoaderData.AttachmentKey<>();

    public void add(ClassChangeAware aware) {
        ClassLoaderData cd = ClassLoaderData.get(aware.getClass().getClassLoader());
        Set<ClassChangeAware> set = cd.getAttachment(classChangeAwares);
        if(set == null) {
            cd.putAttachment(classChangeAwares, set = new HashSet<>());
        }
        set.add(aware);
    }

     public void afterChange(List<ChangedClass> changed, List<NewClassData> newClasses) {
        if (!NOTIFICATION_IN_PROGRESS.get()) {
            NOTIFICATION_IN_PROGRESS.set(true);
            try {
                Class<?>[] a = new Class[0];
                for (Set<ClassChangeAware> c : ClassLoaderData.allAttachment(classChangeAwares)) {
                    for (ClassChangeAware i : c) {
                        try {
                            i.afterChange(changed, newClasses);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } finally {
                NOTIFICATION_IN_PROGRESS.set(false);
            }
        }
    }

    public static ClassChangeNotifier instance() {
        return INSTANCE;
    }
}

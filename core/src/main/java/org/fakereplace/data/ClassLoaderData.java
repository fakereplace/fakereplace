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

package org.fakereplace.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * All data that is stored for a class loader.
 * <p>
 * This is stored in a weak map, to prevent class loader leaks. Where possible if the environment allows for it this
 * should be manually cleaned up.
 *
 * @author Stuart Douglas
 */
public class ClassLoaderData {

    public static final class AttachmentKey<T> {

    }

    private static final Map<ClassLoader, ClassLoaderData> DATA = Collections.synchronizedMap(new WeakHashMap<>());

    private final Map<String, ClassData> classData = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, BaseClassData> baseClassData = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, byte[]> proxyDefinitions = Collections.synchronizedMap(new HashMap<>());
    private final Map<AttachmentKey, Object> attachments = Collections.synchronizedMap(new HashMap<>());


    public static ClassLoaderData get(ClassLoader loader) {
        return DATA.computeIfAbsent(loader, (l) -> new ClassLoaderData());
    }

    public Map<String, ClassData> getClassData() {
        return classData;
    }

    public Map<String, BaseClassData> getBaseClassData() {
        return baseClassData;
    }

    public Map<String, byte[]> getProxyDefinitions() {
        return proxyDefinitions;
    }

    public <T> void putAttachment(AttachmentKey<T> attachmentKey, T value) {
        attachments.put(attachmentKey, value);
    }

    public <T> T getAttachment(AttachmentKey<T> attachmentKey) {
        return (T) attachments.get(attachmentKey);
    }
}

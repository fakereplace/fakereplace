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

package org.fakereplace.replacement.notification;

import org.fakereplace.api.ChangeType;
import org.fakereplace.api.Changed;

/**
 * Represents an addition, a removal or a modification
 *
 * @author Stuart Douglas
 */
public class ChangedImpl<T> implements Changed<T> {

    private final ChangeType type;
    private final T modified;
    private final T existing;

    public ChangedImpl(T modified, T existing, ChangeType type) {
        this.modified = modified;
        this.existing = existing;
        this.type = type;
    }

    @Override
    public ChangeType getType() {
        return type;
    }

    @Override
    public T getModified() {
        return modified;
    }

    @Override
    public T getExisting() {
        return existing;
    }
}

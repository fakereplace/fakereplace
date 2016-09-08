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

package org.fakereplace.client;

/**
* @author Stuart Douglas
*/
public final class ResourceData {
    private final long timestamp;
    private final String relativePath;
    private final ContentSource contentSource;

    public ResourceData(final String relativePath, long time, final ContentSource contentSource) {
        this.relativePath = relativePath;
        this.timestamp = time;
        this.contentSource = contentSource;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ContentSource getContentSource() {
        return contentSource;
    }
}

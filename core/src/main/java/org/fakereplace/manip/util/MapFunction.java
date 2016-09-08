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

package org.fakereplace.manip.util;

import java.util.concurrent.ConcurrentMap;

import org.fakereplace.com.google.common.base.Function;
import org.fakereplace.com.google.common.collect.MapMaker;

public class MapFunction<F, K, V> implements Function<F, ConcurrentMap<K, V>> {
    final boolean weakKeys;

    public MapFunction(boolean weakKeys) {
        this.weakKeys = weakKeys;
    }

    public ConcurrentMap<K, V> apply(F from) {
        if (weakKeys) {
            return new MapMaker().weakKeys().makeMap();
        } else {
            return new MapMaker().makeMap();
        }
    }

}

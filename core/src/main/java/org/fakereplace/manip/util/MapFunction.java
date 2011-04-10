package org.fakereplace.manip.util;

import org.fakereplace.com.google.common.base.Function;
import org.fakereplace.com.google.common.collect.MapMaker;

import java.util.Map;

public class MapFunction<F, K, V> implements Function<F, Map<K, V>> {
    final boolean weakKeys;

    public MapFunction(boolean weakKeys) {
        this.weakKeys = weakKeys;
    }

    public Map<K, V> apply(F from) {
        if (weakKeys) {
            return new MapMaker().weakKeys().makeMap();
        } else {
            return new MapMaker().makeMap();
        }
    }

}

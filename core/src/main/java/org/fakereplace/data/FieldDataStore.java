package org.fakereplace.data;

import org.fakereplace.com.google.common.base.Function;
import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.runtime.NullSafeConcurrentHashMap;

import java.util.Map;

/**
 * This class holds field data for added fields. It maintains a weakly
 * referenced computing map of instance -> field value.
 *
 * @author Stuart Douglas
 */
public class FieldDataStore {
    private static final Map<Object, Map<Integer, Object>> fieldData = new MapMaker().weakKeys().makeComputingMap(new Function<Object, Map<Integer, Object>>() {
        public Map<Integer, Object> apply(Object from) {
            return new NullSafeConcurrentHashMap<Integer, Object>();
        }
    });

    public static Object getValue(Object instance, int field) {
        return fieldData.get(instance).get(field);
    }

    public static void setValue(Object instance, Object value, int field) {
        fieldData.get(instance).put(field, value);
    }
}

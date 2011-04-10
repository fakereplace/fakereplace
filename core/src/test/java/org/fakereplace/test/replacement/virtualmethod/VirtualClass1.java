package org.fakereplace.test.replacement.virtualmethod;

import org.fakereplace.util.NoInstrument;

import java.util.List;
import java.util.Map;
import java.util.Set;

@NoInstrument
public class VirtualClass1 {

    private Integer value = 0;

    public Integer getValue() {
        addValue(1);
        return value;
    }

    public void addValue(int value) throws ArithmeticException {
        this.value = this.value + value;
    }

    public List<String> getStuff(List<Integer> aList) {
        return null;
    }

    public void clear(Map<String, String> map, Set<String> set) {
        clearFunction(map, set, 0);
    }

    public void clearFunction(Map<String, String> map, Set<String> set, int i) {
        map.clear();
        set.clear();
    }

    private void privateFunction() {

    }

    @Override
    public String toString() {
        return "VirtualChild1";
    }
}

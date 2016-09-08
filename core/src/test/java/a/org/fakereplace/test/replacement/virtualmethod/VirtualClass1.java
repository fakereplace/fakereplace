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

package a.org.fakereplace.test.replacement.virtualmethod;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fakereplace.util.NoInstrument;

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

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

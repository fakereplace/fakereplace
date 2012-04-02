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

package a.org.fakereplace.test.replacement.constructor;

import java.util.List;
import java.util.Set;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class ConstructorClass1 {

    public ConstructorClass1(String a) {
        value = a;
    }

    private ConstructorClass1(List<String> a) {
        value = "h";
    }

    private ConstructorClass1(Set<String> a) {
        value = "e";
    }

    public ConstructorClass1(int i1, int i2, int i3, int i4, int i5, int i6) {
        value = "h";
    }

    String value = "a";

    public String getValue() {
        return value;
    }
}

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

package a.org.fakereplace.test.replacement.instancefield;

import org.fakereplace.util.NoInstrument;

@NoInstrument
public class InstanceFieldClass1 {

    int afield;
    int bfield;

    long otherField;

    private int yaf;
    Object fa2;

    public InstanceFieldClass1() {
        sv = "aa";
    }

    public String sv;

    int yy;

    private int value = 0;

    private long lv = 1;

    public int get() {
        return value;
    }

    public void inc() {
        value++;
    }

    public long getlong() {
        return lv;
    }

    public void inclong() {
        lv++;
    }

    public String getSv() {
        return sv;
    }

    public Object getFa2() {
        return fa2;
    }

    public void setFa2(Object fa2) {
        this.fa2 = fa2;
    }

}

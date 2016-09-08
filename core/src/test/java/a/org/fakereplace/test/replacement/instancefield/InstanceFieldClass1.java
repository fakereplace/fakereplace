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

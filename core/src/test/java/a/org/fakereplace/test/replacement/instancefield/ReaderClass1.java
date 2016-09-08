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

/**
 * @author Stuart Douglas
 */
public class ReaderClass1 {

    private FieldClass1 fieldClass = new FieldClass1();

    int readField() {
        return fieldClass.field;
    }

    void writeField(int value) {
        fieldClass.field = value;
    }

    int readStaticField() {
        return fieldClass.staticField;
    }

    void writeStaticField(int value) {
        fieldClass.staticField = value;
    }
}

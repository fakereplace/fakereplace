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

package a.org.fakereplace.test.replacement.addedclass;

import a.org.fakereplace.test.util.ClassReplacer;
import org.testng.annotations.Test;

public class AddedClassTest {

    @Test
    public void testAddedClass() {
        ClassReplacer r = new ClassReplacer();
        r.queueClassForReplacement(ReplacedClass.class, ReplacedClass1.class);
        r.addNewClass(AddedClass1.class, "a.org.fakereplace.test.replacement.addedclass.AddedClass");
        r.replaceQueuedClasses();

        ReplacedClass c = new ReplacedClass();
        assert c.getValue().equals("hello Bob");

    }
}

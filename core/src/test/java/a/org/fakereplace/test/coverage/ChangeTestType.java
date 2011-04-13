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

package a.org.fakereplace.test.coverage;

public enum ChangeTestType {
    GET_BY_NAME("Get by name"),
    GET_DECLARED_BY_NAME("Get declared by name"),
    GET_DECLARED_ALL("Get declared all"),
    GET_ALL("Get all"),
    INVOKE_BY_REFLECTION("Invoke by reflection", false),
    ACCESS_THROUGH_BYTECODE("Access through bytecode", false),
    GET_DECLARED_CLASS("getDeclaringClass works", false);

    private final String label;
    private final boolean applicableToRemoved;

    private ChangeTestType(String label, boolean applicableToRemoved) {
        this.label = label;
        this.applicableToRemoved = applicableToRemoved;
    }

    private ChangeTestType(String label) {
        this.label = label;
        this.applicableToRemoved = true;
    }

    public String getLabel() {
        return label;
    }

    public boolean isApplicableToRemoved() {
        return applicableToRemoved;
    }

}

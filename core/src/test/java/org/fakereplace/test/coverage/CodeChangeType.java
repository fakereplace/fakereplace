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

package org.fakereplace.test.coverage;

public enum CodeChangeType {
    ADD_STATIC_METHOD("Add static method"),
    REMOVE_STATIC_METHOD("Remove static method", true),
    EXISTING_STATIC_METHOD("Existing static method"),
    ADD_INSTANCE_METHOD("Add instance method"),
    REMOVE_INSTANCE_METHOD("Remove instance method", true),
    EXISTING_INSTANCE_METHOD("Existing instance method"),
    ADD_CONSTRUCTOR("Add Constructor"),
    REMOVE_CONSTRUCTOR("Remove Constructor", true),
    EXISTING_CONSTRUCTOR("Existing Constructor"),
    ADD_STATIC_FIELD("Add static field"),
    REMOVE_STATIC_FIELD("Remove static field", true),
    EXISTING_STATIC_FIELD("Existing static field"),
    ADD_INSTANCE_FIELD("Add instance field"),
    REMOVE_INSTANCE_FIELD("Remove instance field", true),
    EXISTING_INSTANCE_FIELD("Existing instance field"),
    STATIC_FIELD_TO_INSTANCE("Static field to instance field"),
    INSTANCE_FIELD_TO_STATIC_FIELD("Instance field to static field");

    private final String label;
    private final boolean removed;

    private CodeChangeType(String label, boolean removed) {
        this.label = label;
        this.removed = removed;
    }

    private CodeChangeType(String label) {
        this.label = label;
        this.removed = false;
    }

    public String getLabel() {
        return label;
    }

    public boolean isRemoved() {
        return removed;
    }

}

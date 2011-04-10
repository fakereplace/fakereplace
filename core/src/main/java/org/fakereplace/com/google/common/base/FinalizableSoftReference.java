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

package org.fakereplace.com.google.common.base;

import java.lang.ref.SoftReference;

/**
 * Soft reference with a {@code finalizeReferent()} method which a background
 * thread invokes after the garbage collector reclaims the referent. This is a
 * simpler alternative to using a {@link java.lang.ref.ReferenceQueue}.
 *
 * @author Bob Lee
 */
public abstract class FinalizableSoftReference<T> extends SoftReference<T>
        implements FinalizableReference {

    /**
     * Constructs a new finalizable soft reference.
     *
     * @param referent to softly reference
     * @param queue    that should finalize the referent
     */
    protected FinalizableSoftReference(T referent,
                                       FinalizableReferenceQueue queue) {
        super(referent, queue.queue);
        queue.cleanUp();
    }
}

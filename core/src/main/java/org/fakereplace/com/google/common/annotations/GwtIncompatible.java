/*
 *
 *  * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 *  * by the @authors tag.
 *  *
 *  * This is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU Lesser General Public License as
 *  * published by the Free Software Foundation; either version 2.1 of
 *  * the License, or (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this software; if not, write to the Free
 *  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */

package org.fakereplace.com.google.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The presence of this annotation on a method indicates that the method may
 * <em>not</em> be used with the
 * <a href="http://code.google.com/webtoolkit/">Google Web Toolkit</a> (GWT),
 * even though its type is annotated as {@link GwtCompatible} and accessible in
 * GWT.  They can cause GWT compilation errors or simply unexpected exceptions
 * when used in GWT.
 * <p/>
 * <p>Note that this annotation should only be applied to methods of types which
 * are annotated as {@link GwtCompatible}.
 *
 * @author Charles Fry
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
// @Documented - uncomment when GWT support is official
@GwtCompatible
public @interface GwtIncompatible {

    /**
     * Describes why the annotated element is incompatible with GWT. Since this is
     * generally due to a dependence on a type/method which GWT doesn't support,
     * it is sufficient to simply reference the unsupported type/method. E.g.
     * "Class.isInstance".
     */
    String value();

}

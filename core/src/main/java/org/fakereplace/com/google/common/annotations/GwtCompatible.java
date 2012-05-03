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
 * The presence of this annotation on a type indicates that the type may be
 * used with the
 * <a href="http://code.google.com/webtoolkit/">Google Web Toolkit</a> (GWT).
 * When applied to a method, the return type of the method is GWT compatible.
 * It's useful to indicate that an instance created by factory methods has a GWT
 * serializable type.  In the following example,
 * <p/>
 * <pre style="code">
 * {@literal @}GwtCompatible
 * class Lists {
 * ...
 * {@literal @}GwtCompatible(serializable = true)
 * static &lt;E> List&lt;E> newArrayList(E... elements) {
 * ...
 * }
 * }
 * </pre>
 * The return value of {@code Lists.newArrayList(E[])} has GWT
 * serializable type.  It is also useful in specifying contracts of interface
 * methods.  In the following example,
 * <p/>
 * <pre style="code">
 * {@literal @}GwtCompatible
 * interface ListFactory {
 * ...
 * {@literal @}GwtCompatible(serializable = true)
 * &lt;E> List&lt;E> newArrayList(E... elements);
 * }
 * </pre>
 * The {@code newArrayList(E[])} method of all implementations of {@code
 * ListFactory} is expected to return a value with a GWT serializable type.
 * <p/>
 * <p>Note that a {@code GwtCompatible} type may have some {@link
 * GwtIncompatible} methods.
 *
 * @author Charles Fry
 * @author Hayward Chan
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
// @Documented - uncomment when GWT support is official
@GwtCompatible
public @interface GwtCompatible {

    /**
     * When {@code true}, the annotated type or the type of the method return
     * value is GWT serializable.
     *
     * @see <a href="http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&t=DevGuideSerializableTypes">
     *      Documentation about GWT serialization</a>
     */
    boolean serializable() default false;

    /**
     * When {@code true}, the annotated type is emulated in GWT. The emulated
     * source (also known as super-source) is different from the implementation
     * used by the JVM.
     *
     * @see <a href="http://code.google.com/docreader/#p=google-web-toolkit-doc-1-5&t=DevGuideModuleXml">
     *      Documentation about GWT emulated source</a>
     */
    boolean emulated() default false;
}

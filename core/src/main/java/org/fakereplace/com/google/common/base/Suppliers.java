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

import org.fakereplace.com.google.common.annotations.VisibleForTesting;

import java.io.Serializable;


/**
 * Useful suppliers.
 * <p/>
 * <p>All methods return serializable suppliers as long as they're given
 * serializable parameters.
 *
 * @author Laurence Gonsalves
 * @author Harry Heymann
 */
public final class Suppliers {
    private Suppliers() {
    }

    /**
     * Returns a new supplier which is the composition of the provided function
     * and supplier. In other words, the new supplier's value will be computed by
     * retrieving the value from {@code first}, and then applying
     * {@code function} to that value. Note that the resulting supplier will not
     * call {@code first} or invoke {@code function} until it is called.
     */
    public static <F, T> Supplier<T> compose(
            Function<? super F, T> function, Supplier<F> first) {
        Preconditions.checkNotNull(function);
        Preconditions.checkNotNull(first);
        return new SupplierComposition<F, T>(function, first);
    }

    private static class SupplierComposition<F, T>
            implements Supplier<T>, Serializable {
        final Function<? super F, ? extends T> function;
        final Supplier<? extends F> first;

        SupplierComposition(Function<? super F, ? extends T> function,
                            Supplier<? extends F> first) {
            this.function = function;
            this.first = first;
        }

        public T get() {
            return function.apply(first.get());
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Returns a supplier which caches the instance retrieved during the first
     * call to {@code get()} and returns that value on subsequent calls to
     * {@code get()}. See:
     * <a href="http://en.wikipedia.org/wiki/Memoization">memoization</a>
     * <p/>
     * <p>The returned supplier is thread-safe. The supplier's serialized form
     * does not contain the cached value, which will be recalculated when {@code
     * get()} is called on the reserialized instance.
     */
    public static <T> Supplier<T> memoize(Supplier<T> delegate) {
        return new MemoizingSupplier<T>(Preconditions.checkNotNull(delegate));
    }

    @VisibleForTesting
    static class MemoizingSupplier<T>
            implements Supplier<T>, Serializable {
        final Supplier<T> delegate;
        transient boolean initialized;
        transient T value;

        MemoizingSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        public synchronized T get() {
            if (!initialized) {
                value = delegate.get();
                initialized = true;
            }
            return value;
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Returns a supplier that always supplies {@code instance}.
     */
    public static <T> Supplier<T> ofInstance(T instance) {
        return new SupplierOfInstance<T>(instance);
    }

    private static class SupplierOfInstance<T>
            implements Supplier<T>, Serializable {
        final T instance;

        SupplierOfInstance(T instance) {
            this.instance = instance;
        }

        public T get() {
            return instance;
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * Returns a supplier whose {@code get()} method synchronizes on
     * {@code delegate} before calling it, making it thread-safe.
     */
    public static <T> Supplier<T> synchronizedSupplier(Supplier<T> delegate) {
        return new ThreadSafeSupplier<T>(Preconditions.checkNotNull(delegate));
    }

    private static class ThreadSafeSupplier<T>
            implements Supplier<T>, Serializable {
        final Supplier<T> delegate;

        ThreadSafeSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        public T get() {
            synchronized (delegate) {
                return delegate.get();
            }
        }

        private static final long serialVersionUID = 0;
    }
}

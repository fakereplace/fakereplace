/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.integration.weld;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.integration.weld.javassist.WeldProxyClassLoadingDelegate;
import org.jboss.weld.bean.proxy.ProxyFactory;

public class WeldClassChangeAware implements ClassChangeAware {

    private static final Field beanField;

    /**
     * proxy factories, key by by a weak reference to their bean object to prevent a memory leak.
     */
    private static final Map<Object, ProxyFactory<?>> proxyFactories = new MapMaker().weakKeys().makeMap();

    static {
        try {
            beanField = ProxyFactory.class.getDeclaredField("bean");
            beanField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeChange(final Class<?>[] changed, final ClassIdentifier[] added) {

    }

    @Override
    public void notify(final Class<?>[] changed, final ClassIdentifier[] added) {
        ClassLoader oldCl = null;
        WeldProxyClassLoadingDelegate.beginProxyRegeneration();
        try {
            final Set<Class<?>> changedClasses = new HashSet<Class<?>>(Arrays.asList(changed));

            //Hack to re-generate the weld client proxies
            for (final ProxyFactory instance : proxyFactories.values()) {
                try {
                    final Bean<?> bean = (Bean<?>) beanField.get(instance);
                    for(final Class<?> clazz: changedClasses) {
                        if(bean.getTypes().contains(clazz)) {
                            Thread.currentThread().setContextClassLoader(bean.getBeanClass().getClassLoader());
                            instance.getProxyClass();
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            WeldProxyClassLoadingDelegate.endProxyRegeneration();
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public static void addProxyFactory(final ProxyFactory<?> factory, final Object bean) {
        proxyFactories.put(bean, factory);
    }
}

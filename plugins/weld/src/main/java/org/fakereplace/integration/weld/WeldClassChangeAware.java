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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.fakereplace.api.Attachments;
import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.integration.weld.javassist.WeldProxyClassLoadingDelegate;

public class WeldClassChangeAware implements ClassChangeAware {

    /**
     * proxy factories, key by by a weak reference to their bean object to prevent a memory leak.
     */
    private static final Map<Object, Object> proxyFactories = new MapMaker().weakKeys().makeMap();

    @Override
    public void beforeChange(final List<Class<?>> changed, final List<ClassIdentifier> added, final Attachments attachments) {

    }

    @Override
    public void afterChange(List<ChangedClass> changed, List<ClassIdentifier> added, final Attachments attachments) {
        ClassLoader oldCl = null;
        WeldProxyClassLoadingDelegate.beginProxyRegeneration();
        try {
            final Set<ChangedClass> changedClasses = new HashSet<ChangedClass>(changed);

            //Hack to re-generate the weld client proxies
            for (final Object instance : proxyFactories.values()) {
                try {
                    Class cp = instance.getClass();
                    while(!cp.getName().equals(WeldClassTransformer.ORG_JBOSS_WELD_BEAN_PROXY_PROXY_FACTORY)) {
                        cp = cp.getSuperclass();
                    }

                    Field beanField = cp.getDeclaredField("bean");
                    beanField.setAccessible(true);
                    Method getProxy = instance.getClass().getMethod("getProxyClass");
                    getProxy.setAccessible(true);
                    final Bean<?> bean = (Bean<?>) beanField.get(instance);
                    for(final ChangedClass clazz: changedClasses) {
                        if(bean.getTypes().contains(clazz.getChangedClass())) {
                            Thread.currentThread().setContextClassLoader(bean.getBeanClass().getClassLoader());
                            getProxy.invoke(instance);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            WeldProxyClassLoadingDelegate.endProxyRegeneration();
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public static void addProxyFactory(final Object factory, final Object bean) {
        proxyFactories.put(bean, factory);
    }
}

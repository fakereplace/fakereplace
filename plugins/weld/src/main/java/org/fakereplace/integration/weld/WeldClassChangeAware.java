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

package org.fakereplace.integration.weld;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.enterprise.inject.spi.Bean;

import org.fakereplace.api.ChangedClass;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.NewClassData;
import org.fakereplace.integration.weld.javassist.WeldProxyClassLoadingDelegate;

public class WeldClassChangeAware implements ClassChangeAware {

    /**
     * proxy factories, key by by a weak reference to their bean object to prevent a memory leak.
     */
    private static final Map<Object, Object> proxyFactories = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void afterChange(List<ChangedClass> changed, List<NewClassData> added) {
        ClassLoader oldCl = null;
        WeldProxyClassLoadingDelegate.beginProxyRegeneration();
        try {
            final Set<ChangedClass> changedClasses = new HashSet<>(changed);

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
        if(bean == null) {
            //apparently this can happen for createInjectionTarget
            return;
        }
        proxyFactories.put(bean, factory);
    }
}

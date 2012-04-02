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

package org.fakereplace.integration.weld;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.Bean;

import javassist.bytecode.ClassFile;
import org.fakereplace.Agent;
import org.fakereplace.api.ClassChangeAware;
import org.fakereplace.api.ClassChangeNotifier;
import org.fakereplace.classloading.ClassIdentifier;
import org.fakereplace.data.InstanceTracker;
import org.fakereplace.replacement.AddedClass;
import org.jboss.weld.bean.proxy.ClientProxyFactory;
import org.jboss.weld.bean.proxy.ClientProxyProvider;
import org.jboss.weld.bean.proxy.ProxyFactory;
import org.jboss.weld.util.Proxies;

public class ClassRedefinitionPlugin implements ClassChangeAware {

    private final Field proxyPoolField;
    private final Field addedClassFileField;
    private final Method createProxy;

    public ClassRedefinitionPlugin() {
        try {
            ClassChangeNotifier.add(this);
            proxyPoolField = ClientProxyProvider.class.getDeclaredField("pool");
            proxyPoolField.setAccessible(true);
            addedClassFileField = ProxyFactory.class.getDeclaredField(WeldClassTransformer.CLASS_FILE_FIELD);
            addedClassFileField.setAccessible(true);
            createProxy = ProxyFactory.class.getDeclaredMethod("createProxyClass", String.class);
            createProxy.setAccessible(true);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeChange(final Class<?>[] changed, final ClassIdentifier[] added) {

    }

    @Override
    public void notify(final Class<?>[] changed, final ClassIdentifier[] added) {
        ClassLoader oldCl = null;
        try {
            final Set<Class<?>> changedClasses = new HashSet<Class<?>>(Arrays.asList(changed));

            final Set<ClientProxyProvider> instances = (Set<ClientProxyProvider>) InstanceTracker.get("org.jboss.weld.bean.proxy.ClientProxyProvider");
            final Map<Bean<?>, Class<?>> beans = new HashMap<Bean<?>, Class<?>>();
            //Hack to re-generate the weld client proxies
            for (ClientProxyProvider instance : instances) {
                try {
                    final ConcurrentMap<Bean<Object>, Object> pool = (ConcurrentMap<Bean<Object>, Object>) proxyPoolField.get(instance);
                    final Iterator<Map.Entry<Bean<Object>, Object>> itr = pool.entrySet().iterator();
                    while (itr.hasNext()) {
                        final Map.Entry<Bean<Object>, Object> entry = itr.next();
                        if (changedClasses.contains(entry.getKey().getBeanClass())) {
                            beans.put(entry.getKey(), entry.getValue().getClass());
                            if (oldCl == null) {
                                oldCl = Thread.currentThread().getContextClassLoader();
                                Thread.currentThread().setContextClassLoader(entry.getValue().getClass().getClassLoader());
                            }
                        }
                    }

                    final Set<ClassDefinition> proxies = new HashSet<ClassDefinition>();

                    for (final Map.Entry<Bean<?>, Class<?>> entry : beans.entrySet()) {
                        final Bean<?> bean = entry.getKey();
                        final Proxies.TypeInfo typeInfo = Proxies.TypeInfo.of(bean.getTypes());
                        final ClientProxyFactory factory = new ClientProxyFactory(typeInfo.getSuperClass(), bean.getTypes(), bean);
                        createProxy.invoke(factory, entry.getValue().getName());
                        final ClassFile file = (ClassFile) addedClassFileField.get(factory);
                        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        file.write(new DataOutputStream(bytes));
                        proxies.add(new ClassDefinition(entry.getValue(), bytes.toByteArray()));
                    }
                    Agent.redefine(proxies.toArray(new ClassDefinition[proxies.size()]), new AddedClass[0]);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (UnmodifiableClassException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}

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

package org.fakereplace.integration.resteasy;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author Stuart Douglas
 */
public class ResteasyServletContex implements ServletContext {

    private final ServletContext delegate;
    private final Map<String, String> initParams = new HashMap<>();

    public ResteasyServletContex(ServletContext delegate) {
        this.delegate = delegate;
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }

    @Override
    public ServletContext getContext(String s) {
        return delegate.getContext(s);
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public String getMimeType(String s) {
        return delegate.getMimeType(s);
    }

    @Override
    public Set getResourcePaths(String s) {
        return delegate.getResourcePaths(s);
    }

    @Override
    public URL getResource(String s) throws MalformedURLException {
        return delegate.getResource(s);
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        return delegate.getResourceAsStream(s);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        return delegate.getRequestDispatcher(s);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String s) {
        return delegate.getNamedDispatcher(s);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Servlet getServlet(String s) throws ServletException {
        return delegate.getServlet(s);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Enumeration getServlets() {
        return delegate.getServlets();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Enumeration getServletNames() {
        return delegate.getServletNames();
    }

    @Override
    public void log(String s) {
        delegate.log(s);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void log(Exception e, String s) {
        delegate.log(e, s);
    }

    @Override
    public void log(String s, Throwable throwable) {
        delegate.log(s, throwable);
    }

    @Override
    public String getRealPath(String s) {
        return delegate.getRealPath(s);
    }

    @Override
    public String getServerInfo() {
        return delegate.getServerInfo();
    }

    @Override
    public String getInitParameter(String s) {
        if(initParams.containsKey(s)) {
            return initParams.get(s);
        }
        return delegate.getInitParameter(s);
    }

    @Override
    public Enumeration getInitParameterNames() {
        Enumeration initParameterNames = delegate.getInitParameterNames();
        Set<String> names = new HashSet<>();
        while (initParameterNames.hasMoreElements()) {
            names.add((String) initParameterNames.nextElement());
        }
        names.addAll(initParams.keySet());
        final Iterator<String> it = names.iterator();
        return new Enumeration() {
            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public Object nextElement() {
                return it.next();
            }
        };
    }

    @Override
    public Object getAttribute(String s) {
        return delegate.getAttribute(s);
    }

    @Override
    public Enumeration getAttributeNames() {
        return delegate.getAttributeNames();
    }

    @Override
    public void setAttribute(String s, Object o) {
        delegate.setAttribute(s, o);
    }

    @Override
    public void removeAttribute(String s) {
        delegate.removeAttribute(s);
    }

    @Override
    public String getServletContextName() {
        return delegate.getServletContextName();
    }
}

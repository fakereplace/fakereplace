package org.fakereplace.integration.resteasy;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Stuart Douglas
 */
public class ResteasyServletConfig implements ServletConfig {

    private final String servletName;
    private ServletContext servletContext;
    private final Map<String, String> initParams;
    private final ClassLoader classLoader;

    public ResteasyServletConfig(ServletConfig config) {
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.servletName = config.getServletName();
        this.servletContext = config.getServletContext();
        this.initParams = new HashMap<>();
        Enumeration e = config.getInitParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            initParams.put(name, config.getInitParameter(name));
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return initParams.get(s);
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Enumeration getInitParameterNames() {
        final Iterator<String> it = initParams.keySet().iterator();
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

    public Map<String, String> getInitParams() {
        return initParams;
    }
}

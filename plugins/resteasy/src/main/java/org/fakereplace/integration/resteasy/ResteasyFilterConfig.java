package org.fakereplace.integration.resteasy;

import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Stuart Douglas
 */
public class ResteasyFilterConfig implements FilterConfig {

    private final String filterName;
    private ServletContext servletContext;
    private final Map<String, String> initParams;
    private final ClassLoader classLoader;

    public ResteasyFilterConfig(FilterConfig config) {
        this.filterName = config.getFilterName();
        this.servletContext = config.getServletContext();
        this.initParams = new HashMap<>();
        Enumeration e = config.getInitParameterNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            initParams.put(name, config.getInitParameter(name));
        }
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public String getFilterName() {
        return filterName;
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

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
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

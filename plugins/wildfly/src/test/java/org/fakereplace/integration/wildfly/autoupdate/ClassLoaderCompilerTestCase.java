package org.fakereplace.integration.wildfly.autoupdate;

import org.junit.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * @author Stuart Douglas
 */
public class ClassLoaderCompilerTestCase {

    @Test
    public void testCompiler() throws Exception {
        try {
            URL baseUrl = getClass().getClassLoader().getResource(".");
            Path base = Paths.get(baseUrl.getFile(), "../../src/test/java");
            List<String> data = Collections.singletonList(getClass().getName());
            ClassLoaderCompiler compiler = new ClassLoaderCompiler(new ClassLoader(getClass().getClassLoader()) { }, base, data); //the CL will be closed if it is not wrapped
            compiler.compile();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}

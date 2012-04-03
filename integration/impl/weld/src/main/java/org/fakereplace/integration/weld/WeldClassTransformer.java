package org.fakereplace.integration.weld;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.MethodInfo;
import org.fakereplace.manip.MethodInvokationManipulator;
import org.fakereplace.transformation.FakereplaceTransformer;

/**
 * @author Stuart Douglas
 */
public class WeldClassTransformer implements FakereplaceTransformer {

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file) throws IllegalClassFormatException {

        /**
         * Hack up the proxy factory so it stores the proxy ClassFile. We need this to regenerate proxies.
         */
        if (file.getName().equals("org.jboss.weld.bean.proxy.ProxyFactory")) {
            for (final MethodInfo method : (List<MethodInfo>) file.getMethods()) {
                if (method.getName().equals("createProxyClass")) {
                    final MethodInvokationManipulator methodInvokationManipulator = new MethodInvokationManipulator();
                    methodInvokationManipulator.replaceVirtualMethodInvokationWithStatic(ClassLoader.class.getName(), WeldProxyClassLoadingDelegate.class.getName(), "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", "(Ljava/lang/ClassLoader;Ljava/lang/String;)Ljava/lang/Class;", loader);
                    methodInvokationManipulator.replaceVirtualMethodInvokationWithStatic("org.jboss.weld.util.bytecode.ClassFileUtils", WeldProxyClassLoadingDelegate.class.getName(), "toClass", "(Ljavassist/bytecode/ClassFile;Ljava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", "(Ljavassist/bytecode/ClassFile;Ljava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", loader);
                    methodInvokationManipulator.transformClass(file, loader, true);
                    return true;
                } else if (method.getName().equals("<init>")) {
                    //similar to other tracked instances
                    //but we need a strong ref
                    Bytecode code = new Bytecode(file.getConstPool());
                    code.addAload(0);
                    code.addInvokestatic(ClassRedefinitionPlugin.class.getName(), "addProxyFactory", "(Lorg/jboss/weld/bean/proxy/ProxyFactory;)V");
                    CodeIterator it = method.getCodeAttribute().iterator();
                    try {
                        it.skipConstructor();
                        it.insert(code.get());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return false;
    }
}

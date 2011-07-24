package org.fakereplace.integration.weld;

import javassist.bytecode.*;
import org.fakereplace.transformation.FakereplaceTransformer;
import org.jboss.weld.bean.proxy.ClientProxyFactory;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author Stuart Douglas
 */
public class WeldClassTransformer implements FakereplaceTransformer {

    public static final String CLASS_FILE_FIELD = "CLASS_FILE";

    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file) throws IllegalClassFormatException {

        /**
         * Hack up the proxy factory so it stores the proxy ClassFile. We need this to regenerate proxies.
         */
        if(file.getName().equals("org.jboss.weld.bean.proxy.ProxyFactory")) {
            final FieldInfo field = new FieldInfo(file.getConstPool(), CLASS_FILE_FIELD, "Ljavassist/bytecode/ClassFile;");
            try {
                file.addField(field);
            } catch (DuplicateMemberException e) {
                e.printStackTrace();
            }
            for(MethodInfo method : (List<MethodInfo>)file.getMethods()) {
                if(method.getName().equals("addConstructors")) {
                    final Bytecode b = new Bytecode(file.getConstPool());
                    b.add(Opcode.ALOAD_0);
                    b.add(Opcode.ALOAD_1);
                    b.addPutfield("org.jboss.weld.bean.proxy.ProxyFactory", CLASS_FILE_FIELD, "Ljavassist/bytecode/ClassFile;" );
                }
            }

            return true;
        }
        return false;
    }
}

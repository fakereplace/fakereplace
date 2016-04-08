package org.fakereplace.integration.wildfly;

import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import org.fakereplace.transformation.FakereplaceTransformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;

/**
 * @author Stuart Douglas
 */
public class WildflyClassTransformer implements FakereplaceTransformer {
    @Override
    public boolean transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, ClassFile file) throws IllegalClassFormatException, BadBytecode, DuplicateMemberException {
        if(!file.getName().equals("org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService")) {
            return false;
        }
        for(MethodInfo method : (List<MethodInfo>)file.getMethods()) {
            if(method.getName().equals("createServletConfig")) {
                CodeAttribute code = method.getCodeAttribute();
                code.setMaxStack(code.getMaxStack() + 1);
                CodeIterator it = code.iterator();
                while (it.hasNext()) {
                    int pos = it.next();
                    int inst = it.byteAt(pos);
                    if(inst == CodeAttribute.ARETURN) {
                        Bytecode b = new Bytecode(method.getConstPool());
                        b.addGetstatic("org.fakereplace.integration.wildfly.autoupdate.WebUpdateHandlerWrapper", "INSTANCE", "Lio/undertow/server/HandlerWrapper;");
                        b.addInvokevirtual("io.undertow.servlet.api.DeploymentInfo", "addInnerHandlerChainWrapper", "(Lio/undertow/server/HandlerWrapper;)Lio/undertow/servlet/api/DeploymentInfo;");
                        it.insert(pos, b.get());
                    }
                }
            }
        }

        return true;
    }
}

package org.fakereplace;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import org.fakereplace.api.ClassTransformer;
import org.fakereplace.api.IntegrationInfo;
import org.fakereplace.boot.Constants;
import org.fakereplace.boot.Enviroment;
import org.fakereplace.com.google.common.collect.MapMaker;
import org.fakereplace.data.BaseClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.InstanceTracker;
import org.fakereplace.detector.ClassChangeDetector;
import org.fakereplace.detector.ClassChangeDetectorRunner;
import org.fakereplace.manip.Manipulator;
import org.fakereplace.manip.util.ManipulationUtils;
import org.fakereplace.reflection.ReflectionInstrumentationSetup;
import org.fakereplace.util.NoInstrument;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This file is the transformer that instruments classes as they are added to
 * the system.
 * <p/>
 * It is doing to much at the moment, it needs to be split up a bit
 *
 * @author stuart
 */
public class Transformer implements ClassFileTransformer {

    private final Instrumentation instrumentation;

    private final ClassLoaderInstrumentation classLoaderInstrumenter;

    private final Enviroment enviroment;

    private static final Manipulator manipulator = new Manipulator();

    private static final Map<ClassLoader, Object> integrationClassloader = new MapMaker().weakKeys().makeMap();

    private static final Map<String, IntegrationInfo> integrationClassTriggers = new MapMaker().makeMap();

    private final Set<String> trackedInstances = new HashSet<String>();

    private final List<ClassTransformer> integrationTransformers = new CopyOnWriteArrayList<ClassTransformer>();

    private ClassChangeDetectorRunner detectorRunner = null;

    Transformer(Instrumentation inst, Set<IntegrationInfo> integrationInfo, Enviroment enviroment) {

        instrumentation = inst;
        classLoaderInstrumenter = new ClassLoaderInstrumentation(instrumentation);
        ReflectionInstrumentationSetup.setup(manipulator);
        this.enviroment = enviroment;
        for (IntegrationInfo i : integrationInfo) {
            trackedInstances.addAll(i.getTrackedInstanceClassNames());
            for (String j : i.getIntegrationTriggerClassNames()) {
                integrationClassTriggers.put(j, i);
            }
            ClassTransformer t = i.getTransformer();
            if (t != null) {
                integrationTransformers.add(t);
            }
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classBeingRedefined != null) {
            ClassDataStore.markClassReplaced(classBeingRedefined);
        }
        try {
            classLoaderInstrumenter.instrumentClassLoaderIfNessesary(loader, className);
            byte[] data = classfileBuffer;
            for (ClassTransformer i : integrationTransformers) {
                byte[] dt = i.transform(data, className);
                if (dt != null) {
                    data = dt;
                }
            }

            ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(data)));

            // we do not instrument any classes from fakereplace
            // if we did we get an endless loop
            // we also avoid instrumenting much of the java/lang and
            // java/io namespace except for java/lang/reflect/Proxy
            if (BuiltinClassData.skipInstrumentation(className)) {
                if (classBeingRedefined == null) {
                    BaseClassData baseData = new BaseClassData(file, loader, false);
                    ClassDataStore.saveClassData(loader, baseData.getInternalName(), baseData);
                } else {
                    manipulator.transformClass(file, loader, enviroment);
                }
                return null;
            }

            if (classBeingRedefined == null) {
                AnnotationsAttribute at = (AnnotationsAttribute) file.getAttribute(AnnotationsAttribute.invisibleTag);
                if (at != null) {
                    // NoInstrument is used for testing or by integration modules
                    Object an = at.getAnnotation(NoInstrument.class.getName());
                    if (an != null) {
                        return null;
                    }
                }
            }

            if (integrationClassTriggers.containsKey(file.getName())) {
                integrationClassloader.put(loader, new Object());
                // we need to load the class in another thread
                // otherwise it will not go through the javaagent
                ThreadLoader.loadAsync(integrationClassTriggers.get(file.getName()).getClassChangeAwareName(), loader, true);
            }

            if (trackedInstances.contains(file.getName())) {
                makeTrackedInstance(file);
            }

            manipulator.transformClass(file, loader, enviroment);

            boolean replaceable = enviroment.isClassReplacable(file.getName(), loader);
            if (replaceable && (AccessFlag.ENUM & file.getAccessFlags()) == 0 && (AccessFlag.ANNOTATION & file.getAccessFlags()) == 0) {
                // Initialise the detector
                // there is no point running it until replaceable classes have been
                // loaded
                if (detectorRunner == null) {
                    detectorRunner = new ClassChangeDetectorRunner();
                    Thread t = new Thread(detectorRunner);
                    t.start();
                }
                if (classBeingRedefined == null) {
                    ClassChangeDetector.addClassLoader(loader, file.getName());
                }
                if (file.isInterface()) {
                    addAbstractMethodForInstrumentation(file);
                } else {
                    addMethodForInstrumentation(file);
                    addConstructorForInstrumentation(file);
                    addStaticConstructorForInstrumentation(file);
                }
            }

            if (classBeingRedefined == null) {
                BaseClassData baseData = new BaseClassData(file, loader, replaceable);
                ClassDataStore.saveClassData(loader, baseData.getInternalName(), baseData);
            }

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            file.write(new DataOutputStream(bs));

            // dump the class for debugging purposes
            if (enviroment.getDumpDirectory() != null && classBeingRedefined != null) {
                FileOutputStream s = new FileOutputStream(enviroment.getDumpDirectory() + '/' + file.getName() + ".class");
                DataOutputStream dos = new DataOutputStream(s);
                file.write(dos);
                s.close();
            }
            // SerialVersionUIDChecker.testReflectionInfo(loader, file.getName(),
            // file.getSuperclass(), classfileBuffer);
            return bs.toByteArray();
        } catch (Throwable e) {
            e.printStackTrace();

            throw new IllegalClassFormatException();
        }
    }

    /**
     * Adds a method to a class that re can redefine when the class is reloaded
     *
     * @param file
     * @throws DuplicateMemberException
     */
    public void addMethodForInstrumentation(ClassFile file) {
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
            m.setAccessFlags(0 | AccessFlag.PUBLIC | AccessFlag.SYNTHETIC);

            Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
            if (BuiltinClassData.skipInstrumentation(file.getSuperclass())) {
                b.add(Bytecode.ACONST_NULL);
                b.add(Bytecode.ARETURN);
            } else {
                // delegate to the parent class
                b.add(Bytecode.ALOAD_0);
                b.add(Bytecode.ILOAD_1);
                b.add(Bytecode.ALOAD_2);
                b.addInvokespecial(file.getSuperclass(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
                b.add(Bytecode.ARETURN);

            }
            CodeAttribute ca = b.toCodeAttribute();
            m.setCodeAttribute(ca);
            file.addMethod(m);
        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_STATIC_METHOD_NAME, Constants.ADDED_STATIC_METHOD_DESCRIPTOR);
            m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC | AccessFlag.SYNTHETIC);
            Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
            b.add(Bytecode.ACONST_NULL);
            b.add(Bytecode.ARETURN);
            CodeAttribute ca = b.toCodeAttribute();
            m.setCodeAttribute(ca);
            file.addMethod(m);

        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
    }

    public static void addStaticConstructorForInstrumentation(ClassFile file) {
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), "<clinit>", "()V");
            m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
            Bytecode b = new Bytecode(file.getConstPool());
            b.add(Opcode.RETURN);
            m.setCodeAttribute(b.toCodeAttribute());
            file.addMethod(m);
        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Adds a method to a class that re can redefine when the class is reloaded
     *
     * @param file
     * @throws DuplicateMemberException
     */
    public void addAbstractMethodForInstrumentation(ClassFile file) {
        try {
            MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
            m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.ABSTRACT | AccessFlag.SYNTHETIC);
            file.addMethod(m);
        } catch (DuplicateMemberException e) {
            // e.printStackTrace();
        }
    }

    void addConstructorForInstrumentation(ClassFile file) {

        MethodInfo ret = new MethodInfo(file.getConstPool(), "<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
        Bytecode code = new Bytecode(file.getConstPool());
        // if the class does not have a constructor return
        if (!ManipulationUtils.addBogusConstructorCall(file, code)) {
            return;
        }
        CodeAttribute ca = code.toCodeAttribute();
        ca.setMaxLocals(4);
        ret.setCodeAttribute(ca);
        ret.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.SYNTHETIC);
        try {
            ca.computeMaxStack();
            file.addMethod(ret);
        } catch (DuplicateMemberException e) {

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Manipulator getManipulator() {
        return manipulator;
    }

    public static byte[] getIntegrationClass(ClassLoader c, String name) {
        if (!integrationClassloader.containsKey(c)) {
            return null;
        }
        URL resource = ClassLoader.getSystemClassLoader().getResource(name.replace('.', '/') + ".class");
        InputStream in = null;
        try {
            in = resource.openStream();
            return org.fakereplace.util.FileReader.readFileBytes(resource.openStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * modifies a class so that all created instances are registered with
     * InstanceTracker
     *
     * @param file
     * @throws BadBytecode
     */
    public void makeTrackedInstance(ClassFile file) throws BadBytecode {
        for (Object mo : file.getMethods()) {
            MethodInfo m = (MethodInfo) mo;
            if (m.getName().equals("<init>")) {
                Bytecode code = new Bytecode(file.getConstPool());
                code.addLdc(file.getName());
                code.addAload(0);
                code.addInvokestatic(InstanceTracker.class.getName(), "add", "(Ljava/lang/String;Ljava/lang/Object;)V");
                CodeIterator it = m.getCodeAttribute().iterator();
                it.skipConstructor();
                it.insert(code.get());
            }
        }
    }
}

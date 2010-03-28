package org.fakereplace.replacement;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import javassist.bytecode.AccessFlag;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.DuplicateMemberException;
import javassist.bytecode.ExceptionsAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.ParameterAnnotationsAttribute;
import javassist.bytecode.SignatureAttribute;

import org.fakereplace.Transformer;
import org.fakereplace.boot.Constants;
import org.fakereplace.boot.GlobalClassDefinitionData;
import org.fakereplace.boot.Logger;
import org.fakereplace.data.AnnotationDataStore;
import org.fakereplace.data.ClassData;
import org.fakereplace.data.ClassDataStore;
import org.fakereplace.data.MemberType;
import org.fakereplace.data.MethodData;
import org.fakereplace.data.MethodIdentifierStore;
import org.fakereplace.manip.Boxing;
import org.fakereplace.manip.MethodReturnRewriter;
import org.fakereplace.manip.ParameterRewriter;
import org.fakereplace.util.DescriptorUtils;

public class MethodReplacer
{
   public static void handleMethodReplacement(ClassFile file, ClassLoader loader, Class oldClass)
   {
      // state for added static methods

      CodeAttribute staticCodeAttribute = null, virtualCodeAttribute = null, constructorCodeAttribute = null;
      try
      {
         // stick our added methods into the class file
         // we can't finalise the code yet because we will probably need
         // the add stuff to them
         MethodInfo m = new MethodInfo(file.getConstPool(), Constants.ADDED_METHOD_NAME, Constants.ADDED_METHOD_DESCRIPTOR);
         m.setAccessFlags(0 | AccessFlag.PUBLIC);

         Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
         b.add(Bytecode.ACONST_NULL);
         b.add(Bytecode.ARETURN);
         virtualCodeAttribute = b.toCodeAttribute();
         m.setCodeAttribute(virtualCodeAttribute);
         file.addMethod(m);

         m = new MethodInfo(file.getConstPool(), Constants.ADDED_STATIC_METHOD_NAME, Constants.ADDED_STATIC_METHOD_DESCRIPTOR);
         m.setAccessFlags(AccessFlag.PUBLIC | AccessFlag.STATIC);
         b = new Bytecode(file.getConstPool(), 5, 3);
         b.add(Bytecode.ACONST_NULL);
         b.add(Bytecode.ARETURN);
         staticCodeAttribute = b.toCodeAttribute();
         m.setCodeAttribute(staticCodeAttribute);
         file.addMethod(m);

         m = new MethodInfo(file.getConstPool(), "<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
         m.setAccessFlags(AccessFlag.PUBLIC);
         b = new Bytecode(file.getConstPool(), 5, 5);
         if (FakeConstructorUtils.addBogusConstructorCall(file, b))
         {
            constructorCodeAttribute = b.toCodeAttribute();
            m.setCodeAttribute(constructorCodeAttribute);
            constructorCodeAttribute.setMaxLocals(6);
            file.addMethod(m);
         }

      }
      catch (DuplicateMemberException e)
      {
         e.printStackTrace();
      }
      ClassData data = ClassDataStore.getClassData(loader, Descriptor.toJvmName(file.getName()));

      Set<MethodData> methods = new HashSet<MethodData>();

      methods.addAll(data.getMethods());

      ListIterator<?> it = file.getMethods().listIterator();

      // now we iterator through all methods and constructors and compare new
      // and old. in the process we modify the new class so that is's signature
      // is exactly compatible with the old class, otherwise an
      // IncompatibleClassChange exception will be thrown
      while (it.hasNext())
      {
         MethodInfo m = (MethodInfo) it.next();
         MethodData md = null;

         for (MethodData i : methods)
         {
            if (i.getMethodName().equals(m.getName()) && i.getDescriptor().equals(m.getDescriptor()) && i.getAccessFlags() == m.getAccessFlags())
            {
               // if it is the constructor
               if (m.getName().equals("<init>"))
               {
                  try
                  {
                     Constructor<?> meth = i.getConstructor(oldClass);
                     AnnotationDataStore.recordConstructorAnnotations(meth, (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag));
                     // now revert the annotations:
                     m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), meth));
                     m.addAttribute(AnnotationReplacer.duplicateParameterAnnotationsAttribute(file.getConstPool(), meth));
                  }
                  catch (Exception e)
                  {
                     throw new RuntimeException(e);
                  }
               }
               else if (!m.getName().equals("<clinit>"))
               {
                  // other methods
                  // static constructors cannot have annotations so
                  // we do not have to worry about them
                  try
                  {
                     Method meth = i.getMethod(oldClass);
                     AnnotationDataStore.recordMethodAnnotations(meth, (AnnotationsAttribute) m.getAttribute(AnnotationsAttribute.visibleTag));
                     AnnotationDataStore.recordMethodParameterAnnotations(meth, (ParameterAnnotationsAttribute) m.getAttribute(ParameterAnnotationsAttribute.visibleTag));
                     // now revert the annotations:
                     m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), meth));
                     m.addAttribute(AnnotationReplacer.duplicateParameterAnnotationsAttribute(file.getConstPool(), meth));
                  }
                  catch (Exception e)
                  {
                     throw new RuntimeException(e);
                  }
               }
               md = i;
               break;
            }
         }
         // we do not need to deal with these
         if (m.getName().equals(Constants.ADDED_METHOD_NAME) || m.getName().equals(Constants.ADDED_STATIC_METHOD_NAME))
         {
            break;
         }
         // This is a newly added method.
         if (md == null)
         {
            if ((m.getAccessFlags() & AccessFlag.STATIC) != 0)
            {
               addMethod(file, loader, m, data, staticCodeAttribute, true, oldClass);
            }
            else if ((m.getName().equals("<init>")))
            {
               addConstructor(file, loader, m, data, constructorCodeAttribute, oldClass);
            }
            else if (m.getName().equals("<clinit>"))
            {
               // nop, we can't change this, just ignore it
            }
            else if ((m.getAccessFlags() & AccessFlag.INTERFACE) == 0)
            {
               addMethod(file, loader, m, data, virtualCodeAttribute, false, oldClass);
            }
            else
            {
               // interface method
            }

            // TODO deal with constructors and virtual methods
            // remove the actual definition
            it.remove();
         }
         else
         {
            // the removed method has been replaced so lets change it back
            if (md.getType() == MemberType.REMOVED_METHOD)
            {
               data.replaceMethod(new MethodData(md.getMethodName(), md.getDescriptor(), md.getClassName(), MemberType.NORMAL, md.getAccessFlags()));
            }
            methods.remove(md);
         }
      }
      // these methods have been removed, change them to throw a
      // MethodNotFoundError

      for (MethodData md : methods)
      {
         MethodData nmd = createRemovedMethod(file, md, oldClass);
         data.replaceMethod(nmd);
      }

      // if we did not return from a virtual method we need to call the parent
      // method directly so to this end we append some stuff to the bottom of
      // the method declaration to propagate the call to the parent

      try
      {
         Bytecode rcode = new Bytecode(staticCodeAttribute.getConstPool());
         rcode.add(Opcode.ACONST_NULL);
         rcode.add(Opcode.ARETURN);
         CodeIterator cit = staticCodeAttribute.iterator();
         cit.append(rcode.get());

         staticCodeAttribute.computeMaxStack();
         virtualCodeAttribute.computeMaxStack();
         if (constructorCodeAttribute != null)
         {
            constructorCodeAttribute.computeMaxStack();
         }
      }
      catch (BadBytecode e)
      {
         e.printStackTrace();
      }
   }

   private static String generateProxyInvocationBytecode(MethodInfo mInfo, ConstPool constPool, int methodNumber, String className, ClassLoader loader, boolean staticMethod) throws BadBytecode
   {
      String proxyName = GlobalClassDefinitionData.getProxyName();
      ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
      proxy.setVersionToJava5();
      proxy.setAccessFlags(AccessFlag.PUBLIC);

      // now generate our proxy that is used to actually call the method
      // we use a proxy because it makes the re-writing of loaded classes
      // much simpler

      MethodInfo nInfo;
      if (staticMethod)
      {
         nInfo = new MethodInfo(proxy.getConstPool(), mInfo.getName(), mInfo.getDescriptor());
      }
      else
      {
         // the descriptor is different as now there is an extra parameter for a
         // static call
         String nDesc = "(" + DescriptorUtils.extToInt(className) + mInfo.getDescriptor().substring(1);
         nInfo = new MethodInfo(proxy.getConstPool(), mInfo.getName(), nDesc);
      }
      copyMethodAttributes(mInfo, nInfo);

      // set the sync bit on the proxy if it was set on the method

      nInfo.setAccessFlags(0 | AccessFlag.PUBLIC | AccessFlag.STATIC);
      Bytecode proxyBytecode = new Bytecode(proxy.getConstPool());

      int paramOffset = 0;
      // if this is not a static method then we need to load the instance
      // onto the stack
      if (!staticMethod)
      {
         proxyBytecode.addAload(0);
         paramOffset = 1;
      }

      // stick the method number in the const pool then load it onto the
      // stack
      int scind = proxy.getConstPool().addIntegerInfo(methodNumber);
      proxyBytecode.addLdc(scind);

      String[] types = DescriptorUtils.descriptorStringToParameterArray(mInfo.getDescriptor());
      // create a new array the same size as the parameter array
      int index = proxyBytecode.getConstPool().addIntegerInfo(types.length);
      proxyBytecode.addLdc(index);
      // create new array to use to pass our parameters
      proxyBytecode.addAnewarray("java.lang.Object");
      int locals = types.length + paramOffset;
      for (int i = 0; i < types.length; ++i)
      {
         // duplicate the array reference on the stack
         proxyBytecode.add(Opcode.DUP);
         // load the array index into the stack
         index = proxyBytecode.getConstPool().addIntegerInfo(i);
         proxyBytecode.addLdc(index);

         char tp = types[i].charAt(0);
         if (tp != 'L' && tp != '[')
         {
            // we have a primitive type
            switch (tp)
            {
            case 'J':
               proxyBytecode.addLload(i + paramOffset);
               locals++;
               break;
            case 'D':
               proxyBytecode.addDload(i + paramOffset);
               locals++;
               break;
            case 'F':
               proxyBytecode.addFload(i + paramOffset);
               break;
            default:
               proxyBytecode.addIload(i + paramOffset);
            }
            // lets box it
            Boxing.box(proxyBytecode, tp);
         }
         else
         {
            proxyBytecode.addAload(i + paramOffset); // load parameter i onto
            // the stack
         }
         proxyBytecode.add(Opcode.AASTORE);// store the value in the array

      }

      // invoke the added static method
      if (staticMethod)
      {
         proxyBytecode.addInvokestatic(className, Constants.ADDED_STATIC_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
      }
      else
      {
         proxyBytecode.addInvokevirtual(className, Constants.ADDED_METHOD_NAME, "(I[Ljava/lang/Object;)Ljava/lang/Object;");
      }
      // cast it to the appropriate type and return it
      MethodReturnRewriter.addReturnProxyMethod(mInfo.getDescriptor(), proxyBytecode);
      CodeAttribute ca = proxyBytecode.toCodeAttribute();
      ca.setMaxLocals(locals);

      ca.computeMaxStack();
      nInfo.setCodeAttribute(ca);

      // now we have the static method that actually does the we-writes.
      // if this is a virtual method then we need to add another virtual method with the exact signature of the existing
      // method.
      // this is so that we do not need to instrument the reflection API to much
      if (!staticMethod)
      {
         // as this method is never called the bytecode just returns
         Bytecode b = new Bytecode(proxy.getConstPool());
         String ret = DescriptorUtils.getReturnType(mInfo.getDescriptor());
         if (ret.length() == 1)
         {
            if (ret.equals("V"))
            {
               b.add(Opcode.RETURN);
            }
            else if (ret.equals("D"))
            {
               b.add(Opcode.DLOAD_0);
               b.add(Opcode.DRETURN);
            }
            else if (ret.equals("F"))
            {
               b.add(Opcode.FLOAD_0);
               b.add(Opcode.FRETURN);
            }
            else if (ret.equals("J"))
            {
               b.add(Opcode.LLOAD_0);
               b.add(Opcode.LRETURN);
            }
            else
            {
               b.add(Opcode.ILOAD_0);
               b.add(Opcode.IRETURN);
            }
         }
         else
         {
            b.add(Opcode.ACONST_NULL);
            b.add(Opcode.ARETURN);
         }

         MethodInfo method = new MethodInfo(proxy.getConstPool(), mInfo.getName(), mInfo.getDescriptor());
         method.setAccessFlags(mInfo.getAccessFlags());
         method.setCodeAttribute(b.toCodeAttribute());
         method.getCodeAttribute().computeMaxStack();
         method.getCodeAttribute().setMaxLocals(types.length + 1);

         copyMethodAttributes(mInfo, method);
         try
         {
            proxy.addMethod(method);
         }
         catch (DuplicateMemberException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }

      try
      {
         proxy.addMethod(nInfo);
      }
      catch (DuplicateMemberException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      try
      {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bytes);
         proxy.write(dos);
         GlobalClassDefinitionData.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
         return proxyName;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }

   }

   /**
    * Adds a method to a class
    * 
    */
   private static void addMethod(ClassFile file, ClassLoader loader, MethodInfo mInfo, ClassData data, CodeAttribute bytecode, boolean staticMethod, Class oldClass)
   {

      int methodCount = MethodIdentifierStore.getMethodNumber(mInfo.getName(), mInfo.getDescriptor());

      try
      {
         generateBoxedConditionalCodeBlock(methodCount, mInfo, file.getConstPool(), bytecode, staticMethod, false);
         String proxyName = generateProxyInvocationBytecode(mInfo, file.getConstPool(), methodCount, file.getName(), loader, staticMethod);
         ClassDataStore.registerProxyName(oldClass, proxyName);
         String newMethodDesc = mInfo.getDescriptor();
         if (!staticMethod)
         {
            newMethodDesc = "(L" + Descriptor.toJvmName(file.getName()) + ";" + newMethodDesc.substring(1);
         }
         Transformer.getManipulator().replaceVirtualMethodInvokationWithStatic(file.getName(), proxyName, mInfo.getName(), mInfo.getDescriptor(), newMethodDesc);
         MethodData md = new MethodData(mInfo.getName(), mInfo.getDescriptor(), proxyName, MemberType.FAKE, mInfo.getAccessFlags());

         data.addMethod(md);
         ClassDataStore.registerReplacedMethod(proxyName, md);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * This method will take a method body and add it to an added method local
    * the bytecode is inserted inside a conditional that will only run the code
    * if the method number is correct variables are removed from the parameter
    * array and unboxed if nessesary the return value is boxed if nessesary
    * 
    * Much of this work is handled by helper classes
    * 
    * @param methodNumber
    * @param mInfo
    * @param methodConstPool
    * @param addedMethod
    * @throws BadBytecode
    */
   private static void generateBoxedConditionalCodeBlock(int methodNumber, MethodInfo mInfo, ConstPool methodConstPool, CodeAttribute addedMethod, boolean staticMethod, boolean constructor) throws BadBytecode
   {
      // we need to insert a conditional
      Bytecode bc = new Bytecode(mInfo.getConstPool());
      if (staticMethod)
      {
         bc.addOpcode(Opcode.ILOAD_0);
      }
      else
      {
         bc.addOpcode(Opcode.ILOAD_1);
      }
      int methodCountIndex = methodConstPool.addIntegerInfo(methodNumber);
      bc.addLdc(methodCountIndex);
      bc.addOpcode(Opcode.IF_ICMPNE);

      // now we need to fix local variables and unbox parameters etc
      ParameterRewriter.mangleParameters(staticMethod, mInfo.getCodeAttribute(), mInfo.getDescriptor(), mInfo.getCodeAttribute().getMaxLocals());
      int newMax = mInfo.getCodeAttribute().getMaxLocals() + 2;
      if (newMax > addedMethod.getMaxLocals())
      {
         addedMethod.setMaxLocals(newMax);
      }
      // later
      int offset = mInfo.getCodeAttribute().getCodeLength();
      // offset is +3, 2 for the branch offset after the IF_ICMPNE and 1 to
      // take it past the end of the code
      ByteUtils.add16bit(bc, offset + 3); // add the branch offset
      // now we need to insert our generated conditional at the start of the
      // new method
      CodeIterator newInfo = mInfo.getCodeAttribute().iterator();
      newInfo.insert(bc.get());
      // now insert the new method code at the begining of the static method
      // code attribute
      addedMethod.iterator().insert(mInfo.getCodeAttribute().getCode());
      // now we need to make sure the function is returning an object
      // rewriteFakeMethod makes sure that the return type is properly boxed
      if (!constructor)
      {
         MethodReturnRewriter.rewriteFakeMethod(addedMethod.iterator(), mInfo.getDescriptor());
      }
   }

   private static MethodData createRemovedMethod(ClassFile file, MethodData md, Class<?> oldClass)
   {
      // load up the existing method object

      MethodInfo m = new MethodInfo(file.getConstPool(), md.getMethodName(), md.getDescriptor());
      m.setAccessFlags(md.getAccessFlags());

      // put the old annotations on the class
      if (md.getMethodName().equals("<init>"))
      {
         Constructor<?> meth;
         try
         {
            meth = md.getConstructor(oldClass);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error accessing existing constructor via reflection in not found", e);
         }
         m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), meth));
      }
      else
      {
         Method meth;
         try
         {
            meth = md.getMethod(oldClass);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error accessing existing method via reflection in not found", e);
         }
         m.addAttribute(AnnotationReplacer.duplicateAnnotationsAttribute(file.getConstPool(), meth));
      }
      Bytecode b = new Bytecode(file.getConstPool(), 5, 3);
      b.addNew("java.lang.NoSuchMethodError");
      b.add(Opcode.DUP);
      b.addInvokespecial("java.lang.NoSuchMethodError", "<init>", "()V");
      b.add(Bytecode.ATHROW);
      CodeAttribute ca = b.toCodeAttribute();
      m.setCodeAttribute(ca);

      try
      {
         ca.computeMaxStack();
         file.addMethod(m);
      }
      catch (DuplicateMemberException e)
      {
         Logger.log(ClassRedefiner.class, "Duplicate error");
      }
      catch (BadBytecode e)
      {
         e.printStackTrace();
      }
      return new MethodData(md.getMethodName(), md.getDescriptor(), md.getClassName(), MemberType.REMOVED_METHOD, md.getAccessFlags());
   }

   private static void addConstructor(ClassFile file, ClassLoader loader, MethodInfo mInfo, ClassData data, CodeAttribute bytecode, Class oldClass)
   {
      int methodCount = MethodIdentifierStore.getMethodNumber(mInfo.getName(), mInfo.getDescriptor());

      try
      {
         generateBoxedConditionalCodeBlock(methodCount, mInfo, file.getConstPool(), bytecode, false, true);
         String proxyName = generateFakeConstructorBytecode(mInfo, file.getConstPool(), methodCount, file.getName(), loader);
         ClassDataStore.registerProxyName(oldClass, proxyName);
         MethodData md = new MethodData(mInfo.getName(), mInfo.getDescriptor(), proxyName, MemberType.FAKE_CONSTRUCTOR, mInfo.getAccessFlags());
         Transformer.getManipulator().rewriteConstructorAccess(file.getName(), mInfo.getDescriptor(), methodCount);
         data.addMethod(md);
         ClassDataStore.registerReplacedMethod(proxyName, md);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   /**
    * creates a class with a fake constructor that can be used by the reflection api
    * 
    * Constructors are not invoked through the proxy class, instead we have to do a lot more 
    * bytecode re-writing at the actual invocation sites
    * @param mInfo
    * @param constPool
    * @param methodNumber
    * @param className
    * @param loader
    * @param staticMethod
    * @return
    * @throws BadBytecode
    */
   private static String generateFakeConstructorBytecode(MethodInfo mInfo, ConstPool constPool, int methodNumber, String className, ClassLoader loader) throws BadBytecode
   {
      String proxyName = GlobalClassDefinitionData.getProxyName();
      ClassFile proxy = new ClassFile(false, proxyName, "java.lang.Object");
      proxy.setVersionToJava5();
      proxy.setAccessFlags(AccessFlag.PUBLIC);

      // add our new annotations directly onto the new proxy method. This way
      // they will just work without registering them with the
      // AnnotationDataStore

      String[] types = DescriptorUtils.descriptorStringToParameterArray(mInfo.getDescriptor());
      // as this method is never called the bytecode just returns
      Bytecode b = new Bytecode(proxy.getConstPool());
      b.add(Opcode.ALOAD_0);
      b.addInvokespecial("java.lang.Object", "<init>", "()V");
      b.add(Opcode.RETURN);
      MethodInfo method = new MethodInfo(proxy.getConstPool(), mInfo.getName(), mInfo.getDescriptor());
      method.setAccessFlags(mInfo.getAccessFlags());
      method.setCodeAttribute(b.toCodeAttribute());
      method.getCodeAttribute().computeMaxStack();
      method.getCodeAttribute().setMaxLocals(types.length + 1);

      copyMethodAttributes(mInfo, method);

      try
      {
         proxy.addMethod(method);
      }
      catch (DuplicateMemberException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      try
      {
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream dos = new DataOutputStream(bytes);
         proxy.write(dos);
         GlobalClassDefinitionData.saveProxyDefinition(loader, proxyName, bytes.toByteArray());
         return proxyName;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }

   }

   public static void copyMethodAttributes(MethodInfo oldMethod, MethodInfo newMethod)
   {
      AnnotationsAttribute annotations = (AnnotationsAttribute) oldMethod.getAttribute(AnnotationsAttribute.visibleTag);
      ParameterAnnotationsAttribute pannotations = (ParameterAnnotationsAttribute) oldMethod.getAttribute(ParameterAnnotationsAttribute.visibleTag);
      ExceptionsAttribute exAt = (ExceptionsAttribute) oldMethod.getAttribute(ExceptionsAttribute.tag);
      SignatureAttribute sigAt = (SignatureAttribute) oldMethod.getAttribute(SignatureAttribute.tag);
      if (annotations != null)
      {
         AttributeInfo newAnnotations = annotations.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
         newMethod.addAttribute(newAnnotations);
      }
      if (pannotations != null)
      {
         AttributeInfo newAnnotations = pannotations.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
         newMethod.addAttribute(newAnnotations);
      }
      if (sigAt != null)
      {
         AttributeInfo newAnnotations = sigAt.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
         newMethod.addAttribute(newAnnotations);
      }
      if (exAt != null)
      {
         AttributeInfo newAnnotations = exAt.copy(newMethod.getConstPool(), Collections.EMPTY_MAP);
         newMethod.addAttribute(newAnnotations);
      }
   }

}

package org.fakereplace.manip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

import org.fakereplace.boot.Constants;
import org.fakereplace.boot.Enviroment;
import org.fakereplace.boot.Logger;
import org.fakereplace.util.JumpMarker;
import org.fakereplace.util.JumpUtils;

/**
 * manipulator that replaces Field.set* / Field.get* with the following:
 * <p>
 * <code>
 *  if(FieldAcess.set*)
 *     FieldAccess.set*
 *  else
 *     field.set
 * </code>
 * 
 * @author stuart
 * 
 */
public class FieldAccessManipulator implements ClassManipulator
{
   private final Map<String, RewriteData> manipulationData = new ConcurrentHashMap<String, RewriteData>();

   public void clearRewrites(String className, ClassLoader loader)
   {

   }

   public FieldAccessManipulator()
   {
      // field access setters
      setupData("set", "(Ljava/lang/Object;Ljava/lang/Object;)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Ljava/lang/Object;)V", true, false);
      setupData("setBoolean", "(Ljava/lang/Object;Z)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;Z)V", true, false);
      setupData("setChar", "(Ljava/lang/Object;C)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;C)V", true, false);
      setupData("setDouble", "(Ljava/lang/Object;D)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;D)V", true, true);
      setupData("setFloat", "(Ljava/lang/Object;F)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;F)V", true, false);
      setupData("setInt", "(Ljava/lang/Object;I)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;I)V", true, false);
      setupData("setLong", "(Ljava/lang/Object;J)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;J)V", true, true);
      setupData("setShort", "(Ljava/lang/Object;S)V", "(Ljava/lang/reflect/Field;Ljava/lang/Object;S)V", true, false);

      // field access getters
      setupData("get", "(Ljava/lang/Object;)Ljava/lang/Object;", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Ljava/lang/Object;", false, false);
      setupData("getBoolean", "(Ljava/lang/Object;)Z", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)Z", false, false);
      setupData("getByte", "(Ljava/lang/Object;)B", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)B", false, false);
      setupData("getChar", "(Ljava/lang/Object;)C", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)C", false, false);
      setupData("getDouble", "(Ljava/lang/Object;)D", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)D", false, true);
      setupData("getFloat", "(Ljava/lang/Object;)F", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)F", false, false);
      setupData("getInt", "(Ljava/lang/Object;)I", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)I", false, false);
      setupData("getLong", "(Ljava/lang/Object;)J", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)J", false, true);
      setupData("getShort", "(Ljava/lang/Object;)S", "(Ljava/lang/reflect/Field;Ljava/lang/Object;)S", false, false);

   }

   private void setupData(String methodName, String descriptor, String newDescriptor, boolean set, boolean wide)
   {
      RewriteData data = new RewriteData(set, wide, methodName, descriptor, newDescriptor);
      manipulationData.put(methodName, data);
   }

   public void transformClass(ClassFile file, ClassLoader loader, Enviroment environment)
   {
      Map<Integer, RewriteData> methodCallLocations = new HashMap<Integer, RewriteData>();
      Map<RewriteData, Integer> newClassPoolLocations = new HashMap<RewriteData, Integer>();
      Integer fieldAccessLocation = null;
      Integer isFakeFieldLocation = null;
      // first we need to scan the constant pool looking for
      // CONSTANT_method_info_ref structures
      ConstPool pool = file.getConstPool();
      for (int i = 1; i < pool.getSize(); ++i)
      {
         // we have a method call
         if (pool.getTag(i) == ConstPool.CONST_Methodref || pool.getTag(i) == ConstPool.CONST_InterfaceMethodref)
         {
            String className, methodDesc, methodName;
            if (pool.getTag(i) == ConstPool.CONST_Methodref)
            {
               className = pool.getMethodrefClassName(i);
               methodDesc = pool.getMethodrefType(i);
               methodName = pool.getMethodrefName(i);
            }
            else
            {
               className = pool.getInterfaceMethodrefClassName(i);
               methodDesc = pool.getInterfaceMethodrefType(i);
               methodName = pool.getInterfaceMethodrefName(i);
            }

            if (className.equals("java.lang.reflect.Field"))
            {
               RewriteData data = manipulationData.get(methodName);
               if (data != null)
               {
                  // store the location in the const pool of the method ref
                  methodCallLocations.put(i, data);
                  // we have found a method call

                  // if we have not already stored a reference to our new
                  // method in the const pool
                  if (!newClassPoolLocations.containsKey(data))
                  {
                     if (fieldAccessLocation == null)
                     {
                        fieldAccessLocation = pool.addClassInfo("org.fakereplace.reflection.FieldAccess");
                        int nt = pool.addNameAndTypeInfo("isFakeField", "(Ljava/lang/reflect/Field;)Z");
                        isFakeFieldLocation = pool.addMethodrefInfo(fieldAccessLocation, nt);
                     }
                     int newNameAndType = pool.addNameAndTypeInfo(data.getMethodName(), data.getNewMethodDescriptor());
                     newClassPoolLocations.put(data, newNameAndType);
                  }
               }
            }
         }
      }

      // this means we found an instance of the call, now we have to iterate
      // through the methods and replace instances of the call
      if (fieldAccessLocation != null)
      {
         List<MethodInfo> methods = file.getMethods();
         for (MethodInfo m : methods)
         {
            try
            {
               // ignore abstract methods
               if (m.getCodeAttribute() == null)
               {
                  continue;
               }
               CodeIterator it = m.getCodeAttribute().iterator();
               while (it.hasNext())
               {
                  // loop through the bytecode
                  int index = it.next();
                  int op = it.byteAt(index);
                  // if the bytecode is a method invocation
                  if (op == CodeIterator.INVOKEVIRTUAL || op == CodeIterator.INVOKESTATIC || op == CodeIterator.INVOKEINTERFACE)
                  {
                     int val = it.s16bitAt(index + 1);
                     // if the method call is one of the methods we are
                     // replacing
                     if (methodCallLocations.containsKey(val))
                     {
                        RewriteData data = methodCallLocations.get(val);
                        // now we need to do some very hard bytecode
                        // manipulation.
                        Bytecode b = new Bytecode(file.getConstPool());
                        prepareForIsFakeFieldCall(b, data);
                        b.addInvokestatic(fieldAccessLocation, "isFakeField", "(Ljava/lang/reflect/Field;)Z");
                        b.add(Opcode.IFEQ);
                        JumpMarker performRealCall = JumpUtils.addJumpInstruction(b);
                        // now perform the fake call
                        b.addInvokestatic(fieldAccessLocation, data.getMethodName(), data.getNewMethodDescriptor());
                        b.add(Opcode.GOTO);
                        JumpMarker finish = JumpUtils.addJumpInstruction(b);
                        performRealCall.mark();
                        b.addInvokevirtual(Constants.FIELD_NAME, data.getMethodName(), data.getMethodDescriptor());
                        finish.mark();
                        it.writeByte(CodeIterator.NOP, index);
                        it.writeByte(CodeIterator.NOP, index + 1);
                        it.writeByte(CodeIterator.NOP, index + 2);
                        if (op == CodeIterator.INVOKEINTERFACE)
                        {
                           // INVOKEINTERFACE has some extra parameters
                           it.writeByte(CodeIterator.NOP, index + 3);
                           it.writeByte(CodeIterator.NOP, index + 4);
                        }
                        it.insert(b.get());
                     }
                  }

               }
               m.getCodeAttribute().computeMaxStack();
            }
            catch (Exception e)
            {
               Logger.log(this, "Bad byte code transforming " + file.getName());
               e.printStackTrace();
            }
         }
      }
   }

   private void prepareForIsFakeFieldCall(Bytecode b, RewriteData data)
   {
      if (data.isSet())
      {
         if (data.isWideValue())
         {
            // so current our stack looks like Field, instance, widevalue
            // we need Field, instance, widevalue , Field
            b.add(Opcode.DUP2_X2);
            b.add(Opcode.POP2);
         }
         else
         {
            // so current our stack looks like Field, instance, value
            // we need Field, instance, widevalue , Field
            b.add(Opcode.DUP_X2);
            b.add(Opcode.POP);
         }
         b.add(Opcode.DUP_X2);
         b.add(Opcode.POP);
         b.add(Opcode.DUP_X2);
      }
      else
      {
         // so current our stack looks like Field, instance
         // we need Field, instance, Field
         b.add(Opcode.DUP_X1);
         b.add(Opcode.POP);
         b.add(Opcode.DUP_X1);
      }
   }

   private static class RewriteData
   {
      private final boolean set;
      private final boolean wideValue;
      private final String methodName;
      private final String methodDescriptor;
      private final String newMethodDescriptor;

      public RewriteData(boolean set, boolean wideValue, String methodName, String methodDescriptor, String newMethodDescriptor)
      {
         this.set = set;
         this.wideValue = wideValue;
         this.methodName = methodName;
         this.methodDescriptor = methodDescriptor;
         this.newMethodDescriptor = newMethodDescriptor;
      }

      public boolean isSet()
      {
         return set;
      }

      public boolean isWideValue()
      {
         return wideValue;
      }

      public String getMethodName()
      {
         return methodName;
      }

      public String getMethodDescriptor()
      {
         return methodDescriptor;
      }

      public String getNewMethodDescriptor()
      {
         return newMethodDescriptor;
      }
   }

}

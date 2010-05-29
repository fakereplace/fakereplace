package org.fakereplace.manip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

import org.fakereplace.boot.Constants;
import org.fakereplace.boot.Logger;
import org.fakereplace.manip.data.ConstructorRewriteData;
import org.fakereplace.util.DescriptorUtils;

public class ConstructorInvocationManipulator
{

   Map<String, Set<ConstructorRewriteData>> constructorRewrites = new ConcurrentHashMap<String, Set<ConstructorRewriteData>>();

   public synchronized void clearRewrites(String className)
   {
      constructorRewrites.remove(className);
   }

   /**
    * This class re-writes constructor access. It is more complex than other manipulators as the work can't be hidden away in a temporary class 
    * 
    * @param oldClass
    * @param newClass
    * @param methodName
    * @param methodDesc
    * @param newStaticMethodDesc
    */
   public void rewriteConstructorCalls(String clazz, String descriptor, int methodNo, ClassLoader classLoader)
   {
      ConstructorRewriteData data = new ConstructorRewriteData(clazz, descriptor, methodNo, classLoader);
      if (!constructorRewrites.containsKey(clazz))
      {
         constructorRewrites.put(clazz, new HashSet<ConstructorRewriteData>());
      }
      constructorRewrites.get(clazz).add(data);
   }

   public void transformClass(ClassFile file)
   {
      if (constructorRewrites.isEmpty())
      {
         return;
      }
      Map<Integer, ConstructorRewriteData> methodCallLocations = new HashMap<Integer, ConstructorRewriteData>();
      Integer newCallLocation = null;
      // first we need to scan the constant pool looking for
      // CONSTANT_method_info_ref structures
      ConstPool pool = file.getConstPool();
      for (int i = 1; i < pool.getSize(); ++i)
      {
         // we have a method call
         if (pool.getTag(i) == ConstPool.CONST_Methodref)
         {
            if (constructorRewrites.containsKey(pool.getMethodrefClassName(i)))
            {
               for (ConstructorRewriteData data : constructorRewrites.get(pool.getMethodrefClassName(i)))
               {
                  if (pool.getMethodrefName(i).equals("<init>") && pool.getMethodrefType(i).equals(data.getMethodDesc()))
                  {
                     // store the location in the const pool of the method ref
                     methodCallLocations.put(i, data);
                     // we have found a method call
                     // now lets replace it

                     // if we have not already stored a reference to the refinied constructor
                     // in the const pool
                     if (newCallLocation == null)
                     {
                        // we have not added the new class reference or
                        // the new call location to the class pool yet
                        int classIndex = pool.getMethodrefClass(i);

                        int newNameAndType = pool.addNameAndTypeInfo("<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
                        newCallLocation = pool.addMethodrefInfo(classIndex, newNameAndType);
                     }
                     break;
                  }

               }
            }
         }
      }

      // this means we found an instance of the call, now we have to iterate
      // through the methods and replace instances of the call
      if (newCallLocation != null)
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
                  if (op == CodeIterator.INVOKESPECIAL)
                  {
                     int val = it.s16bitAt(index + 1);
                     // if the method call is one of the methods we are
                     // replacing
                     if (methodCallLocations.containsKey(val))
                     {
                        ConstructorRewriteData data = methodCallLocations.get(val);

                        // so we currently have all the arguments sitting on the stack, and we need to jigger them into
                        // an array and then call our method. First thing to do is scribble over the existing
                        // instructions:
                        it.writeByte(CodeIterator.NOP, index);
                        it.writeByte(CodeIterator.NOP, index + 1);
                        it.writeByte(CodeIterator.NOP, index + 2);

                        Bytecode bc = new Bytecode(file.getConstPool());
                        // now we need an array:
                        bc.addIconst(data.getParameters().length);
                        bc.addAnewarray("java.lang.Object");
                        // now we have our array sitting on top of the stack
                        // we need to stick our parameters into it. We do this is reverse
                        // as we can't pull them from the bottom of the stack
                        for (int i = data.getParameters().length - 1; i >= 0; --i)
                        {

                           if (DescriptorUtils.isWide(data.getParameters()[i]))
                           {
                              // dup the array below the wide
                              bc.add(Opcode.DUP_X2);
                              // now do it again so we have two copies
                              bc.add(Opcode.DUP_X2);
                              // now pop it, the is the equivilent of a wide swap
                              bc.add(Opcode.POP);
                           }
                           else
                           {
                              // duplicate the array to place 3
                              bc.add(Opcode.DUP_X1);
                              // now swap
                              bc.add(Opcode.SWAP);
                           }
                           // now the parameter is above the array
                           // box it if nessesary
                           if (DescriptorUtils.isPrimitive(data.getParameters()[i]))
                           {
                              Boxing.box(bc, data.getParameters()[i].charAt(0));
                           }
                           // add the array index
                           bc.addIconst(i);
                           bc.add(Opcode.SWAP);
                           bc.add(Opcode.AASTORE);
                           // we still have the array on the top of the stack becuase we duplicated it earlier
                        }
                        // so now our stack looks like unconstructed instance : array
                        // we need unconstructed instance : int : array : null
                        bc.addIconst(data.getMethodNo());
                        bc.add(Opcode.SWAP);
                        bc.add(Opcode.ACONST_NULL);
                        bc.addInvokespecial(data.getClazz(), "<init>", Constants.ADDED_CONSTRUCTOR_DESCRIPTOR);
                        // and we have our bytecode
                        it.insert(bc.get());

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

}

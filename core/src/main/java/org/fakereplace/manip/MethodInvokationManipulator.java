package org.fakereplace.manip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;

import org.fakereplace.boot.Logger;
import org.fakereplace.manip.data.VirtualToStaticData;
import org.fakereplace.manip.util.ManipulationDataStore;

public class MethodInvokationManipulator implements ClassManipulator
{
   ManipulationDataStore<VirtualToStaticData> data = new ManipulationDataStore<VirtualToStaticData>();

   public void clearRewrites(String className, ClassLoader loader)
   {
      data.remove(className, loader);
   }

   /**
    * This can also be used to replace a static invokation with another static
    * invokation.
    * 
    * if newClass is null then the invokation is changed to point to a method on the current class
    * 
    * @param oldClass
    * @param newClass
    * @param methodName
    * @param methodDesc
    * @param newStaticMethodDesc
    */
   public void replaceVirtualMethodInvokationWithStatic(String oldClass, String newClass, String methodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader)
   {
      VirtualToStaticData d = new VirtualToStaticData(oldClass, newClass, methodName, methodDesc, newStaticMethodDesc, null, classLoader);
      data.add(oldClass, d);
   }

   public void replaceVirtualMethodInvokationWithLocal(String oldClass, String methodName, String newMethodName, String methodDesc, String newStaticMethodDesc, ClassLoader classLoader)
   {
      VirtualToStaticData d = new VirtualToStaticData(oldClass, null, methodName, methodDesc, newStaticMethodDesc, newMethodName, classLoader);
      data.add(oldClass, d);
   }

   public void transformClass(ClassFile file, ClassLoader loader)
   {
      Map<String, Set<VirtualToStaticData>> virtualToStaticMethod = data.getManipulationData(loader);
      Map<Integer, VirtualToStaticData> methodCallLocations = new HashMap<Integer, VirtualToStaticData>();
      Map<VirtualToStaticData, Integer> newClassPoolLocations = new HashMap<VirtualToStaticData, Integer>();
      Map<VirtualToStaticData, Integer> newCallLocations = new HashMap<VirtualToStaticData, Integer>();
      // first we need to scan the constant pool looking for
      // CONSTANT_method_info_ref structures
      ConstPool pool = file.getConstPool();
      for (int i = 1; i < pool.getSize(); ++i)
      {
         // we have a method call
         if (pool.getTag(i) == ConstPool.CONST_Methodref)
         {
            if (virtualToStaticMethod.containsKey(pool.getMethodrefClassName(i)))
            {
               for (VirtualToStaticData data : virtualToStaticMethod.get(pool.getMethodrefClassName(i)))
               {
                  if (pool.getMethodrefName(i).equals(data.getMethodName()) && pool.getMethodrefType(i).equals(data.getMethodDesc()))
                  {
                     // store the location in the const pool of the method ref
                     methodCallLocations.put(i, data);
                     // we have found a method call
                     // now lets replace it

                     // if we have not already stored a reference to our new
                     // method in the const pool
                     if (!newClassPoolLocations.containsKey(data))
                     {
                        // we have not added the new class reference or
                        // the new call location to the class pool yet
                        int newCpLoc;
                        if (data.getNewClass() != null)
                        {
                           newCpLoc = pool.addClassInfo(data.getNewClass());
                        }
                        else
                        {

                           newCpLoc = pool.addClassInfo(file.getName());
                        }
                        newClassPoolLocations.put(data, newCpLoc);
                        int newNameAndType = pool.addNameAndTypeInfo(data.getNewMethodName(), data.getNewStaticMethodDesc());
                        newCallLocations.put(data, pool.addMethodrefInfo(newCpLoc, newNameAndType));
                     }
                     break;
                  }

               }
            }
         }
      }

      // this means we found an instance of the call, now we have to iterate
      // through the methods and replace instances of the call
      if (!newClassPoolLocations.isEmpty())
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
                  if (op == CodeIterator.INVOKEVIRTUAL || op == CodeIterator.INVOKESTATIC)
                  {
                     int val = it.s16bitAt(index + 1);
                     // if the method call is one of the methods we are
                     // replacing
                     if (methodCallLocations.containsKey(val))
                     {
                        VirtualToStaticData data = methodCallLocations.get(val);
                        // change the call to an invokestatic
                        it.writeByte(CodeIterator.INVOKESTATIC, index);
                        // change the method that is being called
                        it.write16bit(newCallLocations.get(data), index + 1);
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

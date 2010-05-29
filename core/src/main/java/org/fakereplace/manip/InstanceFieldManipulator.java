package org.fakereplace.manip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;

import org.fakereplace.boot.Constants;
import org.fakereplace.boot.Logger;
import org.fakereplace.manip.data.AddedFieldData;
import org.fakereplace.manip.util.Boxing;
import org.fakereplace.manip.util.ManipulationDataStore;
import org.fakereplace.util.DescriptorUtils;

public class InstanceFieldManipulator implements ClassManipulator
{
   /**
    * added field information by class
    */
   ManipulationDataStore<AddedFieldData> data = new ManipulationDataStore<AddedFieldData>();

   public void addField(AddedFieldData dt)
   {
      data.add(dt.getClassName(), dt);
   }

   public void transformClass(ClassFile file, ClassLoader loader)
   {

      Map<String, Set<AddedFieldData>> addedFieldData = data.getManipulationDate(loader);
      if (addedFieldData.isEmpty())
      {
         return;
      }
      Map<Integer, AddedFieldData> fieldAccessLocations = new HashMap<Integer, AddedFieldData>();
      // first we need to scan the constant pool looking for
      // CONSTANT_method_info_ref structures
      ConstPool pool = file.getConstPool();
      for (int i = 1; i < pool.getSize(); ++i)
      {
         // we have a field reference
         if (pool.getTag(i) == ConstPool.CONST_Fieldref)
         {
            if (addedFieldData.containsKey(pool.getFieldrefClassName(i)))
            {
               for (AddedFieldData data : addedFieldData.get(pool.getFieldrefClassName(i)))
               {
                  if (pool.getFieldrefName(i).equals(data.getName()))
                  {
                     // store the location in the const pool of the method ref
                     fieldAccessLocations.put(i, data);
                     break;
                  }

               }
            }
         }
      }

      // this means we found an instance of the call, now we have to iterate
      // through the methods and replace instances of the call
      if (!fieldAccessLocations.isEmpty())
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
                  // if the bytecode is a field access
                  if (op == Opcode.PUTFIELD || op == Opcode.GETFIELD)
                  {
                     int val = it.s16bitAt(index + 1);
                     // if the field access is for an added field
                     if (fieldAccessLocations.containsKey(val))
                     {

                        AddedFieldData data = fieldAccessLocations.get(val);

                        int arrayPos = file.getConstPool().addIntegerInfo(data.getArrayIndex());
                        // write over the field access with nop
                        it.writeByte(Opcode.NOP, index);
                        it.writeByte(Opcode.NOP, index + 1);
                        it.writeByte(Opcode.NOP, index + 2);

                        if (op == Opcode.PUTFIELD)
                        {
                           Bytecode b = new Bytecode(file.getConstPool());
                           if (data.getDescriptor().charAt(0) != 'L' && data.getDescriptor().charAt(0) != '[')
                           {
                              Boxing.box(b, data.getDescriptor().charAt(0));
                           }
                           b.add(Opcode.SWAP);

                           b.addGetfield(file.getName(), Constants.ADDED_FIELD_NAME, Constants.ADDED_FIELD_DESCRIPTOR);
                           b.addOpcode(Opcode.SWAP); // we need to keep swapping the value to put to the top
                           b.addLdc(arrayPos);
                           b.addOpcode(Opcode.SWAP);
                           b.add(Opcode.AASTORE);

                           it.insertEx(b.get());
                        }
                        else if (op == Opcode.GETFIELD)
                        {
                           Bytecode b = new Bytecode(file.getConstPool());
                           b.addGetfield(file.getName(), Constants.ADDED_FIELD_NAME, Constants.ADDED_FIELD_DESCRIPTOR);
                           b.addLdc(arrayPos);
                           b.add(Opcode.AALOAD);
                           if (DescriptorUtils.isPrimitive(data.getDescriptor()))
                           {
                              Boxing.unbox(b, data.getDescriptor().charAt(0));
                           }
                           else
                           {
                              b.addCheckcast(DescriptorUtils.getTypeStringFromDescriptorFormat(data.getDescriptor()));
                           }
                           it.insertEx(b.get());
                        }

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

   public void clearRewrites(String className, ClassLoader loader)
   {
      data.remove(className, loader);
   }

}

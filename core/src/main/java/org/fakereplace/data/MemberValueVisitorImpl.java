package org.fakereplace.data;

import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.MemberValueVisitor;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.fakereplace.util.AnnotationInstanceProvider;
/**
 * Visitor that reads annotations from a class file
 * @author stuart
 *
 */
public class MemberValueVisitorImpl implements MemberValueVisitor
{

   public MemberValueVisitorImpl(ClassLoader loader, AnnotationInstanceProvider provider)
   {
      this.loader = loader;
      this.provider = provider;
   }

   Object value;
   ClassLoader loader;
   AnnotationInstanceProvider provider;

   public void visitAnnotationMemberValue(AnnotationMemberValue node)
   {
      value = AnnotationBuilder.createAnnotation(loader, provider, node.getValue());
   }

   public void visitArrayMemberValue(ArrayMemberValue node)
   {
      MemberValue[] vals = node.getValue();
      Object[] array = new Object[vals.length];
      value = array;
      for (int i = 0; i < vals.length; ++i)
      {
         MemberValueVisitorImpl vis = new MemberValueVisitorImpl(loader, provider);
         vals[i].accept(vis);
         array[i] = vis.getValue();
      }
      // now we have an object array, however what we probably need is a
      // array of primitive types
      if (node.getType() instanceof IntegerMemberValue)
      {
         int[] ret = new int[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Integer) array[i];
         value = ret;
      }
      else if (node.getType() instanceof ShortMemberValue)
      {
         short[] ret = new short[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Short) array[i];
         value = ret;
      }
      else if (node.getType() instanceof LongMemberValue)
      {
         long[] ret = new long[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Long) array[i];
         value = ret;
      }
      else if (node.getType() instanceof ByteMemberValue)
      {
         byte[] ret = new byte[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Byte) array[i];
         value = ret;
      }
      else if (node.getType() instanceof FloatMemberValue)
      {
         float[] ret = new float[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Float) array[i];
         value = ret;
      }
      else if (node.getType() instanceof DoubleMemberValue)
      {
         double[] ret = new double[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Double) array[i];
         value = ret;
      }
      else if (node.getType() instanceof CharMemberValue)
      {
         char[] ret = new char[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Character) array[i];
         value = ret;
      }
      else if (node.getType() instanceof BooleanMemberValue)
      {
         boolean[] ret = new boolean[array.length];
         for (int i = 0; i < ret.length; ++i)
            ret[i] = (Boolean) array[i];
         value = ret;
      }

   }

   public void visitBooleanMemberValue(BooleanMemberValue node)
   {
      value = node.getValue();
   }

   public void visitByteMemberValue(ByteMemberValue node)
   {
      value = node.getValue();
   }

   public void visitCharMemberValue(CharMemberValue node)
   {
      value = node.getValue();
   }

   public void visitClassMemberValue(ClassMemberValue node)
   {
      try
      {
         value = loader.loadClass(node.getValue());
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void visitDoubleMemberValue(DoubleMemberValue node)
   {
      value = node.getValue();
   }

   public void visitEnumMemberValue(EnumMemberValue node)
   {
      value = node.getValue();
   }

   public void visitFloatMemberValue(FloatMemberValue node)
   {
      value = node.getValue();
   }

   public void visitIntegerMemberValue(IntegerMemberValue node)
   {
      value = node.getValue();
   }

   public void visitLongMemberValue(LongMemberValue node)
   {
      value = node.getValue();
   }

   public void visitShortMemberValue(ShortMemberValue node)
   {
      value = node.getValue();
   }

   public void visitStringMemberValue(StringMemberValue node)
   {
      value = node.getValue();
   }

   public Object getValue()
   {
      return value;
   }

}

package org.fakereplace.data;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * Maps a unique field signiture to an arbitary number.
 * 
 * @author stuart
 * 
 */
public class FieldReferenceDataStore
{
   static private final AtomicInteger counter = new AtomicInteger();

   private static final Map<FieldReference, Integer> addedFieldNumbers = new MapMaker().makeComputingMap(new Function<FieldReference, Integer>()
   {
      public Integer apply(FieldReference from)
      {
         return counter.incrementAndGet();
      }
   });

   public static Integer getMethodNo(String fieldName, String desc, String sig)
   {
      return addedFieldNumbers.get(new FieldReference(fieldName, desc, sig));
   }

   private static class FieldReference
   {
      private final String name;
      private final String descriptor;
      private final String signiture;

      public FieldReference(String name, String descriptor, String signiture)
      {
         this.name = name;
         this.descriptor = descriptor;
         this.signiture = signiture;
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((descriptor == null) ? 0 : descriptor.hashCode());
         result = prime * result + ((name == null) ? 0 : name.hashCode());
         result = prime * result + ((signiture == null) ? 0 : signiture.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         FieldReference other = (FieldReference) obj;
         if (descriptor == null)
         {
            if (other.descriptor != null)
               return false;
         }
         else if (!descriptor.equals(other.descriptor))
            return false;
         if (name == null)
         {
            if (other.name != null)
               return false;
         }
         else if (!name.equals(other.name))
            return false;
         if (signiture == null)
         {
            if (other.signiture != null)
               return false;
         }
         else if (!signiture.equals(other.signiture))
            return false;
         return true;
      }

   }

}

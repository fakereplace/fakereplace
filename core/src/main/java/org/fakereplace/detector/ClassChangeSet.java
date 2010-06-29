package org.fakereplace.detector;

import java.util.HashSet;
import java.util.Set;

import org.fakereplace.replacement.AddedClass;

public class ClassChangeSet
{
   final Set<AddedClass> newClasses = new HashSet<AddedClass>();

   final Set<ChangedClassData> changedClasses = new HashSet<ChangedClassData>();

   public Set<AddedClass> getNewClasses()
   {
      return newClasses;
   }

   public Set<ChangedClassData> getChangedClasses()
   {
      return changedClasses;
   }

}

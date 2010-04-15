package org.fakereplace.detector;

import java.util.HashSet;
import java.util.Set;

public class ClassChangeSet
{
   final Set<NewClassData> newClasses = new HashSet<NewClassData>();

   final Set<ChangedClassData> changedClasses = new HashSet<ChangedClassData>();

   public Set<NewClassData> getNewClasses()
   {
      return newClasses;
   }

   public Set<ChangedClassData> getChangedClasses()
   {
      return changedClasses;
   }

}

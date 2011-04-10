package org.fakereplace.detector;

import org.fakereplace.replacement.AddedClass;

import java.util.HashSet;
import java.util.Set;

public class ClassChangeSet {
    final Set<AddedClass> newClasses = new HashSet<AddedClass>();

    final Set<ChangedClassData> changedClasses = new HashSet<ChangedClassData>();

    public Set<AddedClass> getNewClasses() {
        return newClasses;
    }

    public Set<ChangedClassData> getChangedClasses() {
        return changedClasses;
    }

}

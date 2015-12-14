package org.fakereplace.core;

import javassist.bytecode.ClassFile;
import org.fakereplace.replacement.AddedClass;
import org.fakereplace.util.FileReader;
import org.fakereplace.util.WatchServiceFileSystemWatcher;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.instrument.ClassDefinition;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * Class that is responsible for watching the file system and reporting on class change events.
 * <p/>
 * Internally it uses {@link org.fakereplace.util.WatchServiceFileSystemWatcher} to watch the file system.
 *
 * @author Stuart Douglas
 */
public class FileSystemWatcher {

    private final WatchServiceFileSystemWatcher watcher = new WatchServiceFileSystemWatcher();

    private final Map<ClassLoader, Callback> callbacks = new WeakHashMap<>();
    private final Set<File> registered = new HashSet<>();


    private final class Callback implements WatchServiceFileSystemWatcher.FileChangeCallback  {

        private final ClassLoader classLoader;

        private Callback(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public void handleChanges(Collection<WatchServiceFileSystemWatcher.FileChangeEvent> changes) {
            try {
                List<AddedClass> addedClasses = new ArrayList<>();
                List<ClassDefinition> changedClasses = new ArrayList<>();
                for (WatchServiceFileSystemWatcher.FileChangeEvent change : changes) {
                    if (change.getType() == WatchServiceFileSystemWatcher.FileChangeEvent.Type.ADDED) {
                        try (FileInputStream in = new FileInputStream(change.getFile())) {
                            byte[] bytes = FileReader.readFileBytes(in);
                            ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(bytes)));
                            addedClasses.add(new AddedClass(file.getName(), bytes, classLoader));
                        }
                    } else if(change.getType() == WatchServiceFileSystemWatcher.FileChangeEvent.Type.MODIFIED) {

                        try (FileInputStream in = new FileInputStream(change.getFile())) {
                            byte[] bytes = FileReader.readFileBytes(in);
                            ClassFile file = new ClassFile(new DataInputStream(new ByteArrayInputStream(bytes)));
                            changedClasses.add(new ClassDefinition(classLoader.loadClass(file.getName()), bytes));
                        }
                    }
                }
                Agent.redefine(changedClasses.toArray(new ClassDefinition[changedClasses.size()]), addedClasses.toArray(new AddedClass[addedClasses.size()]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void addClassFile(String className, ClassLoader classLoader) {
        if(classLoader == null) {
            return;
        }
        URL resource = classLoader.getResource(className.replace(".", "/") + ".class");
        if(resource == null) {
            return;
        }
        File file = new File(resource.getFile());
        if(!file.exists()) {
            return;
        }

        int parentCount = 1;
        for(int i = 0; i < className.length(); ++i) {
            if(className.charAt(i) == '.' || className.charAt(i) == '/') {
                parentCount++;
            }
        }
        for(int i = 0; i < parentCount; ++i) {
            file = file.getParentFile();
        }
        if(registered.contains(file)) {
            return;
        }
        registered.add(file);

        Callback callback = callbacks.get(classLoader);
        if(callback == null) {
            callbacks.put(classLoader, callback = new Callback(classLoader));
        }
        watcher.watchPath(file, callback);
    }


}

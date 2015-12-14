package org.fakereplace.util;

import com.sun.nio.file.SensitivityWatchEventModifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class WatchServiceFileSystemWatcher implements Runnable, AutoCloseable {

    private static final AtomicInteger threadIdCounter = new AtomicInteger(0);
    private static int WAIT_TIME = Integer.getInteger("fakereplace.wait-time", 500);
    public static final String THREAD_NAME = "fakereplace-file-watcher";

    private WatchService watchService;
    private final Map<File, PathData> files = Collections.synchronizedMap(new HashMap<>());
    private final Map<WatchKey, PathData> pathDataByKey = Collections.synchronizedMap(new IdentityHashMap<>());

    private volatile boolean stopped = false;
    private final Thread watchThread;

    public WatchServiceFileSystemWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        watchThread = new Thread(this, THREAD_NAME + threadIdCounter);
        watchThread.setDaemon(true);
        watchThread.start();
    }

    @Override
    public void run() {
        while (!stopped) {
            try {
                final WatchKey key = watchService.take();
                if (key != null) {
                    try {
                        PathData pathData = pathDataByKey.get(key);
                        if (pathData != null) {
                            List<WatchEvent<?>> events = new ArrayList<>(key.pollEvents());
                            final List<FileChangeEvent> results = new ArrayList<>();
                            List<WatchEvent<?>> latest;
                            do {
                                //we need to wait till nothing has changed in 500ms to make sure we have picked up all the changes
                                Thread.sleep(WAIT_TIME);
                                latest = key.pollEvents();
                                events.addAll(latest);
                            } while (!latest.isEmpty());
                            final Set<File> addedFiles = new HashSet<>();
                            final Set<File> deletedFiles = new HashSet<>();
                            for (WatchEvent<?> event : events) {
                                Path eventPath = (Path) event.context();
                                File targetFile = ((Path) key.watchable()).resolve(eventPath).toFile();
                                FileChangeEvent.Type type;

                                if (event.kind() == ENTRY_CREATE) {
                                    type = FileChangeEvent.Type.ADDED;
                                    addedFiles.add(targetFile);
                                    if (targetFile.isDirectory()) {
                                        try {
                                            addWatchedDirectory(pathData, targetFile);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else if (event.kind() == ENTRY_MODIFY) {
                                    type = FileChangeEvent.Type.MODIFIED;
                                } else if (event.kind() == ENTRY_DELETE) {
                                    type = FileChangeEvent.Type.REMOVED;
                                    deletedFiles.add(targetFile);
                                } else {
                                    continue;
                                }
                                results.add(new FileChangeEvent(targetFile, type));
                            }
                            key.pollEvents().clear();

                            //now we need to prune the results, to remove duplicates
                            //e.g. if the file is modified after creation we only want to
                            //show the create event
                            Iterator<FileChangeEvent> it = results.iterator();
                            while (it.hasNext()) {
                                FileChangeEvent event = it.next();
                                if (event.getType() == FileChangeEvent.Type.MODIFIED) {
                                    if (addedFiles.contains(event.getFile()) ||
                                            deletedFiles.contains(event.getFile())) {
                                        it.remove();
                                    }
                                } else if (event.getType() == FileChangeEvent.Type.ADDED) {
                                    if (deletedFiles.contains(event.getFile())) {
                                        it.remove();
                                    }
                                } else if (event.getType() == FileChangeEvent.Type.REMOVED) {
                                    if (addedFiles.contains(event.getFile())) {
                                        it.remove();
                                    }
                                }
                            }

                            if (!results.isEmpty()) {
                                for (FileChangeCallback callback : pathData.callbacks) {
                                    invokeCallback(callback, results);
                                }
                            }
                        }
                    } finally {
                        //if the key is no longer valid remove it from the files list
                        if (!key.reset()) {
                            files.remove(key.watchable());
                        }
                    }
                }
            } catch (InterruptedException e) {
                //ignore
            } catch (ClosedWatchServiceException cwse) {
                // the watcher service is closed, so no more waiting on events
                // @see https://developer.jboss.org/message/911519
                break;
            }
        }
    }

    public synchronized void watchPath(File file, FileChangeCallback callback) {
        try {
            PathData data = files.get(file);
            if (data == null) {
                Set<File> allDirectories = doScan(file).keySet();
                Path path = Paths.get(file.toURI());
                data = new PathData(path);
                for (File dir : allDirectories) {
                    addWatchedDirectory(data, dir);
                }
                files.put(file, data);
            }
            data.callbacks.add(callback);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addWatchedDirectory(PathData data, File dir) throws IOException {
        Path path = Paths.get(dir.toURI());
        WatchKey key = path.register(watchService, new WatchEvent.Kind[] {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
        pathDataByKey.put(key, data);
        data.keys.add(key);
    }

    public synchronized void unwatchPath(File file, final FileChangeCallback callback) {
        PathData data = files.get(file);
        if (data != null) {
            data.callbacks.remove(callback);
            if (data.callbacks.isEmpty()) {
                files.remove(file);
                for (WatchKey key : data.keys) {
                    key.cancel();
                    pathDataByKey.remove(key);
                }

            }
        }
    }

    @Override
    public void close() throws IOException {
        this.stopped = true;
        watchThread.interrupt();
        if (watchService != null) {
            watchService.close();
        }
    }


    private static Map<File, Long> doScan(File file) {
        final Map<File, Long> results = new HashMap<>();

        final Deque<File> toScan = new ArrayDeque<File>();
        toScan.add(file);
        while (!toScan.isEmpty()) {
            File next = toScan.pop();
            if (next.isDirectory()) {
                results.put(next, next.lastModified());
                File[] list = next.listFiles();
                if (list != null) {
                    for (File f : list) {
                        toScan.push(new File(f.getAbsolutePath()));
                    }
                }
            }
        }
        return results;
    }

    private static void invokeCallback(FileChangeCallback callback, List<FileChangeEvent> results) {
        try {
            callback.handleChanges(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PathData {
        final Path path;
        final List<FileChangeCallback> callbacks = new ArrayList<>();
        final List<WatchKey> keys = new ArrayList<>();

        private PathData(Path path) {
            this.path = path;
        }
    }


    /**
     * The event object that is fired when a file system change is detected.
     *
     * @author Stuart Douglas
     */
    public static class FileChangeEvent {

        private final File file;
        private final Type type;

        /**
         * Construct a new instance.
         *
         * @param file the file which is being watched
         * @param type the type of event that was encountered
         */
        public FileChangeEvent(File file, Type type) {
            this.file = file;
            this.type = type;
        }

        /**
         * Get the file which was being watched.
         *
         * @return the file which was being watched
         */
        public File getFile() {
            return file;
        }

        /**
         * Get the type of event.
         *
         * @return the type of event
         */
        public Type getType() {
            return type;
        }

        /**
         * Watched file event types.  More may be added in the future.
         */
        public static enum Type {
            /**
             * A file was added in a directory.
             */
            ADDED,
            /**
             * A file was removed from a directory.
             */
            REMOVED,
            /**
             * A file was modified in a directory.
             */
            MODIFIED,
        }

    }

    /**
     * Callback for file system change events
     *
     * @author Stuart Douglas
     */
    public interface FileChangeCallback {

        /**
         * Method that is invoked when file system changes are detected.
         *
         * @param changes the file system changes
         */
        void handleChanges(final Collection<FileChangeEvent> changes);

    }

}

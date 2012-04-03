package org.fakereplace.index;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class that tracks unmodified files that can be ignored on future boots. This provides a big speed improvement,
 * as it means that only classes that actually have to be modified are parsed by javassist.
 *
 * @author Stuart Douglas
 */
public class UnmodifiedFileIndex {

    public static String VERSION = "1.0";

    private static final String FILE_PROPERTY = "org.fakereplace.index";

    private static final String FILENAME = "fakereplace.index";

    private static final Set<String> index = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public static void loadIndex() {
        final File file = getFile();
        if (file.exists() && !file.isDirectory()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                final String version = reader.readLine();
                if (VERSION.equals(version)) {
                    String line = reader.readLine();
                    while (line != null) {
                        index.add(line);
                        line = reader.readLine();
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        final Thread writerThread = new Thread(new Runnable() {
            public void run() {
                final File file = getFile();
                if (!file.isDirectory()) {
                    Writer writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(file));
                        writer.write(VERSION);
                        writer.write('\n');
                        //there is no real need to sort
                        //but it makes the file easier to examine when debugging
                        final List<String> sortedIndex = new ArrayList<String>(index);
                        Collections.sort(sortedIndex);
                        for (String clazz : sortedIndex) {
                            writer.write(clazz);
                            writer.write('\n');
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (writer != null) {
                            try {
                                writer.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                }
            }
        });
        Runtime.getRuntime().addShutdownHook(writerThread);
    }

    private static File getFile() {
        final String fileProp = System.getProperty(FILE_PROPERTY);
        if(fileProp != null) {
            return new File(fileProp);
        }
        return new File(FILENAME);
    }

    public static void markClassUnmodified(final String clazz) {
        index.add(clazz);
    }

    public static boolean isClassUnmodified(final String clazz) {
        return index.contains(clazz);
    }


}

/*
 * Copyright 2016, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.fakereplace.transformation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.fakereplace.core.AgentOption;
import org.fakereplace.core.AgentOptions;
import org.fakereplace.logging.Logger;

/**
 * Class that tracks unmodified files that can be ignored on future boots. This provides a big speed improvement,
 * as it means that only classes that actually have to be modified are parsed by javassist.
 *
 * @author Stuart Douglas
 */
public class UnmodifiedFileIndex {

    private static String VERSION = "1.0";

    private static final Logger log = Logger.getLogger(UnmodifiedFileIndex.class);

    private static final Set<String> index = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private static Timer writeTimer = null;

    public static void loadIndex() {
        final File file = getFile();
        if(file == null) {
            return;
        }
        if (file.exists() && !file.isDirectory()) {
            log.debug("Reading Fakereplace unmodified class cache from " + file.getAbsolutePath());
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
                final String version = reader.readLine();
                if (VERSION.equals(version)) {
                    String line = reader.readLine();
                    while (line != null) {
                        index.add(line);
                        line = reader.readLine();
                    }
                }

            } catch (IOException e) {
                log.error("Failed to load unmodified file index", e);
            }
        }
        final Thread writerThread = new Thread(() -> {
            synchronized (UnmodifiedFileIndex.class) {
                writeIndex();
            }
        });
        Runtime.getRuntime().addShutdownHook(writerThread);
    }

    private static void writeIndex() {
        final File file = getFile();
        if(file == null) {
            return;
        }
        log.debug("Writing Fakereplace unmodified class cache at " + file.getAbsolutePath());
        if (!file.isDirectory()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)){
                writer.write(VERSION);
                writer.write('\n');
                //there is no real need to sort
                //but it makes the file easier to examine when debugging
                final List<String> sortedIndex = new ArrayList<>(index);
                Collections.sort(sortedIndex);
                for (String clazz : sortedIndex) {
                    writer.write(clazz);
                    writer.write('\n');
                }
            } catch (IOException e) {
                log.error("Failed to write unmodified file index", e);
            }
        }
    }

    private static File getFile() {
        String noIndex = AgentOptions.getOption(AgentOption.NO_INDEX);
        if(noIndex != null && Boolean.parseBoolean(noIndex)) {
            return null;
        }

        final String fileProp = AgentOptions.getOption(AgentOption.INDEX_FILE);
        return new File(fileProp);
    }

    static synchronized  void markClassUnmodified(final String clazz) {
        index.add(clazz);
        if(writeTimer == null && getFile() != null) {
            //the shutdown hook is not always reliable, so we write the index every 10 seconds
            //but only if new classes are added to it
            writeTimer = new Timer("Fakereplace index writing timer", true);
            writeTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (UnmodifiedFileIndex.class) {
                        writeIndex();
                        writeTimer.cancel();
                        writeTimer = null;
                    }
                }
            }, 10000);
        }
    }

    static boolean isClassUnmodified(final String clazz) {
        return index.contains(clazz);
    }


}

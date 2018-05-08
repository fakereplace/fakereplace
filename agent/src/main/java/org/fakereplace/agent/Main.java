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

package org.fakereplace.agent;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

    private static final String WEB_RESOURCES_DIR = "web.resources.dir";
    private static final String SRCS_DIR = "srcs.dir";
    private static final String CLASSES_DIR = "classes.dir";
    private static final String REMOTE_PASSWORD = "remote.password";


    public static void main(String... args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar agent.jar /path/to/class-change.properties http(s)://remotehost:port/path");
        }
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(args[0])) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        String pw = props.getProperty(REMOTE_PASSWORD);
        if (pw == null) {
            System.out.println("No remote password in supplied properties file");
            System.exit(1);
        }
        String web = props.getProperty(WEB_RESOURCES_DIR);
        String srcs = props.getProperty(SRCS_DIR);
        String classes = props.getProperty(CLASSES_DIR);


        if (web == null && srcs == null && classes == null) {
            System.out.println("No local locations specified to check for changes, exiting");
            System.exit(1);
        }
        AgentRunner runner = new AgentRunner(web, srcs, classes, args[1], pw);
        runner.run();
        for (; ; ) {
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {

            }
        }
    }
}

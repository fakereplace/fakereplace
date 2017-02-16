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

package org.fakereplace.maven;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stuart Douglas
 */
public class Util {

    public static byte[] getBytesFromFile(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return getBytesFromStream(is);
        }
    }

    public static byte[] getBytesFromStream(InputStream is) throws IOException {
        // Create the byte array to hold the data
        final ByteArrayOutputStream out = new ByteArrayOutputStream(is.available());

        int read = -1;
        final byte[] buff = new byte[512];
        while ((read = is.read(buff)) != -1) {
            out.write(buff, 0, read);
        }
        return out.toByteArray();
    }
}

/*
 * Copyright 2012, Stuart Douglas, and individual contributors as indicated
 * by the @authors tag.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.fakereplace.client.standalone;

import org.fakereplace.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Stuart Douglas
 */
public class Util {
    private static final Logger log = Logger.getLogger(Util.class);

    public static byte[] getBytesFromFile(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return getBytesFromStream(is);
        } catch (Exception e) {
            log.error("Unable to read file " + file, e);
            return new byte[0];
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

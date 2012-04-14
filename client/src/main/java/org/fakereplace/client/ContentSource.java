package org.fakereplace.client;

import java.io.IOException;

/**
 * @author Stuart Douglas
 */
public interface ContentSource {

    byte[] getData() throws IOException;

}

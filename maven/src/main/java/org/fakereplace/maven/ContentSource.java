package org.fakereplace.maven;

import java.io.IOException;

/**
 * @author Stuart Douglas
 */
public interface ContentSource {

    byte[] getData() throws IOException;

}

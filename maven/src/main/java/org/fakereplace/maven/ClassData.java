package org.fakereplace.maven;

/**
* @author Stuart Douglas
*/
public final class ClassData {
    private final String className;
    private final long timestamp;
    private final ContentSource contentSource;

    public ClassData(String className, long timestamp, final ContentSource contentSource) {
        this.className = className;
        this.timestamp = timestamp;
        this.contentSource = contentSource;
    }

    public String getClassName() {
        return className;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ContentSource getContentSource() {
        return contentSource;
    }
}

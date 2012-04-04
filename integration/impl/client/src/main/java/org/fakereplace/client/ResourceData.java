package org.fakereplace.client;

/**
* @author Stuart Douglas
*/
public final class ResourceData {
    private final long timestamp;
    private final String relativePath;
    private final ContentSource contentSource;

    public ResourceData(final String relativePath, long time, final ContentSource contentSource) {
        this.relativePath = relativePath;
        this.timestamp = time;
        this.contentSource = contentSource;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ContentSource getContentSource() {
        return contentSource;
    }
}

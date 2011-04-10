package org.fakereplace.api;

public interface ClassTransformer {
    public byte[] transform(byte[] file, String className);
}

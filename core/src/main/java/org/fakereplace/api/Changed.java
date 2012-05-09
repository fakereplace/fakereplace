package org.fakereplace.api;

/**
 * @author Stuart Douglas
 */
public interface Changed<T> {
    ChangeType getType();

    T getModified();

    T getExisting();
}

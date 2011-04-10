package org.fakereplace.util;

import java.lang.reflect.Method;

/**
 * Exception thrown when a annotation is created with a null value
 * for one of the members.
 *
 * @author Stuart Douglas
 */
public class NullMemberException extends RuntimeException {
    Class annotationType;
    Method method;

    public NullMemberException(Class annotationType, Method method, String message) {
        super(message);
        this.annotationType = annotationType;
        this.method = method;
    }

    public Class getAnnotationType() {
        return annotationType;
    }

    public Method getMethod() {
        return method;
    }

}

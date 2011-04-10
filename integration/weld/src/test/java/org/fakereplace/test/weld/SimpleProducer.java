package org.fakereplace.test.weld;

import javax.enterprise.inject.Produces;

public class SimpleProducer {
    @Produces
    public String value = "hello world";
}

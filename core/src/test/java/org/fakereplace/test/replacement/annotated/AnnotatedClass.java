package org.fakereplace.test.replacement.annotated;

@Annotation1(svalue = "test", cvalue = AnnotatedClass.class)
public class AnnotatedClass {

    @Annotation1(svalue = "hello", cvalue = AnnotatedClass.class)
    public String field;

}

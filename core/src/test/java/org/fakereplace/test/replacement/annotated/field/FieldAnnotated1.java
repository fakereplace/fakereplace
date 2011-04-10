package org.fakereplace.test.replacement.annotated.field;

public class FieldAnnotated1 {
    @FieldAnnotation("1")
    public int field1;

    public int field2;

    @FieldAnnotation("3")
    public int field3;

    public void fn() {
        field1 = field3;
    }
}

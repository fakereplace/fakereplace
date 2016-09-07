package a.org.fakereplace.test.replacement.instancefield;

/**
 * @author Stuart Douglas
 */
public class ReaderClass1 {

    private FieldClass1 fieldClass = new FieldClass1();

    int readField() {
        return fieldClass.field;
    }

    void writeField(int value) {
        fieldClass.field = value;
    }
}

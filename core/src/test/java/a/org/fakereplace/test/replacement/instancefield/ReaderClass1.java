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

    int readStaticField() {
        return fieldClass.staticField;
    }

    void writeStaticField(int value) {
        fieldClass.staticField = value;
    }
}

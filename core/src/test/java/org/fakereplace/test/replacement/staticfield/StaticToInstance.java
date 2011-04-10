package org.fakereplace.test.replacement.staticfield;

public class StaticToInstance {
    private static int field = 10;

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

}

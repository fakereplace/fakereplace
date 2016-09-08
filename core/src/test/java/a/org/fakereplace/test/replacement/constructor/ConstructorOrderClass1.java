package a.org.fakereplace.test.replacement.constructor;

import org.fakereplace.util.NoInstrument;

/**
 * @author Stuart Douglas
 */
@NoInstrument
public class ConstructorOrderClass1 {
    final String data;

    public ConstructorOrderClass1(String data, String more){
        this.data = data + more;
    }

    public String getData() {
        return data;
    }
}

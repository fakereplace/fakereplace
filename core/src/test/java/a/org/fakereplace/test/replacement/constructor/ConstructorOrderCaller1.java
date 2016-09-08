package a.org.fakereplace.test.replacement.constructor;

import org.fakereplace.util.NoInstrument;

/**
 * @author Stuart Douglas
 */
@NoInstrument
public class ConstructorOrderCaller1 {

    public ConstructorOrderClass1 getOrder() {
        return new ConstructorOrderClass1("ne" , "w");
    }

}

package a.org.fakereplace.test.replacement.virtualmethod;

import org.fakereplace.util.NoInstrument;

/**
 * @author Stuart Douglas
 */
@NoInstrument
public class OrderCaller1 {

    private final OrderClass1 orderClass = new OrderClass1();

    public String getMessage() {
        return orderClass.sayHello();
    }

}

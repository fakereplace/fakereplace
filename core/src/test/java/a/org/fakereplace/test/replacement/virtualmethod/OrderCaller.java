package a.org.fakereplace.test.replacement.virtualmethod;

/**
 * @author Stuart Douglas
 */
public class OrderCaller {

    private final OrderClass orderClass = new OrderClass();

    public String getMessage() {
        return "bye";
    }

}

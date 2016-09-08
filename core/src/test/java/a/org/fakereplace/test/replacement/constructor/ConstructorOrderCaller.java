package a.org.fakereplace.test.replacement.constructor;

/**
 * @author Stuart Douglas
 */
public class ConstructorOrderCaller {

    public ConstructorOrderClass getOrder() {
        return new ConstructorOrderClass("old");
    }

}

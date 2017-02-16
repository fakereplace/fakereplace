package a.org.fakereplace.test.replacement.lambda;

import java.util.function.Supplier;

public class LambdaInstanceClassNewMethod {
    public Supplier<String> getMessageProducer() {
        return () -> "first";
    }
}

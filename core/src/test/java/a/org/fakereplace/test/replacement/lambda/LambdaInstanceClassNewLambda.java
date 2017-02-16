package a.org.fakereplace.test.replacement.lambda;

import java.util.function.Supplier;

public class LambdaInstanceClassNewLambda {
    public Supplier<String> getMessageProducer() {
        return () -> "first";
    }
}

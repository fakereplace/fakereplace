package a.org.fakereplace.test.replacement.lambda;

import java.util.function.Function;
import java.util.function.Supplier;

public class LambdaInstanceClassNewLambdaM {
    public Supplier<String> getMessageProducer() {
        Function<String, String> sup = (s) -> "th";
        return () -> "tr";
    }
}

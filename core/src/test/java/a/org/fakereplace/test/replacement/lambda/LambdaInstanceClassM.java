package a.org.fakereplace.test.replacement.lambda;

import org.fakereplace.util.NoInstrument;

import java.util.function.Supplier;

@NoInstrument
public class LambdaInstanceClassM {
    public Supplier<String> getMessageProducer() {
        return () -> "second";
    }
}

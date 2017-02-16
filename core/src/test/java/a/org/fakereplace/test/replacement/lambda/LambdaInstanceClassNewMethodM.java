package a.org.fakereplace.test.replacement.lambda;

import org.fakereplace.util.NoInstrument;

import java.util.function.Supplier;

@NoInstrument
public class LambdaInstanceClassNewMethodM {
    public Supplier<String> getMessageProducer() {
        return () -> "third orig";
    }

    public Supplier<String> getAnotherMessageProducer() {
        return () -> "third new";
    }
}

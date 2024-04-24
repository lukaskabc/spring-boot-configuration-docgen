package cz.lukaskabc.cvut.processor.configuration.tests.descriptors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties
public class SingleConstructorConfiguration {
    private final String foo;
    private final Integer bar;
    @Setter
    private String nonFinal;

    public SingleConstructorConfiguration(String foo, Integer bar) {
        this.foo = foo;
        this.bar = bar;
        this.nonFinal = "constructor value";
    }
}

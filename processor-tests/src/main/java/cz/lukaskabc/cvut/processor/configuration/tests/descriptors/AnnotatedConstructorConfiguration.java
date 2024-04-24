package cz.lukaskabc.cvut.processor.configuration.tests.descriptors;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Getter
@ConfigurationProperties
public class AnnotatedConstructorConfiguration {
    private String foo;
    private Integer bar;

    public AnnotatedConstructorConfiguration() {
    }

    @ConstructorBinding
    public AnnotatedConstructorConfiguration(String foo, Integer bar) {
        this.foo = foo;
        this.bar = bar;
    }

    public AnnotatedConstructorConfiguration(String foo) {
        this.foo = foo;
    }

    public AnnotatedConstructorConfiguration(Integer bar) {
        this.bar = bar;
    }
}

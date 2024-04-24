package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Getter
@ConfigurationProperties
public class ConstructorBindingWithDefaultConfiguration {
    private final boolean nonDefaultAttribute;
    private String stringAttribute = "attribute value"; // this value should not be in the documentation
    private int[] intArrayAttribute = {2, 3};

    @ConstructorBinding
    public ConstructorBindingWithDefaultConfiguration(@DefaultValue("string value") String stringAttribute, @DefaultValue({"1", "5"}) int[] intArrayAttribute, boolean nonDefaultAttribute) {
        this.stringAttribute = stringAttribute;
        this.intArrayAttribute = intArrayAttribute;
        this.nonDefaultAttribute = nonDefaultAttribute;
    }
}

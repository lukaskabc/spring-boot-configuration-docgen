package cz.lukaskabc.cvut.processor.configuration.tests.descriptors;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties
@Data
public class ConstructorBindingWithNonAttributeParameterConfiguration {
    /**
     * Attribute description
     */
    private int attribute;

    private String nonConstructorAttribute;

    /**
     * Constructor description
     *
     * @param attribute             description for attribute constructor parameter
     * @param nonAttributeParameter description for non attribute constructor parameter
     */
    @ConstructorBinding
    public ConstructorBindingWithNonAttributeParameterConfiguration(int attribute, String nonAttributeParameter) {
        this.attribute = attribute;
    }
}

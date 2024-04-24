package cz.lukaskabc.cvut.processor.configuration.tests.value;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "value")
public class ValueInPropertiesAnnotation {
    /**
     * This attribute is in configuration properties class
     */
    private String onlyInProperties;

    /**
     * This attribute is in configuration properties class and has a {@link Value} annotation
     */
    @Value("${value.value_annotation}")
    private String valueInProperties;

    /**
     * This attribute has a {@link Value} annotation with matching config name with properties class
     */
    @Value("${value.value-in-properties}")
    private String valueMatchingProperties;
}

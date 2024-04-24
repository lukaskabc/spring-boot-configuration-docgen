package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "external")
@Data
public class ExternalClassAttributeConfiguration {
    /**
     * This is an attribute Javadoc comment
     */
    @NestedConfigurationProperty
    ExternalClass attributeName;

    /**
     * This attribute is also bindable although it is not annotated with {@link NestedConfigurationProperty}
     */
    ExternalRecord recordWithoutNestedAnnotation;

    /**
     * This is an attribute Javadoc comment for external record type
     */
    @NestedConfigurationProperty
    ExternalRecord attributeRecordName;

    ExternalClass nonConvertibleAttribute;
}

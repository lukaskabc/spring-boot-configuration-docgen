package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This class shows invalid usage of {@link ConfigurationProperties} on a nested class which results in bean "duplication".
 */
@Data
@ConfigurationProperties("nested")
public class SpringNestedConfiguration {

    private NestedClass classAttribute;

    // @ConfigurationProperties in combination with @Configuration or @ConfigurationPropertiesScan results in initialization of new bean "nestedClass"
    // which means that there are two configuration beans "springDbouleNestedConfiguration" and "nestedClass"
    // first one maps configuration from NESTED_CLASSATTRIBUTE_VALUE and second one maps configuration from NESTEDCLASS_VALUE
    // this would be more confusing when atribute name matches the nested class "private NestedClass nestedClass;"
    // -> NESTED_NESTEDCLASS_VALUE & NESTEDCLASS_VALUE
    @Data
    @ConfigurationProperties("nested-class")
    public static class NestedClass {
        String value;
    }
}

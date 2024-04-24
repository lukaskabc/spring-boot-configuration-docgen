package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("recursive")
@AllArgsConstructor
@Data
public class RecursivePropertiesConfiguration {
    /**
     * Recursive attribute javadoc.
     */
    Nested recordAttribute;
    /**
     * String attribute javadoc.
     */
    private String attribute;

    public record Nested(String nestedAttribute, Nested nestedRecursiveAttribute) {
    }
}

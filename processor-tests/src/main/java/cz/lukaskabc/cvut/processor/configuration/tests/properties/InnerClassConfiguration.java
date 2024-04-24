package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Javadoc of top level ConfigurationProperties class
 */
@Data
@ConfigurationProperties(prefix = "toplevel-prefix")
public class InnerClassConfiguration {
    /**
     * Top level attribute
     */
    String value;

    /**
     * Top level attribute of SubClass type
     */
    SubClass subClassAttributeName = new SubClass();

    /**
     * Inner class Javadoc
     */
    @Data
    public static class SubClass {
        /**
         * Attribute Javadoc in inner class
         */
        Integer subValue;
    }

}

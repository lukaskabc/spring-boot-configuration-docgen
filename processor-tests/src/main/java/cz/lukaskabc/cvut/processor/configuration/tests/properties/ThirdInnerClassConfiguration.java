package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Javadoc of top level ConfigurationProperties class
 */
@Data
@ConfigurationProperties
public class ThirdInnerClassConfiguration {

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
         * Attribute in inner class
         */
        Integer subValue;

        /**
         * SubClass attribute of a second SubClass2 type
         */
        SubClass2 subClass2AttributeName = new SubClass2();

        /**
         * Second inner class Javadoc
         */
        @Data
        public static class SubClass2 {
            /**
             * Attribute of SubClass2
             */
            Integer subClass2Value;
            /**
             * SubClass2 attribute of a DeepSubClass type
             */
            DeepSubClass deepSubClassAttributeName = new DeepSubClass();

            @Data
            public static class DeepSubClass {
                /**
                 * DeepSubClass attribute
                 */
                Integer deepSubValue;
            }
        }
    }

}

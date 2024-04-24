package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import lombok.Setter;

/**
 * This class does not have any annotation
 */
@Setter
public class ExternalClass {
    /**
     * Top level attribute
     */
    private String value;

    /**
     * Top level attribute of SubClass type
     */
    private SubClass subClassAttributeName = new SubClass();

    /**
     * Inner class Javadoc
     */
    @Setter
    public static class SubClass {
        /**
         * Attribute Javadoc in inner class
         */
        private Integer subValue;

        public Integer testGetSubValue() {
            return subValue;
        }
    }

    public String testGetValue() {
        return value;
    }

    public SubClass testGetSubClassAttribute() {
        return subClassAttributeName;
    }

}


package cz.lukaskabc.cvut.processor.configuration.tests.value;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * ValueAnnotation class Javadoc comment
 */
@Data
@Configuration
public class ValueAnnotationConfiguration {
    /**
     * Attribute with @Value annotation
     */
    @Value("${tests_value_attribute}")
    String attribute;

    /**
     * Autowired constructor with direct usage of @Value at parameter
     *
     * @param constructorParameterValue constructor parameter with @Value annotation
     */
    @Autowired
    public ValueAnnotationConfiguration(@Value("${testsValue-constructor}") String constructorParameterValue) {
    }

    /**
     * Method with @Value annotation
     *
     * @param parameter method parameter with @Value annotation
     */
    @Value("${tests-value-method}")
    public void methodWithValue(Integer parameter) {

    }

    /**
     * Method with @Value annotation at parameter
     *
     * @param parameter method parameter with @Value annotation
     */

    public void methodWithValueParameter(@Value("${tests_value-parameter}") Double parameter) {
    }

    /**
     * Method with @Value annotation
     *
     * @param parameter  parameter with @Value annotation of method with @Value
     * @param parameter2 parameter without @Value
     */
    @Value("${tests-value-method-combination}")
    public void methodWithValueCombination(@Value("${tests_value_method_parameter}") Integer parameter, String parameter2) {

    }
}

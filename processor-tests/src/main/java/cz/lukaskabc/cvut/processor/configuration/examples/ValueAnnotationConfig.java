package cz.lukaskabc.cvut.processor.configuration.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration()
@Validated
public class ValueAnnotationConfig {

    @Value("${value-attribute}")
    String attribute;
    Integer methodValue;
    Double methodParameterValue;
    String constructorParameterValue;

    @Value("${optional-int:#{null}}")
    Integer intValue;

    /**
     * @param constructorParameterValue parameter of constructor
     */
    @Autowired
    public ValueAnnotationConfig(@Value("${value-constructor}")
                                 String constructorParameterValue) {
        this.constructorParameterValue = constructorParameterValue;
    }

    /**
     * @param parameter   with direct @Value
     * @param methodValue with value from methods @Value
     * @author lukaskabc
     */
    @Value("${value-method}")
    public void setMethodWithParameterValue(@Value("${value-parameter}")
                                            Double parameter,
                                            Integer methodValue) {
        methodParameterValue = parameter;
    }

    public String getAttribute() {
        return attribute;
    }

    public Integer getMethodValue() {
        return methodValue;
    }

    @Value("${value-method}")
    public void setMethodValue(Integer parameter) {
        methodValue = parameter;
    }

    public Double getMethodParameterValue() {
        return methodParameterValue;
    }

    public String getConstructorParameterValue() {
        return constructorParameterValue;
    }

    public Integer getIntValue() {
        return intValue;
    }
}

package cz.lukaskabc.cvut.processor.configuration.examples;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "prefix")
@Validated
public class ConfigurationPropertiesConfig {

    String value;

    SubClass subClass = new SubClass();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SubClass getSubClass() {
        return subClass;
    }

    public void setSubClass(SubClass subClass) {
        this.subClass = subClass;
    }

    public static class SubClass {
        Integer subValue;

        public Integer getSubValue() {
            return subValue;
        }

        public void setSubValue(Integer subValue) {
            this.subValue = subValue;
        }
    }
}

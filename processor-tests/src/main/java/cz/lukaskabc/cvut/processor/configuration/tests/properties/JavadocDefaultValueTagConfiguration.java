package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
@Data
public class JavadocDefaultValueTagConfiguration {
    /**
     * @configurationdoc.default javadoc default value
     */
    private String attribute = "some value";
    private Integer constructorDefault;
    private InnerClass innerClass;

    /**
     * This attribute is hidden by javadoc tag
     * @hidden
     */
    private String hiddenAttribute;

    /**
     * @param constructorDefault {@configurationdoc.default constructor javadoc default: 52}
     * @param attribute constructor param javadoc
     * @configurationdoc.default no effect tag
     */
    public JavadocDefaultValueTagConfiguration(String attribute, Integer constructorDefault, InnerClass innerClass, String hiddenAttribute) {
        this.attribute = attribute;
        this.constructorDefault = constructorDefault;
        this.innerClass = innerClass;
        this.hiddenAttribute = hiddenAttribute;
    }

    @Data
    public static class InnerClass {
        /**
         * inner class attribute description
         * @configurationdoc.default inner class javadoc default value
         */
        private String nestedAttribute;
    }
}

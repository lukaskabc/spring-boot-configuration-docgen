package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanMethodPropertiesConfiguration {
    @Bean
    @ConfigurationProperties("bean-method.javabean")
    public ExternalClass externalClass() {
        return new ExternalClass();
    }

    /**
     * Javadoc on bean method
     * @return inner class configuration
     */
    @Bean
    @ConfigurationProperties("bean-method.javabean-constructor")
    public InnerClass innerClass() {
        var config = new InnerClass("value from bean method");
        config.setNonFinalAttribute("value from bean method");
        return config;
    }

    @Data
    public static class InnerClass {
        /**
         * Non final attribute javadoc
         */
        private String nonFinalAttribute;
        private final String finalAttribute;

        public InnerClass(String finalAttribute) {
            this.finalAttribute = finalAttribute;
            nonFinalAttribute = null;
        }
    }

}

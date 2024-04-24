package cz.lukaskabc.cvut.processor.configuration;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;


@Configuration
@Validated
public class BasicValueConfig {
    public SubClass subClass = new SubClass();
    @Value("${top-level-value}")
    @NotNull
    private String topLevelValue;

    public static class SubClass {
        @Value("${sub-class-value}")
        public String subClassValue;

        public static class SubSubClass {
            @Value("${sub-sub-class-value}")
            public String subSubClassValue;
        }
    }
}

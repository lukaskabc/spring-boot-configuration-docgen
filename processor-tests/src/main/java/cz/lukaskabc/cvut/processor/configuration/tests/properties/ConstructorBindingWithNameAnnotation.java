package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.bind.Name;

/**
 * <a href="https://github.com/spring-projects/spring-boot/blob/main/spring-boot-project/spring-boot-tools/spring-boot-configuration-processor/src/test/java/org/springframework/boot/configurationsample/immutable/ImmutableNameAnnotationProperties.java"
 *   >Github SpringBoot configuration processor @Name test</a>
 * <br>
 * <a href="https://docs.spring.io/spring-boot/docs/3.2.1/api/org/springframework/boot/context/properties/bind/Name.html"
 *   >@Name javadoc</a>
 */
@ConfigurationProperties("named")
@Data
public class ConstructorBindingWithNameAnnotation {
    private AnnotatedConstructor annotatedConstructor;
    private SingleConstructor singleConstructor;
    private Box box;

    @Getter
    public static class AnnotatedConstructor {
        private final String name;

        @ConstructorBinding
        public AnnotatedConstructor(@Name("annotated-name") String name) {
            this.name = name;
        }

        /**
         * Default constructor
         */
        public AnnotatedConstructor() {
            this.name = "some-default";
        }
    }

    @Getter
    public static class SingleConstructor {
        private final String name;

        public SingleConstructor(@Name("single-constructor-name") String name) {
            this.name = name;
        }
    }

    @Getter
    public static class Box {
        private final SingleConstructor singleConstructor;

        public Box(@Name("boxed") SingleConstructor singleConstructor) {
            this.singleConstructor = singleConstructor;
        }
    }
}

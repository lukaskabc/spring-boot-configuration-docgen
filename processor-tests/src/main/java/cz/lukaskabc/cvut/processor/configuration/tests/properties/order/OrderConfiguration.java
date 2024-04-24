package cz.lukaskabc.cvut.processor.configuration.tests.properties.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order")
@Data
public class OrderConfiguration {
    String z;
    String y;
    String x;

    String a;
    String b;
    String c;

    @NotNull
    String d;

    Nested d_nested = new Nested();

    @NotNull
    String e;
    String f;

    /**
     * @deprecated reason for deprecation
     */
    @Deprecated
    String g;

    /**
     * @deprecated reason for deprecation
     */
    @Deprecated
    String h;

    String i;
    String j;

    @Data
    public static class Nested {
        String a;
        /**
         * @deprecated reason for deprecation
         */
        @NotNull
        @Deprecated
        String b;
        String c;
    }

}

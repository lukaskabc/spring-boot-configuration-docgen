package cz.lukaskabc.cvut.processor.configuration.tests;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class JSR303MultipleMessagesConfiguration {
    @Digits(integer = 3, fraction = 1, message = "i = 3, f = 1")
    @Digits(integer = 2, fraction = 3, message = "i = 2, f = 3")
    @Digits(integer = 1, fraction = 4, message = "i = 1, f = 4")
    Double multipleDigits;

    @AssertFalse(message = "just false")
    @AssertFalse(message = "really false")
    boolean mustBeReallyFalse;

    @DecimalMax(value = "7", message = "max 7")
    @DecimalMax(value = "5", message = "max 5")
    @DecimalMax(value = "6", message = "max 6")
    Double multipleDecimalMax;

    @Pattern(regexp = "^[a-z]+$", message = "lowercase")
    @Pattern(regexp = "^[a-z_]+$", message = "lowercase with underscore")
    String multiplePattern;

    @Size(min = 2, max = 5, message = "min 2, max 5")
    @Size(min = 5, max = 8, message = "min 5, max 8")
    @Size(min = 1, max = 10, message = "min 1, max 10")
    String multipleSize;
}

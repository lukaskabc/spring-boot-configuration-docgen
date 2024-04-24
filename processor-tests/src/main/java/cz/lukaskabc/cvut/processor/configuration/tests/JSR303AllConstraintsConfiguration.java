package cz.lukaskabc.cvut.processor.configuration.tests;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Date;

@Data
@ConfigurationProperties
public class JSR303AllConstraintsConfiguration {
    @AssertFalse
    boolean mustBeFalse;

    /**
     * Multiple assert false should have no additional effect
     */
    @AssertFalse
    @AssertFalse
    boolean mustBeReallyFalse;

    @AssertTrue
    boolean mustBeTrue;

    /**
     * Multiple assert true should have no additional effect
     */
    @AssertTrue
    @AssertTrue
    boolean mustBeReallyTrue;

    @DecimalMax("5.3")
    Double decimalMax;

    /**
     * When multiple MAX are present, the smallest one should be used
     */
    @DecimalMax("7")
    @DecimalMax("5")
    @DecimalMax("6")
    Double multipleDecimalMax;

    @DecimalMin("0.5")
    Double decimalMin;

    /**
     * When multiple MIN are present, the biggest one should be used
     */
    @DecimalMin("0")
    @DecimalMin("2")
    @DecimalMin("-1")
    Double multipleDecimalMin;

    @Digits(integer = 3, fraction = 2)
    Double digits;

    /**
     * When multiple digits are present, the strictest one should be used
     */
    @Digits(integer = 3, fraction = 1)
    @Digits(integer = 2, fraction = 3)
    @Digits(integer = 1, fraction = 4)
    Double multipleDigits;

    @Email
    String email;

    /**
     * Multiple emails should have no additional effect
     */
    @Email
    @Email
    String multipleEmails;

    @Future
    Date future;

    /**
     * Multiple future should have no additional effect
     */
    @Future
    @Future
    Date multipleFuture;

    @FutureOrPresent
    Date futureOrPresent;

    /**
     * Multiple futureOrPresent should have no additional effect
     */
    @FutureOrPresent
    @FutureOrPresent
    Date multipleFutureOrPresent;

    @Max(5)
    Integer max;

    /**
     * When multiple MAX are present, the smallest one should be used
     */
    @Max(1)
    @Max(5)
    @Max(4)
    Integer multipleMax;

    @Min(5)
    Integer min;

    /**
     * When multiple MIN are present, the biggest one should be used
     */
    @Min(5)
    @Min(10)
    @Min(0)
    Integer multipleMin;

    @Negative
    Integer negative;

    /**
     * Multiple negative should have no additional effect
     */
    @Negative
    @Negative
    Integer multipleNegative;

    @NegativeOrZero
    Integer negativeOrZero;

    /**
     * Multiple negativeOrZero should have no additional effect
     */
    @NegativeOrZero
    @NegativeOrZero
    Integer multipleNegativeOrZero;

    @NotBlank
    String notBlank;

    /**
     * Multiple notBlank should have no additional effect
     */
    @NotBlank
    @NotBlank
    String multipleNotBlank;

    @NotEmpty
    String notEmpty;

    /**
     * Multiple notEmpty should have no additional effect
     */
    @NotEmpty
    @NotEmpty
    String multipleNotEmpty;

    @NotNull
    Object notNull;

    /**
     * Multiple notNull should have no additional effect
     */
    @NotNull
    @NotNull
    Object multipleNotNull;

    @Null
    Object nullObject;

    /**
     * Multiple nullObject should have no additional effect
     */
    @Null
    @Null
    Object multipleNullObject;

    @Past
    Date past;

    /**
     * Multiple past should have no additional effect
     */
    @Past
    @Past
    Date multiplePast;

    @PastOrPresent
    Date pastOrPresent;

    /**
     * Multiple pastOrPresent should have no additional effect
     */
    @PastOrPresent
    @PastOrPresent
    Date multiplePastOrPresent;

    @Pattern(regexp = "^[a-z]+$")
    String pattern;

    /**
     * When multiple patterns are present, all should be used
     */
    @Pattern(regexp = "^[a-z]+$")
    @Pattern(regexp = "^[a-z_]+$")
    String multiplePattern;

    @Positive
    Integer positive;

    /**
     * Multiple positive should have no additional effect
     */
    @Positive
    @Positive
    Integer multiplePositive;

    @PositiveOrZero
    Integer positiveOrZero;

    /**
     * Multiple positiveOrZero should have no additional effect
     */
    @PositiveOrZero
    @PositiveOrZero
    Integer multiplePositiveOrZero;

    @Size(min = 5, max = 10)
    String size;

    /**
     * When multiple sizes are present,
     * the highest the minimum and the lowest maximum should be used (even from different annotations)
     */
    @Size(min = 2, max = 5)
    @Size(min = 5, max = 8)
    @Size(min = 1, max = 10)
    String multipleSize;
}

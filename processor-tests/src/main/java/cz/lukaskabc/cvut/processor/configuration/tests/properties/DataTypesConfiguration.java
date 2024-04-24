package cz.lukaskabc.cvut.processor.configuration.tests.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.boot.convert.PeriodUnit;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import java.io.File;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @see <a href="https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot/src/test/java/org/springframework/boot/convert">Spring Boot implementation</a>
 */
@Data
@ConfigurationProperties("datatypes")
public class DataTypesConfiguration {
    int intPrimitive; // ensure primitive types are documented
    Set<String> setOfStrings;
    List<Integer> listOfIntegers;
    Collection<Boolean> collectionOfBooleans;
    Map<String, String> mapOfStrings;
    String[] arrayOfStrings;
    char[] primitiveCharArray;
    DataTypesConfiguration[] arrayOfConfigurations;
    List<DataTypesConfiguration> listOfConfigurations;
    Optional<Double> optionalDouble;
    Date date;

    // default: milliseconds
    Duration durationNoUnit;

    @DurationUnit(ChronoUnit.DAYS)
    Duration days;

    InetAddress inetAddress;
    File file;

    // default: days
    Period period;

    @PeriodUnit(ChronoUnit.MONTHS)
    Period monthsPeriod;

    // default: bytes
    DataSize dataSize;

    @DataSizeUnit(DataUnit.MEGABYTES)
    DataSize megabytes;

    UUID uuid;
    Locale locale;
    Currency currency;
    Number number;
    Properties properties;
    Charset charset;
    Character character;
    Instant instant;

}

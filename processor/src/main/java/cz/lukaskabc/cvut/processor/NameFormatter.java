package cz.lukaskabc.cvut.processor;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Helper class providing methods for name formatting
 */
public class NameFormatter {

    private NameFormatter() {
    }

    /**
     * @param names
     * @return join names with underscore, protecting from duplicated underscores
     * @see <a href="https://docs.spring.io/spring-boot/docs/3.2.1/reference/htmlsingle/#features.external-config.typesafe-configuration-properties.relaxed-binding.environment-variables">Spring Boot binding from ENV</a>
     */
    public static String combine(String... names) {
        return Arrays.stream(names).map(s ->
                        s.trim()
                                .replaceAll("\\s+", "") // remove spaces
                                .replace("-", "") // remove hyphens
                                .replace(".", "_") // replace dots with underscores
                                .replaceAll("(^_+)|(_+$)", "") // trim underscores
                                .toUpperCase()
                ).filter(s -> !s.isEmpty())
                .collect(Collectors.joining("_"));
    }

    /**
     * @param strings
     * @return First non-empty parameter or empty string
     */
    public static String firstNonEmpty(String... strings) {
        return Arrays.stream(strings).filter(Objects::nonNull).filter(s -> !s.isEmpty())
                .findFirst().orElse("");
    }
}
